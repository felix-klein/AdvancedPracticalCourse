
/**
 * @cond
 ***********************************************************************************************************************
 *
 * Copyright (c) 2015, Infineon Technologies AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this list of conditions and the  following
 *   disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *   Neither the name of the copyright holders nor the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE  FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT  OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 **********************************************************************************************************************/
/*******************************************************************************
**                      Includes                                              **
*******************************************************************************/
#include "Main.h"
#include "Emo_RAM.h"
#include "globvars_FOC.h"
#include "commands.h"
#include "..\..\common.h"
#include "..\..\commonMC.h"

#define CURRENTMODE 3

/*******************************************************************************
**                      Private Function Declarations                         **
*******************************************************************************/
static void Main_lStartMotor(void);
static void Main_lStopMotor(void);

/*******************************************************************************
**                      Private Variable Definitions                          **
*******************************************************************************/
static uint8 bMotorRun = 0;

// interrupt --> main
static uint16 word1 = 0;
static uint8 word1byte1 = 0, word1byte2 = 0;
static uint16 word2 = 0, word3 = 0;
static sint16 motorspeed = 0;

// main --> interrupt
static uint8 data_prepared = 0, success = 0;
static uint8 calcCRC, recvCRC;

// main <--> interrupt
uint8 cmdstate = UNINITIALIZED;

/*******************************************************************************
**                      Global Function Definitions                           **
*******************************************************************************/

/** \brief Executes main code.
 *
 * \param None
 * \return None
 *
 */
int main(void)
{
  /*****************************************************************************
  ** initialization of the hardware modules based on the configuration done   **
  ** by using the IFXConfigWizard                                             **
  *****************************************************************************/
	uint8 boardnr = 0;
	uint8 *uint8ptr;
	
	/* Initialize device drivers, Note: Watchdog is already initialized in Bootloader */
	TLE_Init();
	
	// set Chip_Selected- and Chip_Deselected-interrupts according to boardnr in GPUDATA01
	boardnr = PMU->GPUDATA01.bit.DATA1;
	setBoardnr(boardnr);

	// FOC --> LED 0.1 and 0.2 on 
	PORT_ChangePin(LED_R, PORT_ACTION_CLEAR);
	PORT_ChangePin(LED_G, PORT_ACTION_CLEAR);
	PORT_ChangePin(LED_B, PORT_ACTION_SET);
	sendPosAnswer(0x0100 + CURRENTMODE);
	
	rxtxbuffer.datastruct_FOC = Emo_Focpar_Cfg;
	Emo_SetRefSpeed(2000);
	Emo_Init();
	
	cmdstate = READYFORSPIDATA;

	while (1)
	{
		/* Service watch-dog */
		WDT1_Service();

		/* wait for new spi data */
		if(cmdstate == NEWSPIDATATOPROCESS)
		{
			cmdstate = PROCESSINGSPIDATA;
			
			if(checkCommand(word1, word1byte1, word1byte2))
			{
				cmdstate = READYFORSPIDATA;
				continue;
			}
			
			switch (word1byte1)
			{
				case MODECONTROL:
					if(word1byte2 == GETMYMODE) sendPosAnswer(word1 + CURRENTMODE);
					else if(CURRENTMODE != word1byte2) changeMode(word1byte2);
					else sendAnswer(word1);
					cmdstate = READYFORSPIDATA;
					break;
				
				case LOADDATASET: 
					loadDataset(word1byte2);
					sendPosAnswer(word1);
					cmdstate = READYFORSPIDATA;
					break;
						
				case SENDDATASET:
					// just prepare data here, data is sent in interrupt
					rxtxbuffer.datastruct_FOC = Emo_Focpar_Cfg;

					uint8ptr = (uint8*) &rxtxbuffer;
					calcCRC = CRC8(uint8ptr, NUMBEROF_BYTES_FOC);
					data_prepared = 1;
					cmdstate = READYFORSPIDATA;
				
					break;
					
				case RECEIVEDATASET: 
					uint8ptr = (uint8*) &rxtxbuffer;
					calcCRC = CRC8(uint8ptr, NUMBEROF_BYTES_FOC);
					
					if(recvCRC == calcCRC)
					{
						if(bMotorRun == 1u)
						{
							Main_lStopMotor();
							bMotorRun = 0u;
						}
						Emo_Focpar_Cfg = rxtxbuffer.datastruct_FOC;
						updateDatasetDependencies();
						Emo_Init();
						sendPosAnswer(word1);
					}
					else
					{
						sendAnswer(word1);
					}
					cmdstate = READYFORSPIDATA;
					break;
						
				case CHANGEPARAMETER:
					if(bMotorRun == 1u)
					{
						Main_lStopMotor();
						bMotorRun = 0u;
					}
					changeSingleParameter(word1byte2, word2, word3);
					sendPosAnswer(word1);
					cmdstate = READYFORSPIDATA;
					break;
						
				case SAVEDATASET:
					success = saveCurrentDataset(word1byte2);
					// saving dataset needs more time than other commands --> SSC not enabled anymore
					// For writing answer to transmit-register SSC needs to be enabled, disable after writing answer to register
				
					if(success == 0) // saving was successful
					{
						sendPosAnswer(word1);
					} else // an error occured (1: position was not valid; 2: the checksums don't match; 3: the write protection was not off)
					{
						sendAnswer(word1);
					}
					cmdstate = READYFORSPIDATA;
					break;
				
					/*
					if(success)	sendPosAnswer(word1);
					else sendAnswer(word1);
					cmdstate = READYFORSPIDATA;
					break;			
					*/					
						
				case SETMOTORSPEED:
					Emo_SetRefSpeed(motorspeed);
					sendPosAnswer(word1);
					cmdstate = READYFORSPIDATA;
					break;
					
				case MOTORCONTROL:
					//errorCode = EMO_ERROR_MOTOR_INIT;
					if((word1byte2 == START_MOTOR) && (bMotorRun == 0u))
					{	
						// Initialize and start motor
						//Emo_Init();
						Main_lStartMotor();
						bMotorRun = 1u;
					}
					if (word1byte2 == STOP_MOTOR)	
					{
						// Stop  motor
						Main_lStopMotor();
						bMotorRun = 0u;
					}
					sendPosAnswer(word1);
					cmdstate = READYFORSPIDATA;
					break;
				
				case CHECK_ERROR:
					checkErrorsMC(&(Emo_Status.MotorStartError));
					cmdstate = READYFORSPIDATA;
					break;
				
				default:
					/* wrong command */
					cmdstate = READYFORSPIDATA;
					break;
			}
		}
  }
} /* End of main() */

static void Main_lStartMotor(void)
{
	Emo_StartMotor();
}


static void Main_lStopMotor(void)
{
	Emo_StopMotor();
}

// In case the VCP supply fails the motor stops
void BDRV_Diag_Supply(void)
{
	Main_lStopMotor();
}

/* in case an over current is detected on one of   **
** the MOSFETs then the motor will be switched off */
void BDRV_Diag(void)
{
	Main_lStopMotor();
}


// ----------------------------------------interrupts---------------------------------
void Data_Received(void)
{
	uint16 receivedword = SSC2_ReadWord();
	//internal interrupt state
	static uint8 waitingFor = NEWCOMMAND;
	static uint8 sendcounter = 0, receivecounter = 0, messagesToReceive = 0;
	
	//BOARDCONTROL, RESET
	if((cmdstate != READYFORSPIDATA) && (receivedword != 0x0902)) return;

	switch (waitingFor)
	{
		case NEWCOMMAND:  // ------------------------------ NEWCOMMAND -------------------
			word1 = receivedword;
			word1byte1 = 0xFF & (receivedword >> 8);
			word1byte2 = 0xFF & receivedword;
			
			switch (word1byte1)
			{
				/* catch "special" cases that need another Message */
				case 0x03:
					/* send data to master when receiving next message, now send number of 16bit-messages to send */
					SSC2_SendWord(NUMBEROF_MESSAGES_FOC);
					waitingFor = SENDDATA;
					cmdstate = NEWSPIDATATOPROCESS;
					break;
				case 0x04:
					waitingFor = RECEIVEDATA;
					messagesToReceive = word1byte2;
					break;
				case 0x05:
					if (isValueInArray(word1byte2, indices_16bit, indices_16bit_size) == 1) /* 16Bit data --> wait for one more message */
					{
						waitingFor = DATA_1_OF_1;
					}
					else /* 32Bit data --> wait for two more messages */
					{
						waitingFor = DATA_1_OF_2;
					}
					break;
				case 0x07: /* motorspeed: wait for one more message (=motorspeed) */
					waitingFor = MOTORSPEED;
					break;
				default: /* just one message for this command --> data complete */
					cmdstate = NEWSPIDATATOPROCESS;
					break;
			}
			break;
		
		case DATA_1_OF_1:
			word2 = receivedword;
			waitingFor = NEWCOMMAND;
			cmdstate = NEWSPIDATATOPROCESS;
			break;
		
		case DATA_1_OF_2: 
			word2 = receivedword;
			waitingFor = DATA_2_OF_2;
			break;
		
		case DATA_2_OF_2:  
			word3 = receivedword;
			waitingFor = NEWCOMMAND;
			cmdstate = NEWSPIDATATOPROCESS;
			break;

		case MOTORSPEED:  
			// receivedword (unsigned int) contains an signed value! --> first bit = 1 --> negative
			if(receivedword > 32767) motorspeed = 0x8000 + (receivedword & 0x7FFF);
			else motorspeed = receivedword;
			waitingFor = NEWCOMMAND;
			cmdstate = NEWSPIDATATOPROCESS;
			break;
		
		case SENDDATA:  // ------------------------------ SENDDATA -----------------------
			// send one 16bit-message at each interrupt (only after data is prepared in main loop)
			if (data_prepared == 1)
			{
				if(sendcounter < NUMBEROF_MESSAGES_FOC)
				{
					SSC2_SendWord(rxtxbuffer.dataarray_FOC[sendcounter]);
					sendcounter++;
				}
				else  // send CRC
				{
					SSC2_SendWord(calcCRC);
					sendcounter = 0;
					data_prepared = 0;
					waitingFor = NEWCOMMAND;
					cmdstate = READYFORSPIDATA;
				}
			}
			break;
			
		case RECEIVEDATA:  // ------------------------------ RECEIVEDATA -----------------
			if(receivecounter < messagesToReceive)
			{
				// collect data here and write them to emoconfig variable when complete (in main, not in interrupt)
				rxtxbuffer.dataarray_FOC[receivecounter] = receivedword;
				receivecounter++;
			}
			else
			{
				recvCRC = (uint8_t)receivedword;
				receivecounter++;
				
				// all messages received
				receivecounter = 0;
				messagesToReceive = 0;
				waitingFor = NEWCOMMAND;
				cmdstate = NEWSPIDATATOPROCESS; // 0x18000B19
			}
			break;
			
		default:  // ------------------------------ default ------------------------------
			break;
	} /* end of switch (waitingFor) */
}
