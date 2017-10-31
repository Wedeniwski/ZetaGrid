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


#ifndef __eval_zeta_h__
#define __eval_zeta_h__

#include <ctime>
#include "constants.h"
#include "output.h"
#include "gram.h"
#include "theta.h"

using namespace std;


struct lnSqrt {
  double ln;
  double sqrtinv;
};

const int DCI_SIZE = 39;

class EvalZeta {
private:
  int sleepCounter;
  static int fastSumZMethod;
  static double* cosValue;
  Output* output;
  lnSqrt* sqrtinvLn;
  doubledouble* lnDD;
  doubledouble* sqrtinvDD;
  static doubledouble TWO_PI_DD;
  static doubledouble TWO_PI_INV_DD;
  static doubledouble DC0[DCI_SIZE];
  static doubledouble DC1[DCI_SIZE];
  static doubledouble DC2[DCI_SIZE];
  static doubledouble DC3[DCI_SIZE];
  static doubledouble DDC0_LOW[DCI_SIZE];
  static doubledouble DDC1_LOW[DCI_SIZE];
  static doubledouble DDC2_LOW[DCI_SIZE];
  static doubledouble DDC3_LOW[DCI_SIZE];
  static doubledouble DDC0_HIGH[DCI_SIZE];
  static doubledouble DDC1_HIGH[DCI_SIZE];
  static doubledouble DDC2_HIGH[DCI_SIZE];
  static doubledouble DDC3_HIGH[DCI_SIZE];
  static double DC0_LOW[DCI_SIZE];
  static double DC1_LOW[DCI_SIZE];
  static double DC2_LOW[DCI_SIZE];
  static double DC3_LOW[DCI_SIZE];
  static double DC0_HIGH[DCI_SIZE];
  static double DC1_HIGH[DCI_SIZE];
  static double DC2_HIGH[DCI_SIZE];
  static double DC3_HIGH[DCI_SIZE];

  void errorComputation(const doubledouble& tau, const doubledouble& rho, int m, doubledouble& errorLow, doubledouble& errorHigh);

public:
  void sumZ(double t, int m, double thetaMod, double* zLowHigh);
  void sumDZ(double t, int m, double thetaMod, double* zLowHigh);
  void sumDZSleep(double t, int m, double thetaMod, double* zLowHigh);
  void sumDD(double t, int m, const doubledouble& theta, doubledouble& zLow, doubledouble& zHigh);
  void sumDD(const doubledouble& t, int m, const doubledouble& theta, doubledouble& zLow, doubledouble& zHigh);

  static int getFastSumZMethod();
  static void setFastSumZMethod(int method);

public:
  int ndzevalu,nzevalu;    // number of Z-evaluations.
  clock_t dztime,ztime;
  static EvalZeta* running;

  EvalZeta(Output* output);
  ~EvalZeta();

  bool init(startType LASTN, int NRANGE);
  void destroy();
  void evalZ(double t, double& zLow, double& zHigh);
  double evalZ(double t);
  void evalDZ(double t, double& zLow, double& zHigh);
  double evalDZ(double t);
  void evalDZ(const doubledouble& t, double& zLow, double& zHigh);
  double evalZ(double tau1, double& tau2);
};


extern int sleepMode;

inline int EvalZeta::getFastSumZMethod()
{
  return fastSumZMethod;
}

inline double EvalZeta::evalZ(double t)
// The Riemann-Siegel formula (on sigma=1/2)
{
  double zLow,zHigh;
  evalZ(t, zLow, zHigh);
  return (zLow < 0 && zHigh < 0 || zLow > 0 && zHigh > 0)? (zLow+zHigh)/2 : 0;
}

inline double EvalZeta::evalDZ(double t)
// The Riemann-Siegel formula (on sigma=1/2)
{
  double zLow,zHigh;
  evalDZ(t, zLow, zHigh);
  return (zLow < 0 && zHigh < 0 || zLow > 0 && zHigh > 0)? (zLow+zHigh)/2 : 0;
}

#endif
