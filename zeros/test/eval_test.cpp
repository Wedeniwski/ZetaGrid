#include <cmath>

#include "zeta_zeros.h"
#include "eval_zeta.h"

const int NPREP  = 1048576;   // using for grid
const int NPREP2 = NPREP/2;
const double GRID = TWO_PI/NPREP;
const double GRID_INV = NPREP*TWO_PI_INV;

static void fastSumZ(double t, const lnSqrt* p, const lnSqrt* q, double thetaMod, const double* cosValue, double* zLowHigh)
{
  double value[16];
  do {
    value[0] = p[0].sqrtinvLow; value[1] = p[0].sqrtinvHigh;
    value[4] = p[1].sqrtinvLow; value[5] = p[1].sqrtinvHigh;
    value[8] = p[2].sqrtinvLow; value[9] = p[2].sqrtinvHigh;
    value[12] = p[3].sqrtinvLow; value[13] = p[3].sqrtinvHigh;

    /*value[2] = t*p[0].ln; value[6] = t*p[1].ln;
    value[10] = t*p[2].ln; value[14] = t*p[3].ln;
    value[2] -= floor(value[2]*TWO_PI_INV)*TWO_PI;
    value[6] -= floor(value[6]*TWO_PI_INV)*TWO_PI;
    value[10] -= floor(value[10]*TWO_PI_INV)*TWO_PI;
    value[14] -= floor(value[14]*TWO_PI_INV)*TWO_PI;
    const int idx0 = int(fabs(thetaMod-value[2])*GRID_INV);
    const int idx1 = int(fabs(thetaMod-value[6])*GRID_INV);
    const int idx2 = int(fabs(thetaMod-value[10])*GRID_INV);
    const int idx3 = int(fabs(thetaMod-value[14])*GRID_INV);*/

    const int idx0 = int(fabs(thetaMod-fmod(t*p[0].ln, TWO_PI))*GRID_INV);
    const int idx1 = int(fabs(thetaMod-fmod(t*p[1].ln, TWO_PI))*GRID_INV);
    const int idx2 = int(fabs(thetaMod-fmod(t*p[2].ln, TWO_PI))*GRID_INV);
    const int idx3 = int(fabs(thetaMod-fmod(t*p[3].ln, TWO_PI))*GRID_INV);
    /*const int idx0 = int(fabs(thetaMod-atan2(TWO_PI, t*p[0].ln))*GRID_INV);
    const int idx1 = int(fabs(thetaMod-atan2(TWO_PI, t*p[1].ln))*GRID_INV);
    const int idx2 = int(fabs(thetaMod-atan2(TWO_PI, t*p[2].ln))*GRID_INV);
    const int idx3 = int(fabs(thetaMod-atan2(TWO_PI, t*p[3].ln))*GRID_INV);*/

    value[2] = cosValue[idx0]; value[3] = cosValue[idx0+1];
    value[6] = cosValue[idx1]; value[7] = cosValue[idx1+1];
    value[10] = cosValue[idx2]; value[11] = cosValue[idx2+1];
    value[14] = cosValue[idx3]; value[15] = cosValue[idx3+1];
    /*if (idx0 > NPREP2) swap(value[2], value[3]);
    if (idx1 > NPREP2) swap(value[6], value[7]);
    if (idx2 > NPREP2) swap(value[10], value[11]);
    if (idx3 > NPREP2) swap(value[14], value[15]);*/
    if (idx0 > NPREP2) { double d = value[2]; value[2] = value[3]; value[3] = d; }
    if (idx1 > NPREP2) { double d = value[6]; value[6] = value[7]; value[7] = d; }
    if (idx2 > NPREP2) { double d = value[10]; value[10] = value[11]; value[11] = d; }
    if (idx3 > NPREP2) { double d = value[14]; value[14] = value[15]; value[15] = d; }
    zLowHigh[0] += value[0]*value[3] + value[4]*value[7] + value[8]*value[11] + value[12]*value[15];
    zLowHigh[1] += value[1]*value[2] + value[5]*value[6] + value[9]*value[10] + value[13]*value[14];
    p += 4;
  } while (p < q);
}
