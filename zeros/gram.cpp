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
#include "gram.h"
#include "doubledouble.h"

using namespace std;

double Gram(longlong n, double a)
// Using a as an initial approximation the nth Gram point is calculated and assigned to b.
{
  /*doubledouble t2,t1 = a/(2.0*doubledouble::Pi);
  doubledouble d = double(n) + 1.125;
  double diff = 1e10;
  double diff2;
  while (true) {
cout << "t1=" << t1;
    t2 = d; t2 /= (log(t1)-1.0);
    diff2 = double(fabs(t1-t2));
cout << ", t2=" << t2 << ", diff2=" << diff2 << endl;
    if (diff2 >= diff) return double(t2*2.0*doubledouble::Pi);
    t1 = t2; diff = diff2;
  }*/
  double t2,t1 = a*TWO_PI_INV;
  double d = double(n) + 0.125;
  do {
    t2 = (t1+d)/log(t1);
    if (fabs(t1-t2) < t2*1e-13) return t2*TWO_PI;
    t1 = (t2+d)/log(t2);
  } while (fabs(t1-t2) >= t1*1e-13);
  return t1*TWO_PI;
}

doubledouble Gram(const doubledouble& n, const doubledouble& a)
// Using a as an initial approximation the nth Gram point is calculated and assigned to b.
// g_n * (ln(g_n/(2*pi))-1) < 2*pi * (n + 1/8)
// Then h_n < (h_n+n+1/8)/ln(h_n) where h_n = g_n/(2*pi)
{
  doubledouble t2,t1 = a/(2.0*doubledouble::Pi);
  doubledouble d = n + 0.125;
  do {
    t2 = (t1+d)/log(t1);
    if ((double(fabs(t1-t2)/t2)) < 1e-13) {
      return t2*(2.0*doubledouble::Pi);
    }
    t1 = (t2+d)/log(t2);
  } while (double(fabs(t1-t2)/t1) >= 1e-13);
  return t1*(2.0*doubledouble::Pi);
}

startType getStartN(double t)
{
  startType n2,n1 = startType(t*0.83675);
  double g2,g1 = Gram(n1, TWO_PI*n1/log(double(n1)));
  if (g1 > t) {
    do {
      n2 = n1; g2 = g1;
      n1 >>= 1;
      g1 = Gram(n1, TWO_PI*n1/log(double(n1)));
    } while (g1 > t);
  } else {
    n2 = n1; g2 = g1;
    while (true) {
      n2 <<= 1;
      g2 = Gram(n2, TWO_PI*n2/log(double(n2)));
      if (g2 > t) break;
      g1 = g2; n1 = n2;
    }
  }
  while (n2-n1 > 100) {
    startType n = (n1+n2) >> 1;
    double g = Gram(n, TWO_PI*n/log(double(n)));
    if (g > t) n2 = n;
    else n1 = n;
  }
  return n1;
}

doubledouble getStartN(const doubledouble& t)
{
  const doubledouble TWO_PI = 2.0*doubledouble::Pi;
  doubledouble n2,n1 = t*0.83675;
  doubledouble g2,g1 = Gram(n1, TWO_PI*n1/log(n1));
  if (g1 > t) {
    do {
      n2 = n1; g2 = g1;
      n1 /= 2.0;
      g1 = Gram(n1, TWO_PI*n1/log(n1));
    } while (g1 > t);
  } else {
    n2 = n1; g2 = g1;
    while (true) {
      n2 *= 2.0;
      g2 = Gram(n2, TWO_PI*n2/log(n2));
      if (g2 > t) break;
      g1 = g2; n1 = n2;
    }
  }
  while (double(n2-n1) > 100.0) {
    doubledouble n = (n1+n2)/2;
    doubledouble g = Gram(n, TWO_PI*n/log(n));
    if (g > t) n2 = n;
    else n1 = n;
  }
  return n1;
}
