/*sha256=576D7C75699063A8CB19ECF3D3FAF968C14C9BAD0B072EBB54890AF79E8D9942*/
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

/*------------------------------------------------------------------------------
IFXConfigWizard output file
created on:Mi Jun 13 14:22:21 2018
------------------------------------------------------------------------------*/

#ifndef _CCU6_DEFINES_H
#define _CCU6_DEFINES_H

#ifndef IFXConfigWizard_Version
  #define IFXConfigWizard_Version 1.8.6
#endif

/* XML Version 1.3.1 */
#ifndef CCU6_XML_VERSION
  #define CCU6_XML_VERSION 10301
#endif

#ifndef CCU6_CC60SR
  #define CCU6_CC60SR 0
#endif

#ifndef CCU6_CC61SR
  #define CCU6_CC61SR 0
#endif

#ifndef CCU6_CC62SR
  #define CCU6_CC62SR 0
#endif

#ifndef CCU6_CC63SR
  #define CCU6_CC63SR 0
#endif

#ifndef CCU6_CH0_CMP_DC
  #define CCU6_CH0_CMP_DC 0
#endif

#ifndef CCU6_CH0_CMP_SEL
  #define CCU6_CH0_CMP_SEL 0
#endif

#ifndef CCU6_CH0_CMP_TICK
  #define CCU6_CH0_CMP_TICK 0
#endif

#ifndef CCU6_CH0_CMP_TIME
  #define CCU6_CH0_CMP_TIME 0
#endif

#ifndef CCU6_CH1_CMP_DC
  #define CCU6_CH1_CMP_DC 0
#endif

#ifndef CCU6_CH1_CMP_SEL
  #define CCU6_CH1_CMP_SEL 0
#endif

#ifndef CCU6_CH1_CMP_TICK
  #define CCU6_CH1_CMP_TICK 0
#endif

#ifndef CCU6_CH1_CMP_TIME
  #define CCU6_CH1_CMP_TIME 0
#endif

#ifndef CCU6_CH2_CMP_DC
  #define CCU6_CH2_CMP_DC 0
#endif

#ifndef CCU6_CH2_CMP_SEL
  #define CCU6_CH2_CMP_SEL 0
#endif

#ifndef CCU6_CH2_CMP_TICK
  #define CCU6_CH2_CMP_TICK 0
#endif

#ifndef CCU6_CH2_CMP_TIME
  #define CCU6_CH2_CMP_TIME 0
#endif

#ifndef CCU6_CH3_CMP_DC
  #define CCU6_CH3_CMP_DC 0
#endif

#ifndef CCU6_CH3_CMP_SEL
  #define CCU6_CH3_CMP_SEL 0
#endif

#ifndef CCU6_CH3_CMP_TICK
  #define CCU6_CH3_CMP_TICK 0
#endif

#ifndef CCU6_CH3_CMP_TIME
  #define CCU6_CH3_CMP_TIME 0
#endif

/*CCU6_CMPSTAT: (0<<8)|(0<<9)|(0<<10)|(0<<11)|(0<<12)|(0<<13)|(0<<14)|(0<<15)*/
#ifndef CCU6_CMPSTAT
  #define CCU6_CMPSTAT (0x0u)
#endif

#ifndef CCU6_DEADTIME
  #define CCU6_DEADTIME 0
#endif

/*CCU6_IEN: (0<<14)|0|(0<<1)|(0<<2)|(0<<3)|(0<<4)|(0<<5)|(0<<6)|(0<<7)|(0<<8)|(\
0<<9)|(0<<10)|(0<<12)|(0<<13)|(0<<15)*/
#ifndef CCU6_IEN
  #define CCU6_IEN (0x0u)
#endif

/*CCU6_INP: 0|(0<<2)|(0<<4)|(0<<6)|(0<<8)|(0<<10)|(0<<12)*/
#ifndef CCU6_INP
  #define CCU6_INP (0x0u)
#endif

/*CCU6_MCMCTR: 0|(0<<4)|(0<<8)|(0<<9)|(0<<10)*/
#ifndef CCU6_MCMCTR
  #define CCU6_MCMCTR (0x0u)
#endif

/*CCU6_MODCTR: 0|(0<<1)|(0<<8)|(0<<9)|(0<<2)|(0<<3)|(0<<10)|(0<<11)|(0<<4)|(0<<\
5)|(0<<12)|(0<<13)|(0<<15)|(0<<7)*/
#ifndef CCU6_MODCTR
  #define CCU6_MODCTR (0x0u)
#endif

/*CCU6_NVIC: 0|(0<<1)|(0<<2)|(0<<3)*/
#ifndef CCU6_NVIC
  #define CCU6_NVIC (0x0u)
#endif

/*CCU6_PISEL0: 1|(1<<2)|(2<<4)|(0<<6)|(3<<8)|(0<<10)|(3<<12)|(2<<14)*/
#ifndef CCU6_PISEL0
  #define CCU6_PISEL0 (0xB325u)
#endif

/*CCU6_PISEL2: 2|(0<<2)|(0<<4)*/
#ifndef CCU6_PISEL2
  #define CCU6_PISEL2 (0x2u)
#endif

/*CCU6_PSLR: 0|(0<<1)|(0<<2)|(0<<3)|(0<<4)|(0<<5)|(0<<7)*/
#ifndef CCU6_PSLR
  #define CCU6_PSLR (0x0u)
#endif

/*CCU6_T12DTC: 0|(0<<8)|(0<<9)|(0<<10)*/
#ifndef CCU6_T12DTC
  #define CCU6_T12DTC (0x0u)
#endif

/*CCU6_T12MSEL: 0|(0<<4)|(0<<8)|(0<<12)|(0<<15)*/
#ifndef CCU6_T12MSEL
  #define CCU6_T12MSEL (0x0u)
#endif

/*CCU6_T12PERIOD: 40*/
#ifndef CCU6_T12PERIOD
  #define CCU6_T12PERIOD (0x28u)
#endif

/*CCU6_T12PR: 39*/
#ifndef CCU6_T12PR
  #define CCU6_T12PR (0x27u)
#endif

#ifndef CCU6_T12_CLK
  #define CCU6_T12_CLK 40
#endif

#ifndef CCU6_T12_FREQ
  #define CCU6_T12_FREQ 20000
#endif

#ifndef CCU6_T12_MAX_PERIOD
  #define CCU6_T12_MAX_PERIOD 1638.38
#endif

#ifndef CCU6_T12_PERIOD_SEL
  #define CCU6_T12_PERIOD_SEL 1
#endif

/*CCU6_T12_TICK: 1*/
#ifndef CCU6_T12_TICK
  #define CCU6_T12_TICK (0x1u)
#endif

#ifndef CCU6_T12_TIME
  #define CCU6_T12_TIME 1
#endif

/*CCU6_T13PERIOD: 40*/
#ifndef CCU6_T13PERIOD
  #define CCU6_T13PERIOD (0x28u)
#endif

/*CCU6_T13PR: 39*/
#ifndef CCU6_T13PR
  #define CCU6_T13PR (0x27u)
#endif

#ifndef CCU6_T13_CLK
  #define CCU6_T13_CLK 40
#endif

#ifndef CCU6_T13_FREQ
  #define CCU6_T13_FREQ 20000
#endif

#ifndef CCU6_T13_MAX_PERIOD
  #define CCU6_T13_MAX_PERIOD 1638.38
#endif

#ifndef CCU6_T13_PERIOD_SEL
  #define CCU6_T13_PERIOD_SEL 1
#endif

/*CCU6_T13_TICK: 1*/
#ifndef CCU6_T13_TICK
  #define CCU6_T13_TICK (0x1u)
#endif

#ifndef CCU6_T13_TIME
  #define CCU6_T13_TIME 1
#endif

/*CCU6_TCTR0: 0|(0<<3)|(0<<7)|(0<<8)|(0<<11)*/
#ifndef CCU6_TCTR0
  #define CCU6_TCTR0 (0x0u)
#endif

/*CCU6_TCTR2: 0|(0<<8)|(0<<1)|(0<<2)|(0<<5)|(0<<10)*/
#ifndef CCU6_TCTR2
  #define CCU6_TCTR2 (0x0u)
#endif

#ifndef CCU6_TEMP_CC60_OUT
  #define CCU6_TEMP_CC60_OUT 0
#endif

#ifndef CCU6_TEMP_CC61_OUT
  #define CCU6_TEMP_CC61_OUT 0
#endif

#ifndef CCU6_TEMP_CC62_OUT
  #define CCU6_TEMP_CC62_OUT 0
#endif

#ifndef CCU6_TEMP_COUT60_OUT
  #define CCU6_TEMP_COUT60_OUT 0
#endif

#ifndef CCU6_TEMP_COUT61_OUT
  #define CCU6_TEMP_COUT61_OUT 0
#endif

#ifndef CCU6_TEMP_COUT62_OUT
  #define CCU6_TEMP_COUT62_OUT 0
#endif

#ifndef CCU6_TEMP_COUT63_OUT
  #define CCU6_TEMP_COUT63_OUT 0
#endif

/*CCU6_TRPCTR: 0|(0<<2)|(0<<8)|(0<<9)|(0<<10)|(0<<11)|(0<<12)|(0<<13)|(0<<14)|(\
0<<15)*/
#ifndef CCU6_TRPCTR
  #define CCU6_TRPCTR (0x0u)
#endif


#endif
