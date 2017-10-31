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


#include <algorithm>
#include <cmath>
#include <cstdlib>
#include <cstdio>
#include <ctime>
#include <list>
#include <fstream>

#ifdef _WIN32
#include <process.h>
#endif

#include "lowhigh.h"
#include "zeta_zeros.h"
#include "gram.h"
#include "theta.h"
#include "statistic.h"
#include "check.h"


using namespace std;


static bool fileExists(const char* name)
{
  FILE* tmp = fopen(name, "rb");
  bool exists = (tmp != NULL);
  if (tmp != NULL) {
    fclose(tmp);
  }
  return exists;
}


#ifdef _MSC_VER
#pragma data_seg("Shared")

int terminateCalc = 0;
bool exitCalc = false;

#pragma data_seg()

// /link SECTION:Shared,rws

extern "C" __declspec(dllexport) void exitCalculation2(int terminate)
{
  terminateCalc = terminate;
  if (fileExists("zeta_zeros.pid")) {
    exitCalc = true;
  }
}

extern "C" __declspec(dllexport) void exitCalculation()
{
  exitCalculation2(1);
}

#else

int terminateCalc = 0;
bool exitCalc = false;

extern "C" void exitCalculation2(int terminate)
{
  terminateCalc = terminate;
  exitCalc = true;
}

extern "C" void exitCalculation()
{
  exitCalculation2(1);
}
#endif
  
#ifdef _DEFINE_SLEEP_
#include <unistd.h>
inline void _sleep(int s)
{
  sleep(s);
}
#endif

bool isExit()
{
  return (exitCalc || terminateCalc != 0);
}


int ZetaZeros::activeThreads = 0;
static int showThreadStatus = 0;
static int threadId = 0;
static bool changeThreadId = false;


void ZetaZeros::iterateZero(doubledouble& a, doubledouble& b, double& za, double& zb, double delta)
// a < b, sign(za) != sign(zb)
{
  if (delta <= 0.001) {
    double zt,ztx;
    while (double(b-a) > delta) {
      doubledouble t = a;
      t += b; t /= 2;
      if (t == a || t == b) {
        break;
      }
      evalZeta.evalDZ(t, zt, ztx);
      if (zt > 0.0 && ztx < 0.0 || zt < 0.0 && ztx > 0.0) {
        break;
      }
      if (za > 0.0 && zt > 0.0 || za < 0.0 && zt < 0.0) {
        a = t; za = zt;
      } else if (zb > 0.0 && zt > 0.0 || zb < 0.0 && zt < 0.0) {
        b = t; zb = zt;
      } else {
        break;
      }
    }
  } else {
    while (double(b-a) > delta) {
      doubledouble t = a;
      t += b; t /= 2;
      if (t == a || t == b) {
        break;
      }
      double zt = evalZeta.evalZ(t);
      if (zt == 0.0) {
        zt = evalZeta.evalDZ(t);
      }
      if (za > 0.0 && zt > 0.0 || za < 0.0 && zt < 0.0) {
        a = t; za = zt;
      } else if (zb > 0.0 && zt > 0.0 || zb < 0.0 && zt < 0.0) {
        b = t; zb = zt;
      } else {
        break;
      }
    }
  }
}

void ZetaZeros::checkClosePairOfZeros(double a, double b, double c, double za, double zb, double zc)
// a < b < c
// za and zc have the same sign but the sign of zb is different
{
  if (::fabs(zb) < 1.0e-4) {
    doubledouble a2 = a;
    doubledouble b2 = b;
    doubledouble d2 = b;
    doubledouble c2 = c;
    if (a > c) {
      a2 = c; c2 = a;
    }
    // a2 is not equal to a
    za = evalZeta.evalZ(a2);
    if (za == 0.0) za = evalZeta.evalDZ(a2);
    // b2 is not equal to b
    zb = evalZeta.evalZ(b2);
    if (zb == 0.0) zb = evalZeta.evalDZ(b2);
    double zd = zb;
    // c2 is not equal to c
    zc = evalZeta.evalZ(c2);
    if (zc == 0.0) zc = evalZeta.evalDZ(c2);
    if (!(za < 0.0 && zb > 0.0 && zc < 0.0 || za > 0.0 && zb < 0.0 && zc > 0.0)) {
      a2 -= 0.001; c2 += 0.001;
      za = evalZeta.evalZ(a2);
      if (za == 0.0) za = evalZeta.evalDZ(a2);
      zc = evalZeta.evalZ(c2);
      if (zc == 0.0) zc = evalZeta.evalDZ(c2);
    }
    iterateZero(a2, b2, za, zb, 0.01);
    iterateZero(d2, c2, zd, zc, 0.01);
    if (double(c2-a2) < 0.04) {
      iterateZero(a2, b2, za, zb, 0.001);
      iterateZero(d2, c2, zd, zc, 0.001);
      if (double(c2-a2) < 0.004) {
        iterateZero(a2, b2, za, zb, 0.0005);
        iterateZero(d2, c2, zd, zc, 0.0005);
        iterateZero(a2, b2, za, zb, 0.0001);
        iterateZero(d2, c2, zd, zc, 0.0001);
        if (double(c2-a2) < 0.0004) {
          iterateZero(a2, b2, za, zb, 0.00001);
          iterateZero(d2, c2, zd, zc, 0.00001);
          iterateZero(a2, b2, za, zb, 0.000001);
          iterateZero(d2, c2, zd, zc, 0.000001);
          if (double(c2-a2) < 0.000204) {
            iterateZero(a2, b2, za, zb, 0.0000001);
            iterateZero(d2, c2, zd, zc, 0.0000001);
          }
          if (double(c2-a2) < 0.000201) {
            iterateZero(a2, b2, za, zb, 0.00000001);
            iterateZero(d2, c2, zd, zc, 0.00000001);
          }
          if (double(c2-a2) < 0.0002) {
            int prec = ((output).logOut)->precision();
            cout.precision(25);
            ((output).logOut)->precision(25);
            LOGLN(output, "... Close pair of zeros between " << a2 << " and " << c2 << " (" << fabs(b2-a2) << ',' << fabs(c2-b2) << ')');
            cout.precision(prec);
            ((output).logOut)->precision(prec);
            logHappened = true;
          }
        }
      }
    }
  }
}

bool ZetaZeros::search2b(double a, double b, double za, double zb, double& t, double& zt)
{
  // We try to find two sign changes of Z(t) in the interval (a, b).
  // search2b is called only if a < b and Z(a)*Z(b) > 0.
  LOGLN(output, "Call search2b between " << a << " and " << b << '.');
  double h = (b-a)*0.5;
  const double m = (a+b)*0.5;
  // Note that in this routine the argument of Z(t) zigzags
  // according to a pattern such as ... 9 7 5 3 1 0 2 4 6 8 10...
  int ncycles = 6;
  do {
    h *= 0.5;
    for (double step = h; ; step += 2*h) {
      t = m-step;
      if (t < a) break;
      zt = evalZeta.evalZ(t);
      if (zt == 0) zt = evalZeta.evalDZ(t);
      if (za < 0 && zt > 0 || za > 0 && zt < 0) {
        checkClosePairOfZeros(a, t, b, za, zt, zb);
        return true; // Two zeros found.
      }
      t = m+step;
      if (t > b) break;
      zt = evalZeta.evalZ(t);
      if (zt == 0) zt = evalZeta.evalDZ(t);
      if (za < 0 && zt > 0 || za > 0 && zt < 0) {
        checkClosePairOfZeros(a, t, b, za, zt, zb);
        return true; // Two zeros found.
      }
    }
    if (isExit()) return false;
  } while (--ncycles);
  return false;
}

bool ZetaZeros::search2c(double a, double b, double za, double zb, double& t, double& zt)
{
  const int CYCLES = 3;
  const int sz = (1 << (CYCLES+1)) + 1;
  // We search for two zeros in [a, b]. Search direction: a --> b.
  // In this routine we always have Z(a)*Z(b) > 0.
  LOGLN(output, "Call search2c between " << a << " and " << b << '.');
  double h = (b-a)*0.5;
  char ncycles = CYCLES;
  double c[sz+1],zc[sz+1];
  do {
    h *= 0.5;
    short i = 1;
    char nbreak = 4;  // nbreak=4 since n=17913470332
    c[0] = a; zc[0] = za;
    for (t = a+h; t < b && i < sz; t += 2*h, ++i) {
      zt = evalZeta.evalZ(t);
      if (zt == 0) {
        zt = evalZeta.evalDZ(t);
        if (--nbreak == 0) break;
      }
      if (za < 0 && zt > 0 || za > 0 && zt < 0) {
        checkClosePairOfZeros(c[i-1], t, b, zc[i-1], zt, zb);
        return true; // Two zeros found.
      }
      c[i] = t; zc[i] = zt;
      if (isExit()) return false;
    }
    if (nbreak == 3 && ncycles == 1) break;
    if (i > 1) {
      c[i] = b; zc[i] = zb;
      ++i;
      short k = 0;
      short j = 0;
      double d = zc[0];
      while (++j < i) {
        const double d2 = fabs(zc[j]);
        if (d2 < d) {
          k = j; d = d2;
          if (j+1 < i && fabs(zc[j-1]) > d2 && fabs(zc[j+1]) > d2) break; // since n=8136521010
        }
      }
      if (k == 0) {
        b = c[1]; zb = zc[1];
      } else if (k+1 < i) {
        a = c[k-1]; za = zc[k-1];
        b = c[k+1]; zb = zc[k+1];
      } else {
        a = c[i-2]; za = zc[i-2];
      }
      h = (b-a)*0.5;
      for (j = CYCLES; j >= ncycles; --j) h *= 0.5;
    }
  } while (--ncycles);
  return false;
}

bool ZetaZeros::searchND(int n, double a, double b, double za, double zb, double h, double* t, double* z)
{
  // We search for n zeros in [a, b]. Search direction: a --> b.
  // In this routine we always have Z(a)*Z(b) > 0.
  LOGLN(output, "Call searchND(" << n << ") between " << a << " and " << b << ", h=" << h << '.');
  if (a < b) {
    double c,z0 = za;
    int nfound = 0;
    --n;
    doubledouble dc(a);
    dc += h; h *= 2;
    while (true) {
      while (true) {
        c = double(dc);
        if (c != a) break;
        doubledouble d = dc;
        dc += h;
        if (d == dc) return false;    // endless loop possible
      }
      if (c >= b) break;
      double zc = evalZeta.evalZ(c);
      if (zc == 0) zc = evalZeta.evalDZ(c);
      if (zc < 0 && z0 > 0 || zc > 0 && z0 < 0) {
        // One zeros found.
        z0 = zc;
        t[nfound] = c; z[nfound] = zc;
        if (++nfound == n) {
          if (n == 1) {
            checkClosePairOfZeros(a, t[0], b, za, z[0], zb);
          } else if (n > 1) {
            checkClosePairOfZeros(a, t[0], t[1], za, z[0], z[1]);
            for (int i = 2; i < n; ++i) {
              checkClosePairOfZeros(t[i-2], t[i-1], t[i], z[i-2], z[i-1], z[i]);
            }
            checkClosePairOfZeros(t[n-2], t[n-1], b, z[n-2], z[n-1], zb);
          }
          return true;
        }
      }
      if (isExit()) return false;
      dc += h; a = c;
    }
  }
  return false;
}

bool ZetaZeros::search2d(double a, double b, double za, double zb, double& t, double& zt)
{
  if (search2c(a, b, za, zb, t, zt)) return true;
  const int CYCLES = 8;   // since n=23279559198
  const int sz = (1 << (CYCLES+2)) + 1;
  // We search for two zeros in [a, b]. Search direction: a --> b.
  // In this routine we always have Z(a)*Z(b) > 0.
  LOGLN(output, "Call search2d between " << a << " and " << b << '.');

  double a2,b2,za2,zb2;
  a2 = 0;
  double h = (b-a)*0.25;
  char ncycles = CYCLES;
  double c[sz+1],zc[sz+1];
  do {
    h *= 0.5;
    short i = 1;
    c[0] = a; zc[0] = za;
    for (t = a+h; t < b && i < sz; t += 2*h, ++i) {
      zt = evalZeta.evalZ(t);
      if (zt == 0) zt = evalZeta.evalDZ(t);
      if (za < 0 && zt > 0 || za > 0 && zt < 0) {
        checkClosePairOfZeros(c[i-1], t, b, zc[i-1], zt, zb);
        return true; // Two zeros found.
      }
      c[i] = t; zc[i] = zt;
      if (t == c[i-1]) {  // h is too small
        if (fabs(zt) >= 1.0) return false;
        double t2[1],zt2[1];
        if (searchND(2, a, b, za, zb, h, t2, zt2)) {
          t = t2[0]; zt = zt2[0];
          return true;
        }
        if (a2 != 0) {
          ncycles = i = 1;
          break;
        }
        return false;
      }
      if (isExit()) return false;
    }
    if (i > 1) {
      c[i] = b; zc[i] = zb;
      ++i;
      short k = 0;
      short j = 0;
      double d1 = fabs(zc[0]);
      double d3 = d1;
      while (++j < i) {
        const double d2 = fabs(zc[j]);
        if (d2 < d1) {
          k = j; d1 = d2;
          if (j+1 < i && fabs(zc[j-1]) > d2 && fabs(zc[j+1]) > d2) break; // since n=8136521010
        } else if (d2 > d3) d3 = d2;
      }
      if (ncycles > 1) {
        if (ncycles < 8) {  // since n=40381610677
          if (k == 0) {
            for (j = 2; j < i; ++j) { // since n=10931594655
              if (fabs(zc[j]) < fabs(zc[j-1])) break;
            }
            if (j >= i) {
              if (ncycles == CYCLES && d3 > fabs(zb)) { // since n=8150548697
                a2 = c[i-2]; za2 = zc[i-2];
                b2 = b; zb2 = zb;
              }
              b = c[1]; zb = zc[1];
            }
          } else if (k+1 < i) {
            a = c[k-1]; za = zc[k-1];
            b = c[k+1]; zb = zc[k+1];
          } else {
            for (j = 1; j < i; ++j) {
              if (fabs(zc[j]) > fabs(zc[j-1])) break;
            }
            if (j >= i) {
              if (ncycles == CYCLES && d3 > fabs(za)) {
                a2 = a; za2 = za;
                b2 = c[1]; zb2 = zc[1];
              }
              if (ncycles >= CYCLES-1) {
                a = c[i-3]; za = zc[i-3]; // since n=20601789292
              } else {
                a = c[i-2]; za = zc[i-2];
              }
            }
          }
        }
        h = (b-a)*0.25;
        for (j = CYCLES; j >= ncycles; --j) h *= 0.5;
      }
    }
    if (ncycles == 1 && a2 != 0) {
      ncycles = 2;
      a = a2; za = za2; b = b2; zb = zb2; a2 = 0;
      h = (b-a)*0.5;
      for (int j = CYCLES; j >= ncycles; --j) h *= 0.5;
    }
    if (ncycles <= 4 && fabs(zb-za) < 0.1 && fabs(zb) > 2.0 && fabs(za) > 2.0) return false;
  } while (--ncycles);
  return false;
}

bool ZetaZeros::parabola(int index, double* at, double* az, int* next, bool* accept, int& indout, bool& convex)
{
  // This routine is essentially due to J. van de Lune, H.J.J te Riele, D.T. Winter.
  // parabola is called only in search2a so that it is applied only to intervals [a, b] with za*zb > 0.
  // Define: n=next[index], t1=at[index], t3=at[index+n], n2 = n/2.
  // parabola tries to find two zeros in the interval [t1, t3] by means of a close look at the graph
  // of abs(Z(t)) on this interval. This routine saves many evaluations of Z(t) in case the graph of
  // abs(Z(t)) is concave. If for some k the interval [at[k], at[k+next[k]] is judged as containing
  // no zeros we set accept[k]=true, else accept[k]=false.
  int n = next[index];
  int n2 = n/2;
  if (n2 < 1) {
    indout = index+1;
    return false;
  }
  next[index] = next[index+n2] = n2;
  accept[index] = accept[index+n2] = true;
  double t1 = at[index];
  double t3 = at[index+n];
  double z1 = az[index];
  double z3 = az[index+n];
  double t2 = (t1+t3)*0.5;
  at[index+n2] = t2;
  double z2 = evalZeta.evalZ(t2);
  if (z2 == 0) {
    z2 = evalZeta.evalDZ(t2);
    if (z2 == 0) {
      LOGLN(output, "We are in parabola between t1=" << t1 << " and t3=" << t3 << ".\nWe found a doubtfull zt at t2=(t1+t3)/2.");
      if (t1 > t3) swap(t1, t3);
      if (search2b(t1, t3, z1, z3, t2, z2)) {
        at[index+n2] = t2; az[index+n2] = z2;
        return true;
      }
      indout = 513; convex = false;
      return false;
    }
  }
  az[index+n2] = z2;
  if (z2 < 0 && z1 > 0 || z2 > 0 && z1 < 0) {
    checkClosePairOfZeros(t1, t2, t3, z1, z2, z3);
    return true; // Two zeros found.
  }
  // In the following lines of this routine we inspect the graph of abs(Z(t)) by means of a parabolic approximation.
  z1 = fabs(z1); z2 = fabs(z2); z3 = fabs(z3);
  if (z2 < min(z1, z3)) {
    convex = true;
    // Although convex=true now, we do not call search2b yet!
    // We expect parabola to find the missing two.
    if (z1 >= z3) {
      accept[index+n2] = false;
      indout = index+n2;
    } else {
      accept[index] = false;
      indout = index;
    }
    return false;
  }
  if (z2 >= (z1+z3)*0.5) {
    indout = index+n;
    return false;
  }
  double x2 = t2-t1;
  double x3 = t3-t1;
  double a = (x3*(z2-z1)-x2*(z3-z1))/x2/x3/(t2-t3);
  double b = (z2-z1)/x2-a*x2;
  double rmu = -b*0.5/a;
  double xmidp = x3*0.5;
  double half = fabs(xmidp);
  double xdist = fabs(rmu-xmidp);
  if (xdist > half) {
    indout = index+n;
  } else if (fabs(rmu) > fabs(x2)) {
    accept[index+n2] = false;
    indout = index+n2;
  } else {
    accept[index] = false;
    indout = index;
  }
  return false;
}

bool ZetaZeros::search2Zeros(double a, double za, double b, double zb, double& t, double& zt)
{
  // Search for two zeros between a and b.
  // search2Zeros is called only if za*zb > 0.
  double c = b-a;
  double d = za+zb;
  t = a + za*(c/d);
  zt = evalZeta.evalZ(t);
  if (zt == 0) zt = evalZeta.evalDZ(t);
  if (za < 0 && zt > 0 || za > 0 && zt < 0) return true;
  double x = (zb-zt)/c/(b-t) - (za-zt)/c/(a-t);
  t = (a+t)/2 - (za-zt)/(2*x*(a-t));
  zt = evalZeta.evalZ(t);
  if (zt == 0) zt = evalZeta.evalDZ(t);
  return (za < 0 && zt > 0 || za > 0 && zt < 0);
}

bool ZetaZeros::search2a(double a, double b, double za, double zb, bool reverse, double& t, double& zt)
{
  // Search for two zeros between a and b.
  // Search2a is called only if za*zb > 0.
  // This routine is essentially due to J. van de Lune, H.J.J te Riele, D.T. Winter.
  double at[514],az[514];
  int next[514];
  bool accept[514];
  if (reverse) {
    at[1] = b; at[513] = a;
    az[1] = zb; az[513] = za;
  } else {
    at[1] = a; at[513] = b;
    az[1] = za; az[513] = zb;
  }
  t = (a+b)*0.5;
  zt = evalZeta.evalZ(t);
  if (zt == 0) zt = evalZeta.evalDZ(t);
  if (za < 0 && zt > 0 || za > 0 && zt < 0) {
    checkClosePairOfZeros(a, t, b, za, zt, zb);
    return true;
  }

  at[257] = t; az[257] = zt;
  accept[1] = false; next[1] = 256;
  accept[257] = false; next[257] = 256;
  bool convex = (fabs(zt) < min(fabs(za), fabs(zb)));
  int index = 1;
  do {
    int indout;
    // Note that parabola is called only in search2a.
    if (parabola(index, at, az, next, accept, indout, convex)) {
      int n = next[index];
      t = at[index+n]; zt = at[index+n];
      checkClosePairOfZeros(a, t, b, za, zt, zb);
      return true;
    }
    do {
      if (isExit()) return false;
      if (indout >= 513) {
        // The whole interval [a, b] has been scanned now by parabola.
        // We check wheather parabola has discovered convexity of abs(Z(t)).
        if (!convex) return false;
        // parabola has detected convexity, so that we should try to find the missing two on [a, b].
        LOGLN(output, "Convexity between " << a << " and " << b << " is detected.");
        indout = 1;
        double a1 = az[1];
        for (index = 1; index < 513; index += next[index]) {
          LOGLN(output, " index=" << index << ", at=" << at[index] << ", az=" << az[index]);
          if (fabs(az[index]) < fabs(a1)) { indout = index; a1 = az[index]; }
        }
        LOGLN(output, " index=" << index << ", at=" << at[index] << ", az=" << az[index]);
        if (indout > 1 && fabs(az[index]) >= fabs(a1)) {
          for (index = 1; index < 513; index += next[index]) {
            if (index+next[index] == indout) {
              a = at[index]; za = az[index];
              index += next[index];
              b = at[index+next[index]]; zb = az[index+next[index]];
              if (a > b) {
                swap(a, b);
                swap(za, zb);
              }
              break;
            }
          }
        }
        if (search2d(a, b, za, zb, t, zt)) return true;
        LOGLN(output, "No zero was found.");
        return false;
      }
      // Transfer the search to the next interval.
      index = indout;

      // Since accept[index]=true, we skip the next interval [at[index], at[index+next[index]]].
      while (index < 513 && accept[index]) index += next[index];
      indout = index;
    } while (index >= 513);
  } while (!accept[index]);
  return false;
}


bool ZetaZeros::search3b(double a, double b, double za, double zb, double* t, double* z)
{
  // We try to find three sign changes of Z(t) in the interval (a, b).
  LOGLN(output, "Call search3b" << " between " << a << " and " << b << " to find 3 zeros.");
  const int CYCLES = 4;
  double h = (b-a)*0.125;
  char ncycles = CYCLES;
  do {
    h *= 0.5;
    bool found = false;
    double z0 = za;
    for (double c = a+h; c < b; c += 2*h) {
      double zc = evalZeta.evalZ(c);
      if (zc == 0) zc = evalZeta.evalDZ(c);
      if (zc < 0 && z0 > 0 || zc > 0 && z0 < 0) {
        // One zeros found.
        z0 = zc;
        if (found) {
          t[1] = c; z[1] = zc;
          checkClosePairOfZeros(a, t[0], t[1], za, z[0], z[1]);
          checkClosePairOfZeros(t[0], t[1], b, z[0], z[1], zb);
          return true;
        }
        found = true;
        t[0] = c; z[0] = zc;
      }
      if (isExit()) return false;
    }
  } while (--ncycles);
  return false;
}

bool ZetaZeros::search3a(double a, double b, double za, double zb, int depth, double* t, double* z)
{
  // We try to find three sign changes of Z(t) in the interval (a, b), where a and b are Gram points.
  LOGLN(output, "Call search3a(" << depth << ") between " << a << " and " << b << " to find 3 zeros.");
  const double h = (b-a)/depth;
  char nfound = 0;
  bool checkNext = false;
  double c0 = a;
  double z0 = za;
  for (double c = a+h; c < b; c += 2*h) {
    double zc = evalZeta.evalZ(c);
    if (checkNext && (zc < 0 && z0 < 0 || zc > 0 && z0 > 0) && (depth != 256 && search2c(c0, c, z0, zc, t[0], z[0]) || depth == 256 && search2d(c0, c, z0, zc, t[0], z[0]))) {
      t[1] = c; z[1] = zc;
      checkClosePairOfZeros(t[0], t[1], b, z[0], z[1], zb);
      return true;
    }
    checkNext = false;
    if (zc == 0) {
      zc = evalZeta.evalDZ(c);
      if (zc < 0 && z0 < 0 || zc > 0 && z0 > 0) {
        checkNext = true;
        if (depth != 256 && search2c(c0, c, z0, zc, t[0], z[0]) || depth == 256 && search2d(c0, c, z0, zc, t[0], z[0])) {
          t[1] = c; z[1] = zc;
          checkClosePairOfZeros(t[0], t[1], b, z[0], z[1], zb);
          return true;
        }
      }
    }
    if (zc < 0 && z0 > 0 || zc > 0 && z0 < 0) { // One zeros found.
      t[nfound] = c; z[nfound] = zc;
      if (++nfound == 2) {
        checkClosePairOfZeros(t[0], t[1], b, z[0], z[1], zb);
        return true;
      }
    }
    if (isExit()) return false;
    c0 = c; z0 = zc;
  }
  if (checkNext && (zb < 0 && z0 < 0 || zb > 0 && z0 > 0) && (depth != 256 && search2c(c0, b, z0, zb, t[1], z[1]) || depth == 256 && search2d(c0, b, z0, zb, t[1], z[1]))) { // since n=42583815545
    checkClosePairOfZeros(a, t[0], t[1], za, z[0], z[1]);
    return true;
  }
  //if (nfound == 0 && depth >= 256) return search3b(c0, b, z0, zb, t, z);
  return false;
}

bool ZetaZeros::searchN(int n, double a, double b, double za, double zb, int depth, double* t, double* z)
{
  // We try to find n sign changes of Z(t) in the interval (a, b), where a and b are Gram points.
  LOGLN(output, "Call searchN(" << depth << ") between " << a << " and " << b << " to find " << n << " zeros.");
  const double h = (b-a)/depth;
  double z0 = za;
  int nfound = 0;
  --n;
  double delta = 2*h;
  for (double c = a+h; c < b; c += delta) {
    double zc = evalZeta.evalZ(c);
    if (zc == 0) zc = evalZeta.evalDZ(c);
    if (zc < 0 && z0 > 0 || zc > 0 && z0 < 0) {
      // One zeros found.
      z0 = zc;
      t[nfound] = c; z[nfound] = zc;
      if (++nfound == n) {
        if (n == 1) {
          checkClosePairOfZeros(a, t[0], b, za, z[0], zb);
        } else if (n > 1) {
          checkClosePairOfZeros(a, t[0], t[1], za, z[0], z[1]);
          for (int i = 2; i < n; ++i) {
            checkClosePairOfZeros(t[i-2], t[i-1], t[i], z[i-2], z[i-1], z[i]);
          }
          checkClosePairOfZeros(t[n-2], t[n-1], b, z[n-2], z[n-1], zb);
        }
        return true;
      }
    } else {
      if (fabs(zc) < 0.000001) { // 4 close zeros at n=158366484202
        delta = h/2;
      } else if (fabs(zc) < 0.00001) {
        delta = h;
      } else {
        delta = 2*h;
      }
    }
    if (isExit()) return false;
  }
  return false;
}

bool ZetaZeros::zigzag(gramBlockType& gramBlock, int gramBlockLength)
{
  const int l = gramBlock.size()-2;
  double t[2],z[2];
  for (int i = 4; i <= 256; i *= 2) {
    int ip = 0;
    for (int k = l-1; k > 0; --k) {
      // The next line controls the zig-zag search of search3, e.g. Gram block size 7: 4,1,3,2.
      if (l&1) ip += (k&1)? -k : k;
      else ip += (k&1)? k : -k;
      if (search3a(gramBlock[ip].t, gramBlock[ip+1].t, gramBlock[ip].z, gramBlock[ip+1].z, i, t, z)) {
        output.output(3, t, gramBlock);
        return true;
      }
      if (isExit()) return false;
    }
    if (i == 128) {
      // search2a and search3a have not found the missing two.
      // We search again in the most suspicious outer Gram interval of the Gram block by means of search2d.
      const double z0 = gramBlock[0].z;
      const double z1 = gramBlock[1].z;
      const double z2 = gramBlock[gramBlockLength-1].z;
      const double z3 = gramBlock[gramBlockLength].z;
      if (fabs(z0+z1) <= fabs(z2+z3)) {
        double g,gz;
        if (search2d(gramBlock[0].t, gramBlock[1].t, z0, z1, g, gz) || search2d(gramBlock[gramBlockLength-1].t, gramBlock[gramBlockLength].t, z2, z3, g, gz)) {
          output.output(g, gramBlock);
          return true;
        }
      } else {
        double g,gz;
        if (search2d(gramBlock[gramBlockLength-1].t, gramBlock[gramBlockLength].t, z2, z3, g, gz) || search2d(gramBlock[0].t, gramBlock[1].t, z0, z1, g, gz)) {
          output.output(g, gramBlock);
          return true;
        }
      }
    }
  }
  return false;
}

int ZetaZeros::evalGramBlockLength(startType& n, gramBlockType& gramBlock)
{
  // Rosser's rule: A "Gram block of length k" is an interval B_j = [g_j,g_(j+k)[
  //                such that g_j and g_(j+k) are good Gram points, g_(j+1),...,g_(j+k-1)
  //                are bad Gram points, and k>=1.
  //                In other words: [g_j,g_(j+1)[ and [g_(j+k-1),g_(j+k)[ contain an even number of zeros
  //                and all other intervals between two Gram points contain an odd number of zeros
  int sz = gramBlock.size()-1;
  double z0,z1 = gramBlock[sz].z;
  double t0 = gramBlock[sz].t;
  do {
    n += 1;
    double t1 = Gram(n, t0);
    z0 = z1; z1 = evalZeta.evalZ(t0, t1);
    gramBlock.push_back(GramBlock(t1, z1));
    t0 = t1; ++sz;
  } while (z0 < 0 && z1 > 0 || z0 > 0 && z1 < 0);
  return sz;
}

bool ZetaZeros::newRule(gramBlockType& gramBlock, int activateNewRule)
// Gram block     activateNewRule
//   g_-2               3
//   g_-1               0
//   g_0
//   g_1                1,2
//   g_2                4
{
  if (activateNewRule > 4) return false;
  int i;
  const int gramBlockLength = gramBlock.size()-1;
  LOGLN(output, "Call the new rule for a Gram block of length " << gramBlockLength << '.');
  for (i = 0; i <= gramBlockLength; ++i) {
    LOGLN(output, " i=" << i << ", t=" << gramBlock[i].t << ", z=" << gramBlock[i].z << ", #zeros=" << gramBlock[i].zeros);
  }
  if (gramBlockLength == 1) {
    for (i = 4; i <= 256; i *= 2) {
      double t[2],z[2];
      if (search3a(gramBlock[0].t, gramBlock[1].t, gramBlock[0].z, gramBlock[1].z, i, t, z)) {
        if (activateNewRule >= 3) LOGLN(output, "Extreme S(t) between " << gramBlock[0].t << " and " << gramBlock[1].t);
        if (activateNewRule != 3) output.removePreviousOutput();
        output.output(3, t, gramBlock);
        if (activateNewRule == 3) output.adjustPreviousOutput();
        return true;
      }
    }
    return false;
  }
  bool has3Zeros = false;   // inside a Gram interval
  for (i = 2; !has3Zeros && i < gramBlockLength; ++i) {
    has3Zeros = (gramBlock[i].zeros == 3);
  }
  if (!has3Zeros) {
    double t1,z1,t2,z2;
    if (search2d(gramBlock[0].t, gramBlock[1].t, gramBlock[0].z, gramBlock[1].z, t1, z1) && search2d(gramBlock[gramBlockLength-1].t, gramBlock[gramBlockLength].t, gramBlock[gramBlockLength-1].z, gramBlock[gramBlockLength].z, t2, z2)) {
      if (activateNewRule >= 3) LOGLN(output, "Extreme S(t) between " << gramBlock[0].t << " and " << gramBlock[1].t);
      if (activateNewRule != 3) output.removePreviousOutput();
      output.output2(t1, t2, gramBlock);
      if (activateNewRule == 3) output.adjustPreviousOutput();
      return true;
    }
  }
  for (i = 4; i <= 256; i *= 2) {
    double t[2],z[2];
    for (int j = 2; j < gramBlockLength; ++j) {
      if (gramBlock[j].zeros == 1 && search3a(gramBlock[j-1].t, gramBlock[j].t, gramBlock[j-1].z, gramBlock[j].z, i, t, z)) {
        if (activateNewRule >= 3) LOGLN(output, "Extreme S(t) between " << gramBlock[j-1].t << " and " << gramBlock[j].t);
        if (activateNewRule != 3) output.removePreviousOutput();
        output.output(3, t, gramBlock);
        if (activateNewRule == 3) output.adjustPreviousOutput();
        return true;
      }
    }
  }
  for (i = 4; i <= 256; i *= 2) {
    double t[3],z[3];
    if (gramBlock[1].zeros == 2 && searchN(4, gramBlock[0].t, gramBlock[1].t, gramBlock[0].z, gramBlock[1].z, i, t, z)
     || gramBlock[gramBlockLength].zeros == 2 && searchN(4, gramBlock[gramBlockLength-1].t, gramBlock[gramBlockLength].t, gramBlock[gramBlockLength-1].z, gramBlock[gramBlockLength].z, i, t, z)) {
      if (activateNewRule >= 3) LOGLN(output, "Extreme S(t) between " << gramBlock[0].t << " and " << gramBlock[gramBlockLength].t);
      if (activateNewRule != 3) output.removePreviousOutput();
      output.output(4, t, gramBlock);
      if (activateNewRule == 3) output.adjustPreviousOutput();
      return true;
    }
  }
  if (has3Zeros) {
    double t1,z1;
    if (gramBlock[1].zeros == 0 && search2d(gramBlock[0].t, gramBlock[1].t, gramBlock[0].z, gramBlock[1].z, t1, z1)
     || gramBlock[gramBlockLength].zeros == 0 && search2d(gramBlock[gramBlockLength-1].t, gramBlock[gramBlockLength].t, gramBlock[gramBlockLength-1].z, gramBlock[gramBlockLength].z, t1, z1)) {
      if (activateNewRule >= 3) LOGLN(output, "Extreme S(t) between " << gramBlock[0].t << " and " << gramBlock[gramBlockLength].t);
      if (activateNewRule != 3) output.removePreviousOutput();
      output.output(t1, gramBlock);
      if (activateNewRule == 3) output.adjustPreviousOutput();
      return true;
    }
  }
  return false;
}

static void outputPID()
{
  ofstream fout("zeta_zeros.pid");
  fout << getpid();
}

int ZetaZeros::searchZeros(startType LASTN, int NRANGE, int sleepN)
// Index of the last known zero.
// Number of zeros
{
  if (LASTN < 0) {
    cerr << "index of the last known zero must be greater than or equal to 0\n";
    return 2;
  }
  if (NRANGE <= 0) {
    cerr << "number of zeros must be greater than 0\n";
    return 3;
  }

  int newZeros = output.getInitResult();
  if (newZeros == -1) return 4;
  startType LASTN_ORIG = LASTN;
  int NRANGE_ORIG = NRANGE;
  LASTN += newZeros;
  if (NRANGE < newZeros) NRANGE = 0;
  else NRANGE -= newZeros;
  newZeros = 0;
  if (!evalZeta.init(LASTN, NRANGE)) return 5;

  while (changeThreadId) {
    _sleep(200);
  }
  changeThreadId = true;
  int currentThreadId = ++threadId;
  ++activeThreads;
  if (showThreadStatus == 0) {
    showThreadStatus = currentThreadId;
  }
  changeThreadId = false;
  char filenameThreadStatus[50];
  if (showThreadStatus == currentThreadId) {
    strcpy(filenameThreadStatus, "zeta_zeros.tmp");
  } else {
    char c[20];
    strcat(strcat(strcpy(filenameThreadStatus, "zeta_zeros_"), lltoa(currentThreadId, c, 10)), ".tmp");
  }
  outputPID();

  sleepMode = sleepN;
  clock_t tottime = clock();
  list<gramBlockType> prevGramBlocks;
  const int MAX_PREV_GRAM_BLOCKS = 3;
  // LASTN is the index of the last known zero.
  // It is advised to take LASTN from the output of the previous run.
  startType n;
  double g0,z0;
  while (true) {
    // We call a Gram point g_j good if (-1)^jZ(g_j)>0, and bad otherwise.
    // We start with a good Gram point.
    n = LASTN-1;
    if (n == -1) g0 = 9.6669;
    else if (n == 0) g0 = 17.8456;
    else if (n == 1) g0 = 23.1703;
    else g0 = Gram(n, TWO_PI*n/log(double(n)));
    z0 = evalZeta.evalZ(g0);
    output.setupPrecision(g0);
    LOGLN(output, "Initial Gram point G(" << n << ")=" << g0);
    if (z0 == 0) {
      LOGLN(output, "Bad start with LASTN=" << LASTN << ", Z(" << g0 << ")=" << z0 << " this starting value is too small.\n"
            << "LASTN is decreased by 1 due to this 'unclear' value of Z(t).");
    } else {
      if ((n&1) == 0 && z0 > 0 || (n&1) == 1 && z0 < 0) break;
      LOGLN(output, "LASTN is decreased by 1 due to the bad initial Gram point G(" << n << ")=" << g0);
    }
    LASTN -= 1;
  }
  output.setupPrecision(g0);
  int activateNewRule = 0;
  logHappened = false;
  while (newZeros < NRANGE && !isExit()) {
    if (logHappened) {
      LOGLN(output, "This happened at " << n);
      logHappened = false;
    }
    // We are going to set up a Gram block of length gramBlockLength.
    gramBlockType gramBlock;
    gramBlock.push_back(GramBlock(g0, z0));
    n += 1;
    double g1 = Gram(n, g0);
    double z1 = evalZeta.evalZ(g0, g1);
    gramBlock.push_back(GramBlock(g1, z1));
    if ((newZeros&1023) == 0) {
      ofstream fout(filenameThreadStatus);
      if (fout.good()) fout << n << ',' << LASTN_ORIG << ',' << NRANGE_ORIG << ',' << ((evalZeta.nzevalu == 0)? 0.0 : double(evalZeta.ztime)/double(evalZeta.nzevalu)/CLOCKS_PER_SEC) << ',' << ((evalZeta.ndzevalu == 0)? 0.0 : double(evalZeta.dztime)/double(evalZeta.ndzevalu)/CLOCKS_PER_SEC);
    }
    if (z0 < 0 && z1 > 0 || z0 > 0 && z1 < 0) { // We encounter a Gram interval (gramBlockLength = 1).
      output.output(gramBlock);
      if (activateNewRule > 0) {
        if (((++activateNewRule)&1) == 1 && activateNewRule >= 3) ++activateNewRule;
        if (activateNewRule > MAX_PREV_GRAM_BLOCKS+1) activateNewRule = 0;
        else if (newRule(gramBlock, activateNewRule) || prevGramBlocks.size() > 0 && newRule(prevGramBlocks.front(), activateNewRule+1)) {
          LOGLN(output, "We found the missing two sign changes between " << g0 << " and " << g1
                << ".\nThis happened between " << n-1 << " and " << n);
          activateNewRule = 0;
        }
      }
      ++newZeros; g0 = g1; z0 = z1;
      prevGramBlocks.push_back(gramBlock);
      if (prevGramBlocks.size() > MAX_PREV_GRAM_BLOCKS) prevGramBlocks.pop_front();
      continue;
    }

    if (isExit()) break;
    // Exception to Gram's law. gramBlockLength will be > 1.
    const int gramBlockLength = evalGramBlockLength(n, gramBlock);
    // Note that in this Gram block we have already detected gramBlockLength-2 zeros.
    // The missing two are firstly sought in the two outer intervals of the block.
    bool foundZeros = false;
    const double g2 = gramBlock[gramBlockLength-1].t;
    const double z2 = gramBlock[gramBlockLength-1].z;
    const double g3 = gramBlock[gramBlockLength].t;
    const double z3 = gramBlock[gramBlockLength].z;
    double g,z;
    if (fabs(z0+z1) >= fabs(z2+z3)) {
      foundZeros = search2a(g2, g3, z2, z3, false, g, z) || search2a(g0, g1, z0, z1, true, g, z);
    } else {
      foundZeros = search2a(g0, g1, z0, z1, true, g, z) || search2a(g2, g3, z2, z3, false, g, z);
    }
    if (!foundZeros && gramBlockLength == 2) {
      // search2a have not found the missing two.
      // We search again by means of search2b and search2d.
      foundZeros = search2b(g0, g3, z0, z3, g, z);
      if (!foundZeros) {
        if (fabs(z0+z1) <= fabs(z2+z3)) {
          foundZeros = (search2d(g0, g1, z0, z1, g, z) || search2d(g2, g3, z2, z3, g, z));
        } else {
          foundZeros = (search2d(g2, g3, z2, z3, g, z) || search2d(g0, g1, z0, z1, g, z));
        }
      }
    }
    if (foundZeros) {
      output.output(g, gramBlock);
      if (activateNewRule > 0) {
        if (((++activateNewRule)&1) == 1 && activateNewRule >= 3) ++activateNewRule;
        if (activateNewRule > MAX_PREV_GRAM_BLOCKS+1) activateNewRule = 0;
        else if (newRule(gramBlock, activateNewRule) || prevGramBlocks.size() > 0 && newRule(prevGramBlocks.front(), activateNewRule+1)) {
          LOGLN(output, "We found the missing two sign changes between " << g0 << " and " << g3
                << ".\nThis happened between " << n-gramBlockLength << " and " << n);
          activateNewRule = 0;
        }
      }
      newZeros += gramBlockLength; g0 = g3; z0 = z3;
      prevGramBlocks.push_back(gramBlock);
      if (prevGramBlocks.size() > MAX_PREV_GRAM_BLOCKS) prevGramBlocks.pop_front();
      continue;
    }

    if (isExit()) break;
    if (gramBlockLength > 2 && zigzag(gramBlock, gramBlockLength)) {  // Rosser's rule
      if (activateNewRule > 0) {
        if (((++activateNewRule)&1) == 1 && activateNewRule >= 3) ++activateNewRule;
        if (activateNewRule > MAX_PREV_GRAM_BLOCKS+1) activateNewRule = 0;
        else if (newRule(gramBlock, activateNewRule) || prevGramBlocks.size() > 0 && newRule(prevGramBlocks.front(), activateNewRule+1)) {
          LOGLN(output, "We found the missing two sign changes between " << g0 << " and " << g3
                << ".\nThis happened between " << n-gramBlockLength << " and " << n);
          activateNewRule = 0;
        }
      }
      newZeros += gramBlockLength; g0 = g3; z0 = z3;
      prevGramBlocks.push_back(gramBlock);
      if (prevGramBlocks.size() > MAX_PREV_GRAM_BLOCKS) prevGramBlocks.pop_front();
      continue;
    }

    if (isExit()) break;
    // violation of Rosser's rule
    // New rule: check previous and next Gram block
    if (prevGramBlocks.size() > 0 && newRule(prevGramBlocks.back(), activateNewRule)) {
      LOGLN(output, "The new rule found two additional sign changes in previous Gram block.");
      newZeros += 2;
      activateNewRule = 0;
    } else {
      LOGLN(output, "We did not yield any zeros between " << g0 << " and " << g3
            << " and the new rule has not found additional zeros in previous Gram block.\n"
            << "Therefore, the new rule will be activated for the next Gram block.\n"
            << "Number of sign changes should be " << gramBlockLength << ", but we found only " << gramBlockLength-2
            << ".\nThis happened between " << n-gramBlockLength << " and " << n << '.');
      activateNewRule = 1;
    }
    output.outputError(gramBlock);
    newZeros += gramBlockLength-2; g0 = g3; z0 = z3;
    prevGramBlocks.push_back(gramBlock);
    if (prevGramBlocks.size() > MAX_PREV_GRAM_BLOCKS) prevGramBlocks.pop_front();
  }
  if (logHappened) {
    LOGLN(output, "This happened at " << n);
  }
  if (!isExit() && LASTN < n) {
    endJob(output, LASTN, n, newZeros, g0, clock() - tottime, evalZeta.nzevalu, evalZeta.ztime, evalZeta.ndzevalu, evalZeta.dztime);
  }

  remove(filenameThreadStatus);
  remove("zeta_zeros.pid");
  if (isExit()) {
    LOGLN(output, "Exit n=" << n);
    output.destroy(false);
    if (--activeThreads > 0) {
      _sleep(1000);
    }
    showThreadStatus = 0;
    exitCalc = false;
    if (terminateCalc != 0) {
      exit(0);
    }
    return 0;
  }
  --activeThreads;
  if (showThreadStatus == currentThreadId) {
    showThreadStatus = 0;
  }
  return 0;
}

//#include "test/test.cpp"
//#include "gaps.cpp"
//#include "detect.c"


double Gram2(longlong n, double a)
// Using a as an initial approximation the nth Gram point is calculated and assigned to b.
// g_n * (ln(g_n/(2*pi))-1) < 2*pi * (n - 1/2 + 1/8)
// Then h_n < (h_n+n+5/8)/ln(h_n) where h_n = g_n/(2*pi)
{
  double t2,t1 = a*TWO_PI_INV;
  double d = double(n) + 0.125;
  do {
    t2 = (t1+d)/log(t1);
    if (fabs(t1-t2) < t2*1e-13) return t2*TWO_PI;
    t1 = (t2+d)/log(t2);
  } while (fabs(t1-t2) >= t1*1e-13);
  return t1*TWO_PI;
}

int main(int argc, char** argv)
{
  setCoutLog(false);
  longlong n = longlong(1000000) + 50;
  ZetaZeros zetaZeros2(n, 1, 0);
  cout.precision(20);
  n = 10000;
  double t = 1000.0;
  for (n = 10000; n < 10020; ++n) {
    double z0,z1;
    t = Gram2(n, t);
    zetaZeros2.evalZeta.evalDZ(t, z0, z1);
    cout << ">" << t << ": " << z0 << ", " << sin(t*log(2)-theta(t)) << endl;
    t = Gram(n, t);
    zetaZeros2.evalZeta.evalDZ(t, z0, z1);
    cout << "G" << t << ": " << z0 << endl;
  }
  
  /*longlong n = longlong(1000000000000) + 50;
  ZetaZeros zetaZeros2(n, 1, 0);
  cout.precision(20);
  double t = 267653395649.38727;
  double z0,z1,z2,z3;
  zetaZeros2.evalZeta.evalZ(t, z0, z1);
  zetaZeros2.evalZeta.evalDZ(t, z2, z3);
  if (z2 < z0 || z1 < z3) {
    cout << "t=" << t << ", z0=" << z0 << ", z1=" << z1 << ", z2=" << z2 << ", z3=" << z3 << endl;
    cout << "ERROR!" << endl;
    return 0;
  }
  cout << "OK." << endl;*/
  // bad ecorate at 35663817459 15
  //plot(287448797070, 12);
  //smallGaps();
  //test9();
  //test10();
  if (argc == 2 && strcmp(argv[1], "check") == 0) {
    checkZeros();
    return 0;
  }
  if (argc == 2 && strcmp(argv[1], "stat") == 0) {
    statistic(0, "", false);
    return 0;
  }
  if (argc != 3 && argc != 4 && argc != 5) {
    cerr << "USAGE: " << argv[0] << " <index of the last known zero> <number of zeros> [<method> [<sleep in milliseconds>]]\n";
    return 1;
  }
  if (argc >= 4) {
    EvalZeta::setFastSumZMethod(atoi(argv[3]));
  }
  ZetaZeros zetaZeros(atoll(argv[1]), atoi(argv[2]), (argc == 5)? atoi(argv[4]) : 0);
  return zetaZeros.getResult();
}
