/**
 * @cond
 ***********************************************************************************************************************
 *
 * Copyright (c) 2015, Infineon Technologies AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,are permitted provided that the
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
**                      Author(s) Identity                                    **
********************************************************************************
**                                                                            **
** Initials     Name                                                          **
** ---------------------------------------------------------------------------**
** ISLE         ISLE Ilmenau                                                  **
**                                                                            **
**                                                                            **
*******************************************************************************/

/*******************************************************************************
**                      Revision Control History                              **
*******************************************************************************/
/*
 * V0.1.0: 2016-02-12, ISLE:   Initial version
 */

/*******************************************************************************
**                      Includes                                              **
*******************************************************************************/
#include "Emo.h"
#include "BEMF.h"
#include "globvars_BEMF.h"
#include "..\..\commonMC.h"

/*******************************************************************************
**                      Global Constant Definitions to be changed             **
*******************************************************************************/
/* Constants for MCMOUTS register */
#define CCU6_MASK_MCMOUTS_ENABLE_MCMOUTS  (0x00BFu)
TEmo_Status Emo_Status;

/** \brief Initializes E-Motor HW and SW.
 *
 * \param None
 * \return Error or EMO_ERROR_NONE
 *
 * \ingroup emo_api
 */
uint32 Emo_Init(void)
{
    if (Emo_Status.MotorState != EMO_MOTOR_STATE_UNINIT)
    {
        /* Error detected: return with error */
        return EMO_ERROR_MOTOR_INIT;
    }

    GPT12E_T6_Int_Dis();
    GPT12E_T2_Start();
    GPT12E_T3_Start();
    /*Initialize Comparators */
    //Mf_Init();
    /* Initialize BEMF parameters */
    Emo_lInitBEMFPar();
    /* Initialize motor state */
    Emo_Status.MotorState = EMO_MOTOR_STATE_STOP;
    /* Return without error */
    return EMO_ERROR_NONE;
} /* End of Emo_Init */


/** \brief Sets the reference speed of the motor.
 *
 * \param Reference speed [rpm]
 * \return None
 *
 * \ingroup emo_api
 */
void Emo_SetRefSpeed(sint16 RefSpeed)
{
		Emo_Ctrl.RefSpeed=RefSpeed;
} /* End of Emo_SetRefSpeed */

/** \brief Gets motor speed.
 *
 * \param None
 * \return None
 *
 * \ingroup emo_api
 */
uint32 Emo_GetSpeed(void)
{
		return (Emo_Ctrl.ActSpeed < 0 ? ((uint16) (-Emo_Ctrl.ActSpeed)) : ((uint16) Emo_Ctrl.ActSpeed));
}

/** \brief Starts the motor.
 *
 * \param None
 * \return Error or EMO_ERROR_NONE
 *
 * \note Service should only be called when motor is stopped.
 *
 * \ingroup emo_api
 */
uint32 Emo_StartMotor(void)
{
    uint32 angle6;      /* 6 times start angle  */
    uint16 sector_no;

    if (Emo_Status.MotorState != EMO_MOTOR_STATE_STOP)
    {
        /* Error detected: return with error */
        return EMO_ERROR_MOTOR_NOT_STOPPED;
    }

		Emo_Ctrl.DirectionIndex = (Emo_Ctrl.RefSpeed > 0 ? 8 : 0);

    BackEMF_InitPatterns();
    /* Hallsampling by Hardware turned off  */
    CCU6->T12MSEL.reg = (CCU6->T12MSEL.reg & (~0x7000)) | 0x3000;
    /* Start PWM */
    CCU6_StartTmr_T12();
    /* Enable outputs immediately */
    Emo_Ctrl.vout = Emo_Ctrl.Period;
    CCU6_LoadShadowRegister_CC60(Emo_Ctrl.vout);
    CCU6_LoadShadowRegister_CC61(Emo_Ctrl.vout);
    CCU6_LoadShadowRegister_CC62(Emo_Ctrl.vout);
    CCU6_EnableST_T12();
    /* Enable bridge (after starting PWM according to UM) */
    BDRV_Set_Bridge(Ch_PWM, Ch_PWM, Ch_PWM, Ch_PWM, Ch_PWM, Ch_PWM);
    /* Initialize variables */
    Emo_lInitBEMFVar();
    /* Set start state */
    Emo_Status.MotorState = EMO_MOTOR_STATE_SPEEDTEST1;
    MF->BEMFC_CTRL_STS.reg = MF->BEMFC_CTRL_STS.reg | (0x0008);   //turn off demag_filter
    BackEMF_SwitchtoRamp();
    angle6 = 6 * (uint32)Emo_Bemf.StartAngle;
    sector_no = (angle6 >> 16);
    /* every 60� of motor angle, force commutation  */
    BackEMF_CommutationDelay();             //calculate commutation delay, for smooth change to hall event control
    BackEMF_SetPatterns();                  //immediately switch patterns to next sector
    Emo_Bemf.sector_no_sp = sector_no;
    Emo_Bemf.StartAngle = 0;
    angle6 = 6 * (uint32)Emo_Bemf.StartAngle;
    sector_no = (angle6 >> 16);
    /* every 60� of motor angle, force commutation  */
    BackEMF_CommutationDelay();             //calculate commutation delay, for smooth change to hall event control
    BackEMF_SetPatterns();                  //immediately switch patterns to next sector
    Emo_Bemf.sector_no_sp = sector_no;
    Emo_Ctrl.WronghallCounter = 0;
    Emo_Bemf.CountStart = Emo_Bemf.TimeSpeedzero;
    Emo_Ctrl.SpeedPi.PiMin = Emo_Ctrl.StartSpeedPwmMin;
    Emo_Ctrl.SpeedPi.IMin = Emo_Ctrl.StartSpeedPwmMin;
    /* Return without error */
    return EMO_ERROR_NONE;
} /* End of Emo_StartMotor */


/** \brief Stops the motor.
 *
 * \param None
 * \return Error code
 *
 * \note Service should only be called when motor was started before.
 *
 * \ingroup emo_api
 */
uint32 Emo_StopMotor(void)
{
    /* Disable bridge */
    BDRV_Set_Bridge(Ch_Off, Ch_Off, Ch_Off, Ch_Off, Ch_Off, Ch_Off);
    /* Stop PWM */
    CCU6_StopTmr_T12();
    /* Set stop state */
    Emo_Status.MotorState = EMO_MOTOR_STATE_STOP;
    /* Return without error */
    return EMO_ERROR_NONE;
} /* End of Emo_StopMotor */

void Emo_lInitBEMFPar(void)
{
    float KU;
    static float x;
    Emo_Status.MotorStartError = 0;
    KU = 12.0;
    Emo_Ctrl.Period = ccu6_t12pr;
    /* Initialize parameters for BEMF */
    Emo_Bemf.PolePair = Emo_Bemfpar_Cfg.PolePair;
    Emo_Bemf.EnableFrZero = Emo_Bemfpar_Cfg.EnableFrZero;
    x = Emo_Bemfpar_Cfg.TimeSpeedzero * GPT1_BASE_CLK * (1e+6) / (65536 - GPT12E_T2_TICK);

    if (x > 32767)
    {
        x = 32767;
        Emo_Status.MotorStartError |= EMO_ERROR_STARTTIME;
    }

    if (x < 1)
    {
        x = 1;
        Emo_Status.MotorStartError |= EMO_ERROR_STARTTIME;
    }

    Emo_Bemf.TimeSpeedzero = x;
    Emo_Bemf.StartEndSpeed = Emo_Bemfpar_Cfg.StartSpeedEnd;
    x = ((0xFFFF - GPT12E_T2_TICK) * 32.0) / SCU_FSYS * Emo_Bemfpar_Cfg.StartSpeedSlewRate * 65536.0;

    if (x > 2147483647)
    {
        x = 2147483647;
        Emo_Status.MotorStartError |= EMO_ERROR_SPEEDSLEWRATE;
    }

    if (x < 1)
    {
        x = 1;
        Emo_Status.MotorStartError |= EMO_ERROR_SPEEDSLEWRATE;
    }

    Emo_Bemf.StartSpeedSlewRate = x;
    Emo_Bemf.SpeedtoFrequency = (32768.0 * (1.0 / ccu6_t12_freq) * 32768.0 / 30.0);
    x = ((0xFFFF - GPT12E_T2_TICK) * 32.0) / SCU_FSYS * Emo_Bemfpar_Cfg.RunSpeedSlewRate * 65536.0;

    if (x > 2147483647)
    {
        x = 2147483647;
        Emo_Status.MotorStartError |= EMO_ERROR_SPEEDSLEWRATE;
    }

    if (x < 1)
    {
        x = 1;
        Emo_Status.MotorStartError |= EMO_ERROR_SPEEDSLEWRATE;
    }

    Emo_Ctrl.SpeedSlewRate = x;
    Emo_Ctrl.StartSpeedPwmMin = Emo_Bemfpar_Cfg.StartSpeedPwmMin * ccu6_t12pr;
    Emo_Ctrl.SpeedPwmMin = Emo_Bemfpar_Cfg.SpeedPwmMin * ccu6_t12pr;
    Emo_Ctrl.SpeedPwmBeginMin = Emo_Bemfpar_Cfg.SpeedBeginPwmMin * ccu6_t12pr;
    x = Emo_Ctrl.StartSpeedPwmMin / Emo_Bemfpar_Cfg.StartSpeedEnd * 2048.0; /* vt/n*32767/16 */

    if (x > 32767.0)
    {
        x = 32767;
    }

    Emo_Ctrl.StartSpeedPwmFactor = x;
    Emo_Ctrl.StartSpeedPwmMinOffset = Emo_Bemfpar_Cfg.StartSpeedPwmMinOffset * ccu6_t12pr;
    Emo_Ctrl.SpeedPi.Kp = Emo_Bemfpar_Cfg.SpeedPi_Kp;
    Emo_Ctrl.SpeedPi.Ki = Emo_Bemfpar_Cfg.SpeedPi_Ki;
    Emo_Ctrl.SpeedPi.PiMin = Emo_Ctrl.StartSpeedPwmMin;
    Emo_Ctrl.SpeedPi.PiMax = Emo_Ctrl.Period - 50;
    Emo_Ctrl.SpeedPi.IMin = Emo_Ctrl.StartSpeedPwmMin;
    Emo_Ctrl.SpeedPi.IMax = Emo_Ctrl.Period - 50;
    Emo_Ctrl.SpeedLevelSwitchOn = Emo_Bemfpar_Cfg.SpeedLevelSwitchOn;
    x = (1.0 / ccu6_t12_freq) / (Emo_Bemfpar_Cfg.TimeConstantSpeedFilter) * 32768.0;

    if (x > 32767)
    {
        x = 32767;
        Emo_Status.MotorStartError |= EMO_ERROR_T_SPEED_LP;
    }

    if (x < 1)
    {
        x = 1;
        Emo_Status.MotorStartError |= EMO_ERROR_T_SPEED_LP;
    }

    Emo_Ctrl.SpeedLp.CoefA = x;
    Emo_Ctrl.SpeedLp.CoefB = x;
    Emo_Ctrl.DiffTimeLp.CoefA = x;
    Emo_Ctrl.DiffTimeLp.CoefB = x;
    Emo_Ctrl.SpeedLpdisplay.CoefA = 1000;
    Emo_Ctrl.SpeedLpdisplay.CoefB = 1000;
    Emo_Ctrl.SpeedConst = GPT1_T3_CLK * 1000000.0 * 60.0 / 6.0;
    Emo_Ctrl.StartVoltage = 2.0 / KU * 32767.0;
    Emo_Bemf.speedtestenable = Emo_Bemfpar_Cfg.SpeedTestEnable;
} /* End of Emo_lInitBEMFPar */

void Emo_lInitBEMFVar(void)
{
    Emo_Ctrl.ActSpeed = 0;
    Emo_Ctrl.SpeedLp.Out = 0;
    Emo_Bemf.StartSpeedSlopeMem = 0;
    Emo_Bemf.CountStart = Emo_Bemf.TimeSpeedzero;
    Emo_Ctrl.SpeedLpdisplay.Out = 0;
    Emo_Ctrl.SpeedLp.Out = 0;
    Emo_Bemf.bemf_counter = 0;
    Emo_Bemf.StartAngle = 0;
} /* End of Emo_lInitBEMFVar */


/** \brief Handles T2 overflow.
 *
 * \param None
 * \return None
 *
 * \ingroup emo_api
 */
void Emo_HandleT2Overflow(void)
{
    uint16 i;
    /*  Rechenzeit 11.4 us    */

    switch (Emo_Status.MotorState)
    {
        case  EMO_MOTOR_STATE_SPEEDTEST1:
        {
            Emo_Bemf.timestatus++;
        } break;

        case  EMO_MOTOR_STATE_START0:
        {
            if (Emo_Bemf.CountStart == 0)
            {
                if (Emo_Ctrl.RefSpeed > 0)
                {
                    /* positive rotation */
                    /* apply start up ramp */
                    Emo_Ctrl.DirectionIndex = 8;    /* config Block Commutation for positive speeds */
                    Emo_Bemf.StartSpeedSlope = Mat_Ramp(Emo_Bemf.StartEndSpeed, Emo_Bemf.StartSpeedSlewRate, &Emo_Bemf.StartSpeedSlopeMem);
                    /* calculate frequency out of ramp */
                    Emo_Bemf.StartFrequencySlope = __ssat(Mat_FixMulScale(Emo_Bemf.StartSpeedSlope, Emo_Bemf.SpeedtoFrequency * Emo_Bemf.PolePair, 0), MAT_FIX_SAT);

                    if (Emo_Bemf.StartSpeedSlope == Emo_Bemf.StartEndSpeed)
                    {
                        /* end speed reached */
#if (EMO_RUN==1)
                        Emo_Status.MotorState = EMO_MOTOR_STATE_START1;                       /*Start BEMF */
                        /* Switch to Back-EMF evaluation per hardware once minimum speed is reached */
#endif
                    }
                }
                else
                {
                    /* negative rotation */
                    /* apply start up ramp */
                    Emo_Ctrl.DirectionIndex = 0;    /* config Block Commutation for negative speeds */
                    Emo_Bemf.StartSpeedSlope = Mat_Ramp(-Emo_Bemf.StartEndSpeed, Emo_Bemf.StartSpeedSlewRate, &Emo_Bemf.StartSpeedSlopeMem);
                    /* calculate frequency out of ramp */
                    Emo_Bemf.StartFrequencySlope = __ssat(Mat_FixMulScale(Emo_Bemf.StartSpeedSlope, Emo_Bemf.SpeedtoFrequency * Emo_Bemf.PolePair, 0), MAT_FIX_SAT);

                    if (Emo_Bemf.StartSpeedSlope == -Emo_Bemf.StartEndSpeed)
                    {
#if (EMO_RUN==1)
                        Emo_Status.MotorState = EMO_MOTOR_STATE_START1;                       /*Start BEMF */
                        /* Switch to Back-EMF evaluation per hardware once minimum speed is reached */
#endif
                    }
                }
            }
            else
            {
                if (Emo_Bemf.EnableFrZero == 1)
                {
                    Emo_Bemf.CountStart--;
                }
                else
                {
                    Emo_Bemf.CountStart = 0;    /*Starttime for (Frequenz=0)  =0  */
                }

                Emo_Bemf.StartFrequencySlope = 0;
            }

            i = (sint16)__ssat(Mat_FixMulScale(abs(Emo_Bemf.StartSpeedSlope),
                                               Emo_Ctrl.StartSpeedPwmFactor, 4) + Emo_Ctrl.StartSpeedPwmMinOffset, MAT_FIX_SAT);
            i = __ssat(Mat_FixMulScale(i, Emo_Bemf.DcFactor, 2), 15u);

            if (i > Emo_Ctrl.SpeedPi.PiMax) i = Emo_Ctrl.SpeedPi.PiMax;

            Emo_Ctrl.vout = Emo_Ctrl.Period - i;
            CCU6_LoadShadowRegister_CC60(Emo_Ctrl.vout);
            CCU6_LoadShadowRegister_CC61(Emo_Ctrl.vout);
            CCU6_LoadShadowRegister_CC62(Emo_Ctrl.vout);
            CCU6_EnableST_T12();
        } break;

        case  EMO_MOTOR_STATE_START1: break;
        case  EMO_MOTOR_STATE_START2: break;
        case  EMO_MOTOR_STATE_START3: break;
        case  EMO_MOTOR_STATE_START4: break;
				
        case  EMO_MOTOR_STATE_START5:
        {
            Emo_Ctrl.SpeedPi.IOut = Emo_Ctrl.StartSpeedPwmMin;
            Emo_Ctrl.RefSpeedSlopeMem = ((uint32)Emo_Bemf.StartEndSpeed) << 16;
            i = __ssat(Mat_FixMulScale(Emo_Ctrl.StartSpeedPwmMin, Emo_Bemf.DcFactor, 2), 15u);

            if (i > Emo_Ctrl.SpeedPi.PiMax) i = Emo_Ctrl.SpeedPi.PiMax;

            Emo_Ctrl.vout = Emo_Ctrl.Period - i;
            CCU6_LoadShadowRegister_CC60(Emo_Ctrl.vout);
            CCU6_LoadShadowRegister_CC61(Emo_Ctrl.vout);
            CCU6_LoadShadowRegister_CC62(Emo_Ctrl.vout);
            CCU6_EnableST_T12();
        } break;

        case  EMO_MOTOR_STATE_START6:
        {
            if (Emo_Ctrl.RefSpeed >= 0)
            {
                if (Emo_Ctrl.RefSpeedSlope > 0)
                {
                    Emo_Ctrl.DirectionIndex = 8;   /* config Block Commutation for positive speeds */
                }
            }
            else
            {
                if (Emo_Ctrl.RefSpeedSlope < 0)
                {
                    Emo_Ctrl.DirectionIndex = 0;    /* config Block Commutation for negative speeds */
                }
            }

            Emo_Ctrl.RefSpeedSlope = Mat_Ramp(Emo_Bemf.StartEndSpeed, Emo_Ctrl.SpeedSlewRate, &Emo_Ctrl.RefSpeedSlopeMem);

            /* Speed Regulator: Execute PI algorithm for (imaginary) reference current */
            if (Emo_Ctrl.RefSpeed > 0)
            {
                Emo_Ctrl.SpeedPiOut = Mat_ExePi(&Emo_Ctrl.SpeedPi, Emo_Ctrl.RefSpeedSlope - Emo_Ctrl.ActSpeed);
            }
            else
            {
                Emo_Ctrl.SpeedPiOut = Mat_ExePi(&Emo_Ctrl.SpeedPi, Emo_Ctrl.ActSpeed - Emo_Ctrl.RefSpeedSlope);
            }

            Emo_Ctrl.SpeedPiOut = __ssat(Mat_FixMulScale(Emo_Ctrl.SpeedPiOut, Emo_Bemf.DcFactor, 2), 15u);

            if (Emo_Ctrl.SpeedPiOut > Emo_Ctrl.SpeedPi.PiMax)
            {
                Emo_Ctrl.SpeedPiOut = Emo_Ctrl.SpeedPi.PiMax;
            }

            Emo_Ctrl.vout = Emo_Ctrl.Period - Emo_Ctrl.SpeedPiOut;
            CCU6_LoadShadowRegister_CC60(Emo_Ctrl.vout);
            CCU6_LoadShadowRegister_CC61(Emo_Ctrl.vout);
            CCU6_LoadShadowRegister_CC62(Emo_Ctrl.vout);
            CCU6_EnableST_T12();
            Emo_Ctrl.SpeedPi.IMin = Emo_Ctrl.SpeedPwmBeginMin;
            Emo_Ctrl.SpeedPi.PiMin = Emo_Ctrl.SpeedPwmBeginMin;
        } break;

        case  EMO_MOTOR_STATE_RUN:
        {
            if (Emo_Ctrl.RefSpeed >= 0)
            {
                if (Emo_Ctrl.RefSpeedSlope > 0)
                {
                    Emo_Ctrl.DirectionIndex = 8;   /* config Block Commutation for positive speeds */
                }
            }
            else
            {
                if (Emo_Ctrl.RefSpeedSlope < 0)
                {
                    Emo_Ctrl.DirectionIndex = 0;    /* config Block Commutation for negative speeds */
                }
            }

            Emo_Ctrl.RefSpeedSlope = Mat_Ramp(Emo_Ctrl.RefSpeed, Emo_Ctrl.SpeedSlewRate, &Emo_Ctrl.RefSpeedSlopeMem);

            /* Speed Regulator: Execute PI algorithm for (imaginary) reference current */
            if (Emo_Ctrl.RefSpeed > 0)
            {
                Emo_Ctrl.SpeedPiOut = Mat_ExePi(&Emo_Ctrl.SpeedPi, Emo_Ctrl.RefSpeedSlope - Emo_Ctrl.ActSpeed);
            }
            else
            {
                Emo_Ctrl.SpeedPiOut = Mat_ExePi(&Emo_Ctrl.SpeedPi, Emo_Ctrl.ActSpeed - Emo_Ctrl.RefSpeedSlope);
            }

            Emo_Ctrl.SpeedPiOut = __ssat(Mat_FixMulScale(Emo_Ctrl.SpeedPiOut, Emo_Bemf.DcFactor, 2), 15u);

            if (Emo_Ctrl.SpeedPiOut > Emo_Ctrl.SpeedPi.PiMax)
            {
                Emo_Ctrl.SpeedPiOut = Emo_Ctrl.SpeedPi.PiMax;
            }

            Emo_Ctrl.vout = Emo_Ctrl.Period - Emo_Ctrl.SpeedPiOut;
            CCU6_LoadShadowRegister_CC60(Emo_Ctrl.vout);
            CCU6_LoadShadowRegister_CC61(Emo_Ctrl.vout);
            CCU6_LoadShadowRegister_CC62(Emo_Ctrl.vout);
            CCU6_EnableST_T12();

            if (Emo_Ctrl.SpeedPi.IOut > (Emo_Ctrl.SpeedPwmBeginMin << 1))
            {
                Emo_Ctrl.SpeedPi.IMin = Emo_Ctrl.SpeedPwmMin;
                Emo_Ctrl.SpeedPi.PiMin = Emo_Ctrl.SpeedPwmMin;
            }
        } break;

        case  EMO_MOTOR_STATE_STOP:
        {
            BackEMF_SwitchtoRamp();
            Emo_Ctrl.SpeedLp.Out = 0;
            Emo_Ctrl.SpeedLpdisplay.Out = 0;
            Emo_Ctrl.ActSpeed = Mat_ExeLp_without_min_max(&Emo_Ctrl.SpeedLp, 0);
            Emo_Bemf.timestatus = 0;
        } break;
    };

    Emo_Ctrl.ActSpeeddisplay = Mat_ExeLp_without_min_max(&Emo_Ctrl.SpeedLpdisplay, Emo_Ctrl.ActSpeed);

    /* read DC-Link-Voltage */
    Emo_Bemf.DcLinkVoltage = ADC2_GetChResult(ADC2_RESULT_VS);

    Emo_Bemf.DcFactor = 302623 * 16 / Emo_Bemf.DcLinkVoltage;
} /* End of Emo_HandleT2Overflow */



void GPT1_T2_Handler(void)
{
    /* T2 overflow: */
    /* Set T2 to reload value */
    GPT12E->T2.reg += GPT12E_T2_TICK;
    /* Handle T2 overflow */
    Emo_HandleT2Overflow();
} /* End of GPT1_IRQHandler */





