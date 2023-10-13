
#include "commands.h"
#include "tle_device.h"
#include "..\..\auto_address.h" 
#include "globvars_HALL.h"
#include "..\..\commonMC.h"

//--------------------0x02: loadDataset--------------------
void loadDataset(uint8 datasetnr)
{
	switch (datasetnr) 
	{
		case DATASET0:
			Emo_Hallpar_Cfg = dataset0; 
			break;
		case DATASET1:
			Emo_Hallpar_Cfg = dataset1;
			break;
		case DATASET2:
			Emo_Hallpar_Cfg = dataset2;
			break;
		case DATASET3:
			Emo_Hallpar_Cfg = dataset3;
			break;
		case CUSTOMDATASET0:
			Emo_Hallpar_Cfg = customdataset0;
			break;
		case CUSTOMDATASET1:
			Emo_Hallpar_Cfg = customdataset1;
			break;
		case CUSTOMDATASET2:
			Emo_Hallpar_Cfg = customdataset2;
			break;
		case CUSTOMDATASET3:
			Emo_Hallpar_Cfg = customdataset3;
			break;
		default:
			break;
	}
	updateDatasetDependencies();	
	Emo_Init();
}

//--------------------0x05: changeParameter--------------------
void changeSingleParameter(uint8 index, uint16 data1, uint16 data2)
{
	rxtxbuffer.datastruct_HALL = Emo_Hallpar_Cfg;
	if (isValueInArray(index, indices_16bit, indices_16bit_size) == 1) // 16bit variable
	{	
		rxtxbuffer.dataarray_HALL[index] = data1;
	}
	else // 32bit var 
	{			
		rxtxbuffer.dataarray_HALL[index] = data2;
		rxtxbuffer.dataarray_HALL[index+1] = data1;
	}
	Emo_Hallpar_Cfg = rxtxbuffer.datastruct_HALL;
	updateDatasetDependencies();
	Emo_Init();
}

//--------------------0x06: saveCurrentDataset--------------------
uint8 saveCurrentDataset(uint8 position)
{
		uint8 erase_error = 0, writeprotection_off = 0, checksumRAM = 0, checksumROM = 0;
		uint32 flashpageStart = 0;
		uint8 *ptrToROMData;
		uint8 *ptrToRAMData = (uint8*)&rxtxbuffer;
		rxtxbuffer.datastruct_HALL = Emo_Hallpar_Cfg;
		
		switch (position)
		{
			case POSITION0:
				flashpageStart = STARTOFCUSTOMDATASETS + OFFSET_DATASET0;
				break;
			case POSITION1:
				flashpageStart = STARTOFCUSTOMDATASETS + OFFSET_DATASET1;
				break;
			case POSITION2:
				flashpageStart = STARTOFCUSTOMDATASETS + OFFSET_DATASET2;
				break;
			case POSITION3:
				flashpageStart = STARTOFCUSTOMDATASETS + OFFSET_DATASET3;
				break;
		}

		// erase page
		writeprotection_off = USER_CFLASH_WR_PROT_DIS((uint16) 0);
		if(writeprotection_off == 0)
		{
			erase_error = USER_ERASEPG((uint32*)flashpageStart, 0);
		
			if (erase_error == 0u)
			{
				// write data
				uint8 i;
				checksumRAM = CRC8(ptrToRAMData, NROFBYTES);
				ptrToROMData = (uint8*)flashpageStart;
				
				WDT1_SOW_Service((uint8)1);
				USER_OPENAB((uint32*)flashpageStart);
				WDT1_Service();
				
				for (i = 0; i < NROFBYTES; i++)
				{
					ptrToROMData[i] = ptrToRAMData[i];
				}
				
				WDT1_SOW_Service((uint8)1);
				USER_PROG(0);
				WDT1_Service();
				
				checksumROM = CRC8(ptrToROMData, NROFBYTES);
				if(checksumRAM == checksumROM)
				{
					return 1;
				}
				else
				{
					return 0;
				}
			}
		}
		return 0;
}

//--------------------other functions--------------------
// updateDatasetDependencies
void updateDatasetDependencies()
{
	/* FOC_PWM_FREQ  */
	ccu6_t12_freq = Emo_Hallpar_Cfg.PWM_Frequency;
	ccu6_t12_period_sel = 2; /* constant */
	pwm_period = 1e6/ccu6_t12_freq;
	ccu6_tctr0_bit7 = (CCU6_TCTR0 & 0x0080) >> 7; /* constant */
	ccu6_t12period = ccu6_t12_period_sel<1?CCU6_T12_TICK:ccu6_t12_period_sel<2?CCU6_T12_CLK*CCU6_T12_TIME/(1+1*ccu6_tctr0_bit7):((CCU6_T12_CLK*1e6)/ccu6_t12_freq)/(1+1*ccu6_tctr0_bit7);
	ccu6_t12pr = ccu6_t12_period_sel<1?CCU6_T12_TICK-1:ccu6_t12_period_sel<2?CCU6_T12_CLK*CCU6_T12_TIME/(1+1*ccu6_tctr0_bit7)-1:((CCU6_T12_CLK*1e6)/ccu6_t12_freq)/(1+1*ccu6_tctr0_bit7)-1;
	ccu6_cc60sr = CCU6_CH0_CMP_TICK+(CCU6_T12_CLK*CCU6_CH0_CMP_TIME)+(ccu6_t12pr*CCU6_CH0_CMP_DC/100);
	ccu6_cc61sr = CCU6_CH1_CMP_TICK+(CCU6_T12_CLK*CCU6_CH1_CMP_TIME)+(ccu6_t12pr*CCU6_CH1_CMP_DC/100);
	ccu6_cc62sr = CCU6_CH2_CMP_TICK+(CCU6_T12_CLK*CCU6_CH2_CMP_TIME)+(ccu6_t12pr*CCU6_CH2_CMP_DC/100);

	/* BCHALL_ANGLE_DELAY_EN, BCHALL_DELAY_ANGLE */
  	EmoCcu_HallStatus.DelayAngle = (Emo_Hallpar_Cfg.angleDelayEn == 0)? ((uint8)0) : ((uint8)Emo_Hallpar_Cfg.delayAngle);

	/* BCHALL_DELAY_MINSPEED */
	EmoCcu_HallStatus.DelayMinSpeed = Emo_Hallpar_Cfg.delayMinspeed;

	/* BCHALL_SPEED_KP, BCHALL_SPEED_KI */
  	Emo_Ctrl.SpeedPi.Kp = (sint16)Emo_Hallpar_Cfg.SpeedPi_Kp;
  	Emo_Ctrl.SpeedPi.Ki = (sint16)Emo_Hallpar_Cfg.SpeedPi_Ki;

	/* BCHALL_SPEED_IMIN, BCHALL_SPEED_IMAX, BCHALL_SPEED_PIMIN, BCHALL_SPEED_PIMAX */
  	Emo_Ctrl.SpeedPi.IMin = (sint16)((((sint32)Emo_Hallpar_Cfg.speedIMin) * ccu6_t12pr)/100);
  	Emo_Ctrl.SpeedPi.IMax = (sint16)((((sint32)Emo_Hallpar_Cfg.speedIMax) * ccu6_t12pr)/100);
  	Emo_Ctrl.SpeedPi.PiMin = (sint16)((((sint32)Emo_Hallpar_Cfg.speedPiMin) * ccu6_t12pr)/100);
  	Emo_Ctrl.SpeedPi.PiMax = (sint16)((((sint32)Emo_Hallpar_Cfg.speedPiMax) * ccu6_t12pr)/100);
		
	init_emoccucfg(Emo_Hallpar_Cfg.offset60degEn);
	CCU6_Init();
}

#define Ccu6_SetPtns(CURHS, EXPHS, MCMPS) \
  ( (((uint32)(MCMPS)) << 0u) | \
  (((uint32)(EXPHS)) << 8u) | \
  (((uint32)(CURHS)) << 11U ) )

void init_emoccucfg(uint8 offset60deg_en)
{
	if(offset60deg_en == 0)
	{
	    EmoCcu_Cfg.HallOutPtns[0] = (uint16)Ccu6_SetPtns(0, 0, 0x00); /* Hall pattern=0, forward direction (error) */
	    EmoCcu_Cfg.HallOutPtns[1] = (uint16)Ccu6_SetPtns(1, 3, 0x31); /* Hall pattern=1, forward direction */
	    EmoCcu_Cfg.HallOutPtns[2] = (uint16)Ccu6_SetPtns(2, 6, 0x07); /* Hall pattern=2, forward direction */
	    EmoCcu_Cfg.HallOutPtns[3] = (uint16)Ccu6_SetPtns(3, 2, 0x34); /* Hall pattern=3, forward direction */
	    EmoCcu_Cfg.HallOutPtns[4] = (uint16)Ccu6_SetPtns(4, 5, 0x1C); /* Hall pattern=4, forward direction */
	    EmoCcu_Cfg.HallOutPtns[5] = (uint16)Ccu6_SetPtns(5, 1, 0x0D); /* Hall pattern=5, forward direction */
	    EmoCcu_Cfg.HallOutPtns[6] = (uint16)Ccu6_SetPtns(6, 4, 0x13); /* Hall pattern=6, forward direction */
	    EmoCcu_Cfg.HallOutPtns[7] = (uint16)Ccu6_SetPtns(0, 0, 0x00); /* Hall pattern=7, forward direction (error) */
	    EmoCcu_Cfg.HallOutPtns[8] = (uint16)Ccu6_SetPtns(0, 0, 0x00); /* Hall pattern=0, reverse direction (error) */
	    EmoCcu_Cfg.HallOutPtns[9] = (uint16)Ccu6_SetPtns(1, 5, 0x13); /* Hall pattern=1, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[10] = (uint16)Ccu6_SetPtns(2, 3, 0x0D); /* Hall pattern=2, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[11] = (uint16)Ccu6_SetPtns(3, 1, 0x1C); /* Hall pattern=3, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[12] = (uint16)Ccu6_SetPtns(4, 6, 0x34); /* Hall pattern=4, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[13] = (uint16)Ccu6_SetPtns(5, 4, 0x07), /* Hall pattern=5, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[14] = (uint16)Ccu6_SetPtns(6, 2, 0x31); /* Hall pattern=6, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[15] = (uint16)Ccu6_SetPtns(0, 0, 0x00); /* Hall pattern=7, reverse direction (error) */
	   
	    EmoCcu_Cfg.T12Moden[0] = (uint8)(0x00);        /* Hall pattern=0, forward direction (error) */
	    EmoCcu_Cfg.T12Moden[1] = (uint8)(0x80 + 0x30); /* Hall pattern=1, forward direction */
	    EmoCcu_Cfg.T12Moden[2] = (uint8)(0x80 + 0x03); /* Hall pattern=2, forward direction */
	    EmoCcu_Cfg.T12Moden[3] = (uint8)(0x80 + 0x33); /* Hall pattern=3, forward direction */
	    EmoCcu_Cfg.T12Moden[4] = (uint8)(0x80 + 0x0C); /* Hall pattern=4, forward direction */
	    EmoCcu_Cfg.T12Moden[5] = (uint8)(0x80 + 0x3C); /* Hall pattern=5, forward direction */
	    EmoCcu_Cfg.T12Moden[6] = (uint8)(0x80 + 0x0F); /* Hall pattern=6, forward direction */
	    EmoCcu_Cfg.T12Moden[7] = (uint8)(0x00);        /* Hall pattern=7, forward direction (error) */
	    EmoCcu_Cfg.T12Moden[8] = (uint8)(0x00);        /* Hall pattern=0, reverse direction (error) */
	    EmoCcu_Cfg.T12Moden[9] = (uint8)(0x80 + 0x03); /* Hall pattern=1, reverse direction */
	    EmoCcu_Cfg.T12Moden[10] = (uint8)(0x80 + 0x0C); /* Hall pattern=2, reverse direction */
	    EmoCcu_Cfg.T12Moden[11] = (uint8)(0x80 + 0x0F); /* Hall pattern=3, reverse direction */
	    EmoCcu_Cfg.T12Moden[12] = (uint8)(0x80 + 0x30); /* Hall pattern=4, reverse direction */
	    EmoCcu_Cfg.T12Moden[13] = (uint8)(0x80 + 0x33); /* Hall pattern=5, reverse direction */
	    EmoCcu_Cfg.T12Moden[14] = (uint8)(0x80 + 0x3C); /* Hall pattern=6, reverse direction */
	    EmoCcu_Cfg.T12Moden[15] = (uint8)(0x00);        /* Hall pattern=7, reverse direction (error) */
	}
	else
	{
	    EmoCcu_Cfg.HallOutPtns[0] = (uint16)Ccu6_SetPtns(0, 0, 0x00); /* Hall pattern=0, forward direction (error) */
	    EmoCcu_Cfg.HallOutPtns[1] = (uint16)Ccu6_SetPtns(1, 3, 0x0D); /* Hall pattern=1, forward direction */
	    EmoCcu_Cfg.HallOutPtns[2] = (uint16)Ccu6_SetPtns(2, 6, 0x34); /* Hall pattern=2, forward direction */
	    EmoCcu_Cfg.HallOutPtns[3] = (uint16)Ccu6_SetPtns(3, 2, 0x31); /* Hall pattern=3, forward direction */
	    EmoCcu_Cfg.HallOutPtns[4] = (uint16)Ccu6_SetPtns(4, 5, 0x13); /* Hall pattern=4, forward direction */
	    EmoCcu_Cfg.HallOutPtns[5] = (uint16)Ccu6_SetPtns(5, 1, 0x1C); /* Hall pattern=5, forward direction */
	    EmoCcu_Cfg.HallOutPtns[6] = (uint16)Ccu6_SetPtns(6, 4, 0x07); /* Hall pattern=6, forward direction */
	    EmoCcu_Cfg.HallOutPtns[7] = (uint16)Ccu6_SetPtns(0, 0, 0x00); /* Hall pattern=7, forward direction (error) */
	    EmoCcu_Cfg.HallOutPtns[8] = (uint16)Ccu6_SetPtns(0, 0, 0x00); /* Hall pattern=0, reverse direction (error) */
	    EmoCcu_Cfg.HallOutPtns[9] = (uint16)Ccu6_SetPtns(1, 5, 0x07); /* Hall pattern=1, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[10] = (uint16)Ccu6_SetPtns(2, 3, 0x1C); /* Hall pattern=2, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[11] = (uint16)Ccu6_SetPtns(3, 1, 0x13); /* Hall pattern=3, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[12] = (uint16)Ccu6_SetPtns(4, 6, 0x31); /* Hall pattern=4, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[13] = (uint16)Ccu6_SetPtns(5, 4, 0x34); /* Hall pattern=5, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[14] = (uint16)Ccu6_SetPtns(6, 2, 0x0D); /* Hall pattern=6, reverse direction */
	    EmoCcu_Cfg.HallOutPtns[15] = (uint16)Ccu6_SetPtns(0, 0, 0x00); /* Hall pattern=7, reverse direction (error) */
	
		EmoCcu_Cfg.T12Moden[0] = (uint8)(0x00);        /* Hall pattern=0, forward direction (error) */
	    EmoCcu_Cfg.T12Moden[1] = (uint8)(0x80 + 0x3C); /* Hall pattern=1, forward direction */
	    EmoCcu_Cfg.T12Moden[2] = (uint8)(0x80 + 0x33); /* Hall pattern=2, forward direction */
	    EmoCcu_Cfg.T12Moden[3] = (uint8)(0x80 + 0x30); /* Hall pattern=3, forward direction */
	    EmoCcu_Cfg.T12Moden[4] = (uint8)(0x80 + 0x0F); /* Hall pattern=4, forward direction */
	    EmoCcu_Cfg.T12Moden[5] = (uint8)(0x80 + 0x0C); /* Hall pattern=5, forward direction */
	    EmoCcu_Cfg.T12Moden[6] = (uint8)(0x80 + 0x03); /* Hall pattern=6, forward direction */
	    EmoCcu_Cfg.T12Moden[7] = (uint8)(0x00);        /* Hall pattern=7, forward direction (error) */
	    EmoCcu_Cfg.T12Moden[8] = (uint8)(0x00);        /* Hall pattern=0, reverse direction (error) */
	    EmoCcu_Cfg.T12Moden[9] = (uint8)(0x80 + 0x33); /* Hall pattern=1, reverse direction */
	    EmoCcu_Cfg.T12Moden[10] = (uint8)(0x80 + 0x0F); /* Hall pattern=2, reverse direction */
	    EmoCcu_Cfg.T12Moden[11] = (uint8)(0x80 + 0x03); /* Hall pattern=3, reverse direction */
	    EmoCcu_Cfg.T12Moden[12] = (uint8)(0x80 + 0x3C); /* Hall pattern=4, reverse direction */
	    EmoCcu_Cfg.T12Moden[13] = (uint8)(0x80 + 0x30); /* Hall pattern=5, reverse direction */
	    EmoCcu_Cfg.T12Moden[14] = (uint8)(0x80 + 0x0C); /* Hall pattern=6, reverse direction */
	    EmoCcu_Cfg.T12Moden[15] = (uint8)(0x00);        /* Hall pattern=7, reverse direction (error) */
	}
}

