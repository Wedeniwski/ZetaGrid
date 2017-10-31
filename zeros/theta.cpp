/*--
  This file is a part of ZetaGrid, a program and
  library for seperation of zeros of the Riemann zeta function.

  Copyright (c) 2001-2004 Sebastian Wedeniwski.  All rights reserved.

  Use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:

  1. The source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.

  2. The origin of this software must not be misrepresented; you must 
     not claim that you wrote the original software.  If you plan to
     use this software in a product, please contact the author.

  3. Altered source versions must be plainly marked as such, and must
     not be misrepresented as being the original software. The author
     must be informed about these changes.

  4. The name of the author may not be used to endorse or promote 
     products derived from this software without specific prior written 
     permission.

  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS
  OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

  This program is based on the work of:
     R. P. Brent
     R. S. Lehman
     J. van de Lune
     H. J. J. te Riele
     J. B. Rosser
     L. Schoenfeld
     S. Wedeniwski
     D. T. Winter
     Y. M. Yohe
--*/


#include <cmath>

#include "constants.h"
#include "theta.h"

const double DCNST1 = 1.0/48.0;
const double DCNST2 = 7.0/5760.0;
const double DCNST3 = 31.0/80640.0;
const double DCNST4 = 127.0/430080.0;
const double DCNST5 = 511.0/1216512.0;

const double DCNST1_LOW = low(DCNST1);
const double DCNST1_HIGH = high(DCNST1);
const double DCNST2_LOW = low(DCNST2);
const double DCNST2_HIGH = high(DCNST2);
const double DCNST3_LOW = low(DCNST3);
const double DCNST3_HIGH = high(DCNST3);
const double DCNST4_LOW = low(DCNST4);
const double DCNST4_HIGH = high(DCNST4);
const double DCNST5_HIGH = high(DCNST5);


/*
mp_real getMp(double a, double b)
{
  mpprecision mpp(40);
  mp_real mpa(a, mpp);
  mp_real mpb(b, mpp);
  return a /= b;
}

mp_real getTwoPiInv()
{
  mpprecision mpp(40);
  mp_real mpa(1, mpp);
  mpa /= mp::mppic;
  return mpa *= mp_real(0.5, mpp);
}

mp_real getPiSl8()
{
  mp_real mpa = mp::mppic;
  return mpa *= mp_real(0.125, mpprecision(40));
}

void theta(double t, mp_real& result)
{
  mpprecision mpp(40);
  mp_real DCNST1_MP = getMp(1.0, 48.0);
  mp_real DCNST2_MP = getMp(7.0, 5760.0);     
  mp_real DCNST3_MP = getMp(31.0, 80640.0);   
  mp_real DCNST4_MP = getMp(127.0, 430080.0); 
  mp_real DCNST5_MP = getMp(511.0, 1216512.0);
  mp_real TWO_PI_INV_MP = getTwoPiInv();      
  mp_real PI_SL8_MP = getPiSl8();             
  mp_real mpt(t, mpp);
  mp_real inv(1, mpp);
  inv /= mpt;
  mp_real inv2 = inv;
  inv2 *= inv;
  result = mpt*mp_real(0.5, mpp)*(log(mpt*TWO_PI_INV_MP)-mp_real(1, mpp)) - PI_SL8_MP
           + ((((DCNST5_MP*inv2-DCNST4_MP)*inv2+DCNST3_MP)*inv2-DCNST2_MP)*inv2+DCNST1_MP)*inv;
}
*/

void theta(double t, double& thetaLow, double& thetaHigh)
{
  double invLow,invHigh;
  lowHigh(1.0/t, invLow, invHigh);
  double inv2Low = low(invLow*invLow);
  double inv2High = high(invHigh*invHigh);
  thetaLow = t*0.5*(low(log(t*TWO_PI_INV_LOW))-1.0) - PI_SL8_HIGH
             + (((DCNST4_LOW*inv2Low+DCNST3_LOW)*inv2Low+DCNST2_LOW)*inv2Low+DCNST1_LOW)*invLow;
  thetaHigh = t*0.5*(high(log(t*TWO_PI_INV_HIGH))-1.0) - PI_SL8_LOW
              + ((((DCNST5_HIGH*inv2High+DCNST4_HIGH)*inv2High+DCNST3_HIGH)*inv2High+DCNST2_HIGH)*inv2High+DCNST1_HIGH)*invHigh;
}

double theta(double dt)
{
  const double dtinv = 1.0/dt;
  const double dtinv2 = dtinv*dtinv;
  return dt*0.5*(log(dt*TWO_PI_INV)-1.0) - PI_SL8 + ((DCNST3*dtinv2+DCNST2)*dtinv2+DCNST1)*dtinv;
}

const doubledouble DCNST1_DD = doubledouble(1.0)/doubledouble(48.0);
const doubledouble DCNST2_DD = doubledouble(7.0)/doubledouble(5760.0);
const doubledouble DCNST3_DD = doubledouble(31.0)/doubledouble(80640.0);
const doubledouble DCNST4_DD = doubledouble(127.0)/doubledouble(430080.0);
const doubledouble DCNST5_DD = doubledouble(511.0)/doubledouble(1216512.0);

static doubledouble TWO_PI_INV_DD = 1.0;
static doubledouble PI_SL8_DD = 1.0;

//#include "qd.h"

void theta(double t, doubledouble& result)
{
  doubledouble inv = 1.0;
  if (TWO_PI_INV_DD == inv) {
    TWO_PI_INV_DD = (recip(doubledouble::Pi))/2;
    PI_SL8_DD = doubledouble::Pi/8;
  }
  inv /= t;
  doubledouble inv2 = sqr(inv);
  doubledouble d(t);
  doubledouble d2 = d /= 2;
  d /= doubledouble::Pi;
  result = log(d);
  result -= 1.0; result *= d2; result -= PI_SL8_DD;
  //d = DCNST5_DD; d *= inv2; d += DCNST4_DD;
  d = DCNST4_DD; d *= inv2; d += DCNST3_DD; d *= inv2; d += DCNST2_DD;
  d *= inv2; d += DCNST1_DD; d *= inv;
  result += d;
}

void theta(doubledouble t, doubledouble& result)
{
  doubledouble inv = 1.0;
  if (TWO_PI_INV_DD == inv) {
    TWO_PI_INV_DD = (recip(doubledouble::Pi))/2;
    PI_SL8_DD = doubledouble::Pi/8;
  }
  inv /= t;
  doubledouble inv2 = sqr(inv);
  doubledouble d = t /= 2;
  t /= doubledouble::Pi;
  result = log(t);
  result -= 1.0; result *= d; result -= PI_SL8_DD;
  //t = DCNST5_DD; t *= inv2; t += DCNST4_DD;
  t = DCNST4_DD; t *= inv2; t += DCNST3_DD; t *= inv2; t += DCNST2_DD;
  t *= inv2; t += DCNST1_DD; t *= inv;
  result += t;
}
