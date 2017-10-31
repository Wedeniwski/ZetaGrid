// icl -DNPREP=1048576 -G5 -O2 -Qrcd -I.. -I../../../arithmetic/doubledouble -c fastSum.cpp -FofastSum2.obj -FA
// icl -DNPREP=2097152 -G5 -O2 -Qrcd -I.. -I../../../arithmetic/doubledouble -c fastSum.cpp -FofastSum3.obj
// icl -DNPREP=4194304 -G5 -O2 -Qrcd -I.. -I../../../arithmetic/doubledouble -c fastSum.cpp -FofastSum4.obj

#include <math.h>

const double PI = 3.1415926535897932384626433832795028841971693993751L;
const double PI_INV = 1.0/PI;
const double TWO_PI = 2*PI;
const double TWO_PI_INV = 0.5*PI_INV;

#ifndef NPREP // using for grid
#define NPREP 1048576
#endif
#define NPREP2 (NPREP/2)
const double GRID_INV = NPREP*TWO_PI_INV;

struct lnSqrt {
  double ln;
  double sqrtinv;
};

const double HALF = 0.5;

inline int fastInnerSum(double thetaMod, double tLn)
{
  int a;
  __asm {
          fld   TWO_PI
          fld   tLn
          fprem
          fstp  st(1)
          fsubr thetaMod
          fabs
          fmul  GRID_INV
          fsub  HALF
          fistp a
  }
  return a+a;
}

inline double fastFmod2Pi(double x)
{
  double a;
  __asm {
          fld TWO_PI
          fld x
          fprem
          fstp a
          fstp st(1)
  }
  return a;
}


#if NPREP == 1048576
void fastSumZ2(double t, const lnSqrt* p, const lnSqrt* q, double thetaMod, const double* cosValue, double* zLowHigh)
#elif NPREP == 2097152
void fastSumZ3(double t, const lnSqrt* p, const lnSqrt* q, double thetaMod, const double* cosValue, double* zLowHigh)
#else
void fastSumZ4(double t, const lnSqrt* p, const lnSqrt* q, double thetaMod, const double* cosValue, double* zLowHigh)
#endif
{
  double value[12];
  do {
    value[0] = p[0].sqrtinv; value[3] = p[1].sqrtinv; value[6] = p[2].sqrtinv; value[9] = p[3].sqrtinv;
    /*const int idx0 = fastInnerSum(thetaMod, t*p[0].ln);
    const int idx1 = fastInnerSum(thetaMod, t*p[1].ln);
    const int idx2 = fastInnerSum(thetaMod, t*p[2].ln);
    const int idx3 = fastInnerSum(thetaMod, t*p[3].ln);*/
    const int idx0 = 2*int(fabs(thetaMod-fastFmod2Pi(t*p[0].ln))*GRID_INV-HALF);
    const int idx1 = 2*int(fabs(thetaMod-fastFmod2Pi(t*p[1].ln))*GRID_INV-HALF);
    const int idx2 = 2*int(fabs(thetaMod-fastFmod2Pi(t*p[2].ln))*GRID_INV-HALF);
    const int idx3 = 2*int(fabs(thetaMod-fastFmod2Pi(t*p[3].ln))*GRID_INV-HALF);
    /*value[0] = p[0].sqrtinv;
    value[1] = p[0].ln;
    value[3] = p[1].sqrtinv;
    value[4] = p[1].ln;
    value[6] = p[2].sqrtinv;
    value[7] = p[2].ln;
    value[9] = p[3].sqrtinv;
    value[10] = p[3].ln;
    value[1] *= t; value[4] *= t; value[7] *= t; value[10] *= t;
    value[1] = fastFmod2Pi(value[1]);
    value[4] = fastFmod2Pi(value[4]);
    value[7] = fastFmod2Pi(value[7]);
    value[10] = fastFmod2Pi(value[10]);
    value[1] -= thetaMod;
    value[4] -= thetaMod;
    value[7] -= thetaMod;
    value[10] -= thetaMod;
    value[1] = fabs(value[1]);
    value[4] = fabs(value[4]);
    value[7] = fabs(value[7]);
    value[10] = fabs(value[10]);
    value[1] *= GRID2_INV;
    value[4] *= GRID2_INV;
    value[7] *= GRID2_INV;
    value[10] *= GRID2_INV;
    value[1] -= HALF;
    value[4] -= HALF;
    value[7] -= HALF;
    value[10] -= HALF;
    const int idx0 = int(value[1]);
    const int idx1 = int(value[4]);
    const int idx2 = int(value[7]);
    const int idx3 = int(value[10]);*/
    value[1] = cosValue[idx0]; value[2] = cosValue[idx0+1];
    value[4] = cosValue[idx1]; value[5] = cosValue[idx1+1];
    value[7] = cosValue[idx2]; value[8] = cosValue[idx2+1];
    value[10] = cosValue[idx3]; value[11] = cosValue[idx3+1];
    zLowHigh[0] += value[0]*value[2] + value[3]*value[5] + value[6]*value[8] + value[9]*value[11];
    zLowHigh[1] += value[0]*value[1] + value[3]*value[4] + value[6]*value[7] + value[9]*value[10];
    p += 4;
  } while (p < q);
}
