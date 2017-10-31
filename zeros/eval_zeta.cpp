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
     J. Diermeier
--*/


#include <algorithm>
#include <ctime>
#include <cmath>

#include "zeta_zeros.h"
#include "eval_zeta.h"

#ifdef _DEFINE_SLEEP_
#include <unistd.h>
inline void _sleep(int s)
{
  sleep(s);
}
#endif

static double* getCosMem(int NPREP)
{
  double* d = new double[2*NPREP+2];
  d[2*NPREP+1] = 0.0;
  return d;
}

struct FreeSqrtinvMem {
  lnSqrt* sqrtinvLn;
  doubledouble* lnDD;
  doubledouble* sqrtinvDD;
  int MDIMENS;
  int usage;
  static int active;

  FreeSqrtinvMem() { usage = MDIMENS = 0; sqrtinvLn = 0; lnDD = sqrtinvDD = 0; }
  FreeSqrtinvMem(const FreeSqrtinvMem& mem) { *this = mem; }
  FreeSqrtinvMem& operator=(const FreeSqrtinvMem& mem) { usage = mem.usage; MDIMENS = mem.MDIMENS; sqrtinvLn = mem.sqrtinvLn; lnDD = mem.lnDD; sqrtinvDD = mem.sqrtinvDD; return *this; }

  void get(int MDIMENS, lnSqrt*& sqrtinvLn, doubledouble*& lnDD, doubledouble*& sqrtinvDD);
  void free(lnSqrt*& sqrtinvLn, doubledouble*& lnDD, doubledouble*& sqrtinvDD);
} sqrtinvMem;

int FreeSqrtinvMem::active = 0;

void FreeSqrtinvMem::get(int MDIMENS, lnSqrt*& sqrtinvLn, doubledouble*& lnDD, doubledouble*& sqrtinvDD)
{
  if (MDIMENS <= sqrtinvMem.MDIMENS) {
    ++sqrtinvMem.usage;
    sqrtinvLn = sqrtinvMem.sqrtinvLn;
    lnDD = sqrtinvMem.lnDD;
    sqrtinvDD = sqrtinvMem.sqrtinvDD;
  } else {
    MDIMENS += 500;
    sqrtinvLn = new lnSqrt[MDIMENS+1];
    lnDD = new doubledouble[MDIMENS+1];
    sqrtinvDD = new doubledouble[MDIMENS+1];
    for (int i = 1; i <= MDIMENS; ++i) {
      doubledouble di(i);
      lnDD[i] = di = log(di);
      sqrtinvLn[i].ln = double(di);
      di = i;
      sqrtinvDD[i] = di = sqrt(recip(di));
      sqrtinvLn[i].sqrtinv = double(di);
    }
    if (FreeSqrtinvMem::active == 1 && sqrtinvMem.usage == 0) {
      sqrtinvMem.usage = 1;
      delete[] sqrtinvMem.sqrtinvLn;
      sqrtinvMem.sqrtinvLn = sqrtinvLn;
      delete[] sqrtinvMem.lnDD;
      sqrtinvMem.lnDD = lnDD;
      delete[] sqrtinvMem.sqrtinvDD;
      sqrtinvMem.sqrtinvDD = sqrtinvDD;
      sqrtinvMem.MDIMENS = MDIMENS;
    }
  }
}

void FreeSqrtinvMem::free(lnSqrt*& sqrtinvLn, doubledouble*& lnDD, doubledouble*& sqrtinvDD)
{
   if (sqrtinvMem.usage > 0 && sqrtinvMem.sqrtinvLn == sqrtinvLn) {
    --sqrtinvMem.usage;
  } else {
    delete[] sqrtinvLn;
    sqrtinvLn = 0;
    delete[] lnDD;
    lnDD = 0;
    delete[] sqrtinvDD;
    sqrtinvDD = 0;
  }
}

int NPREP  = 524288;   // using for grid
int NPREP2 = NPREP/2;
double GRID = TWO_PI/NPREP;
double GRID_INV = NPREP*TWO_PI_INV;

int sleepMode = 0;
int sleepCounter = 0;
static bool SSE2Available = 0;

EvalZeta* EvalZeta::running = 0;
int EvalZeta::fastSumZMethod = 0;
double* EvalZeta::cosValue = getCosMem(NPREP);
doubledouble EvalZeta::TWO_PI_DD;
doubledouble EvalZeta::TWO_PI_INV_DD;
doubledouble EvalZeta::DC0[DCI_SIZE];
doubledouble EvalZeta::DC1[DCI_SIZE];
doubledouble EvalZeta::DC2[DCI_SIZE];
doubledouble EvalZeta::DC3[DCI_SIZE];
doubledouble EvalZeta::DDC0_LOW[DCI_SIZE];
doubledouble EvalZeta::DDC1_LOW[DCI_SIZE];
doubledouble EvalZeta::DDC2_LOW[DCI_SIZE];
doubledouble EvalZeta::DDC3_LOW[DCI_SIZE];
doubledouble EvalZeta::DDC0_HIGH[DCI_SIZE];
doubledouble EvalZeta::DDC1_HIGH[DCI_SIZE];
doubledouble EvalZeta::DDC2_HIGH[DCI_SIZE];
doubledouble EvalZeta::DDC3_HIGH[DCI_SIZE];
double EvalZeta::DC0_LOW[DCI_SIZE];
double EvalZeta::DC1_LOW[DCI_SIZE];
double EvalZeta::DC2_LOW[DCI_SIZE];
double EvalZeta::DC3_LOW[DCI_SIZE];
double EvalZeta::DC0_HIGH[DCI_SIZE];
double EvalZeta::DC1_HIGH[DCI_SIZE];
double EvalZeta::DC2_HIGH[DCI_SIZE];
double EvalZeta::DC3_HIGH[DCI_SIZE];


void EvalZeta::setFastSumZMethod(int method)
{
  int oldNPREP = NPREP;
  NPREP = 524288;
  if (method == 1) {
    NPREP <<= 1;
  } else if (method == 2) {
    NPREP <<= 2;
  } else if (method == 3) {
    NPREP <<= 3;
  }
  fastSumZMethod = method;
  NPREP2 = NPREP/2;
  GRID = TWO_PI/NPREP;
  GRID_INV = NPREP*TWO_PI_INV;
  if (oldNPREP != NPREP) {
    delete[] cosValue;
    cosValue = getCosMem(NPREP);
  }
}


// #define DOUBLE2INT(i, d) { double t = ((d)+6755399441055744.0); i = *((int*)(&t)); }

inline int double2Int(double d)
{
#ifndef _FAST_CONVERSION_
  return int(d);
#else
  d += 103079215104.0;
#if defined(BigEndian_) || defined(_BIG_ENDIAN_)
  return ((int*)(&d))[1] >> 16;
#else
  return ((int*)(&d))[0] >> 16;
#endif
#endif
}

#if _M_IX86 >= 300 && defined(_MSC_VER)
static double posFastMod2Pi(double x)
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

const double HALF = 0.5;
static int fastInnerSumZ(double thetaMod, double tLn)
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
#elif (defined (__i386__) || defined (__i486__)) && defined (__GNUC__)
inline double posFastMod2Pi(double x)
{
  register double value;
  __asm__ ("fprem" : "=t" (value) : "0" (x), "u" (TWO_PI));
  return value;
}

inline int fastInnerSumZ(double thetaMod, double tLn)
{
  return double2Int(fabs(thetaMod-posFastMod2Pi(tLn))*GRID_INV) << 1;
}
#elif defined(_FAST_MOD_)
typedef union {
  double value;
  struct {
#if defined(BigEndian_) || defined(_BIG_ENDIAN_)
    unsigned int msw;
    unsigned int lsw;
#else
    unsigned int lsw;
    unsigned int msw;
#endif
  } parts;
} ieee_double;

// x > 2*pi
static double posFastMod2Pi(double x)
{
  ieee_double y;
  y.value = x;
  unsigned int lx = y.parts.lsw;
  int hx = y.parts.msw;
  if (hx <= 1075388923) {
    if (hx < 1075388923 || lx < 1413754136) {
      return x;
    }
    if (lx == 1413754136) {
      return 0.0;
    }
  }

  int n = (hx>>20)-1025;
  hx = 0x00100000|(0x000fffff&hx);
  for (; n; --n) {
    if (lx < 1413754136) {
      if (hx >= 1647100) {
        lx -= 1413754136;
        hx = ((hx-1647100)<<1)|(lx>>31);
      } else {
        hx = (hx<<1)|(lx>>31);
      }
    } else if (hx >= 1647099) {
      if (hx == 1647099 && lx == 1413754136) {
        return 0.0;
      }
      lx -= 1413754136;
      hx = ((hx-1647099)<<1)|(lx>>31);
    } else {
      hx = (hx<<1)|(lx>>31);
    }
    lx <<= 1;
  }
  if (lx < 1413754136) {
    if (hx >= 1647100) {
      hx -= 1647100;
      lx -= 1413754136;
    }
  } else {
    if (hx >= 1647099) {
      hx -= 1647099;
      lx -= 1413754136;
    }
  }
  if (hx >= 0x00100000) {
    hx = (hx-0x00100000)|0x40100000;
  } else if ((hx|lx) == 0) {
    return 0.0;
  } else {
    n = 0x40100000;
    do {
      hx = (hx<<1)|(lx>>31);
      lx <<= 1;
      n -= 0x00100000;
    } while (hx < 0x00100000);
    hx = (hx-0x00100000)|n;
  }
  y.parts.msw = hx;
  y.parts.lsw = lx;
  return y.value;
}

inline int fastInnerSumZ(double thetaMod, double tLn)
{
  return double2Int(fabs(thetaMod-posFastMod2Pi(tLn))*GRID_INV) << 1;
}
#else
inline double posFastMod2Pi(double x)
{
  return fmod(x, TWO_PI);
}

inline int fastInnerSumZ(double thetaMod, double tLn)
{
  return double2Int(fabs(thetaMod-posFastMod2Pi(tLn))*GRID_INV) << 1;
}
#endif

#if _M_IX86 >= 300 && defined(__INTEL_COMPILER)
//__declspec(cpu_specific(pentium_4))
static void fastSumZ(double t, const lnSqrt* p, const lnSqrt* q, double thetaMod, const double* cosValue, double* zLowHigh)
//; parameter 1: 8 + ebx
//; parameter 2: 16 + ebx
//; parameter 3: 20 + ebx
//; parameter 4: 24 + ebx
//; parameter 5: 32 + ebx
//; parameter 6: 36 + ebx
// icl -O3 -G7 -Qrcd -c -S fastSum.cpp
// 332 floating-point operations
// change:
// 1. remove push at begin and pop at end
// 2. mov ebx,esp -> mov ebx,ebp at begin
// 3. mov esp,ebx -> mov ebp,ebx at end
// 4. remove and esp,-8
// 5. add "fsub HALF" after fabs, fmul
//
// %15     ebp-184   ebp-40
// %14 <-> ebp-208   ebp-24
// %13     ebp-160   ebp-48
// %12     ebp-136   ebp-56
// %11     ebp-96    ebp-72
// %10     ebp-80
// %9      ebp-64
// %8      ebp-16
// %7      ebp-8
// %6      ebp-32,ebp-152,ebp-128
{
  if (SSE2Available) {
    __asm {
      mov     ebx, ebp                                      ;63.1
      mov     ebp, esp                                      ;63.1
      and     ebp, -8
      mov     edx, DWORD PTR [ebx+16]                       ;57.6
      mov     ecx, DWORD PTR [ebx+36]                       ;57.6
      fstcw    WORD PTR [ebp-2];
      or      WORD PTR [ebp-2],1024+2048
      fldcw    WORD PTR [ebp-2];        //Set rounding mode to truncate
      mov     eax, DWORD PTR [ebx+32];
      movhpd    xmm0,QWORD PTR [ecx];
      movlpd    xmm0,QWORD PTR [ecx+8];      xmm0= i , j
      fild    DWORD PTR NPREP;          y
      fld      QWORD PTR [ebx+24];        l , y
      fld      QWORD PTR [ebx+8];        k , l , y
      fld      QWORD PTR TWO_PI;          2pi , k , l , y
      fdiv    st(2),st;              2pi , k , l/2pi , y
      fdivp;                    k/2pi , l/2pi , y
      mov      ecx,DWORD PTR [ebx+20]
      sub      ecx,edx
      shr      ecx,4;      ecx=ecx/16
      fld    QWORD PTR [edx];
      mov    edi,16
      sub    ebp,32
      ALIGN     4
                                ; LOE eax edx ecx
$B1$2:                          ; Preds .B1.6 .B1.1
      fmul    st,st(1);              a*k~ , k~ , l~ , y~
      fld      st(0);                a*k~ , a*k~ , k~ , l~ , y~
      frndint;
      fsubp;                    a*k~ mod 1 , l~ , y~
      movlpd    xmm2,QWORD PTR [edx+8];      xmm2= b , _
      fsub    st(0),st(2);            (a*k~ mod 1)-l~, k~ , l~ , y~
      fabs;                    abs((a*k~ mod 1)-l~) , k~ , l~ , y~
      fmul    st(0),st(3);            y~*abs((a*k~ mod 1)-l~) , k~ , l~ , y~
      fistp    DWORD PTR [ebp];          k~ , l~ , y~
      shufpd    xmm2,xmm2,0;            xmm2= b , b
      mov      esi,DWORD PTR [ebp]
      fld      QWORD PTR [edx+edi];          a , k~ , l~ , y~
      add      esi,esi
      movapd    xmm1,XMMWORD PTR [eax+esi*8];      xmm1= B , A
      add     edx,edi
      mulpd    xmm1,xmm2;              xmm1= B*b , A*b
      addpd    xmm0,xmm1;              xmm0= i+B*b , j+A*b
      loop  $B1$2
      mov    ecx,DWORD PTR [ebx+36]
      movhpd  QWORD PTR [ecx],xmm0;
      movlpd  QWORD PTR [ecx+8],xmm0;
      finit
      mov       ebp, ebx
    }
  } else {         //FP only version
    __asm {
      mov     ebx, ebp                                      ;63.1
      mov     ebp, esp                                      ;63.1
      and     ebp, -8
      mov     edx, DWORD PTR [ebx+16]                       ;57.6
      mov     ecx, DWORD PTR [ebx+36]                       ;57.6
      fstcw    WORD PTR [ebp-2];
      or      WORD PTR [ebp-2],1024+2048
      fldcw    WORD PTR [ebp-2];        //Set rounding mode to truncate
      mov     eax, DWORD PTR [ebx+32];
      fld      QWORD PTR [ecx];          i
      fld      QWORD PTR [ecx+8];          j , i
      fild    DWORD PTR NPREP;
      fld      QWORD PTR [ebx+24];          l , y , j , i
      fld      QWORD PTR [ebx+8];          k , l , y , j , i
      fld      QWORD PTR TWO_PI;          2pi , k , l , y , j , i
      fdiv    st(2),st;              2pi , k , l/2pi , y , j , i
      fdivp;                    k/2pi , l/2pi , y , j , i
      mov      ecx,DWORD PTR [ebx+20]
      sub    ebp,32
      push  ebx
      mov    ebx,64
      ALIGN     4
                                ; LOE eax edx ecx
$B1$3:                          ; Preds .B1.6 .B1.1
      fld      QWORD PTR [edx];          a , k~ , l~ , y~ , j , i
      fmul    st,st(1);              a*k~ , k~ , l~ , y~ , j , i
      fld      st(0);                a*k~ , a*k~ , k~ , l~ , y~ , j , i
      frndint;
      fsubp;                    a*k~ mod 1 , l~ , y~ , j , i
      fsub    st(0),st(2);            (a*k~ mod 1)-l~, k~ , l~ , y~ , j , i
      fabs;                    abs((a*k~ mod 1)-l~) , k~ , l~ , y~ , j , i
      fmul    st(0),st(3);            y~*abs((a*k~ mod 1)-l~) , k~ , l~ , y~ , j , i
      fistp    DWORD PTR [ebp];          k~ , l~ , y~ , j~ , i
      mov      esi,DWORD PTR [ebp]
      add      esi,esi
      fld      QWORD PTR [edx+8];          b , k~ , l~ , y~ , j , i
      fld      QWORD PTR [eax+esi*8];        A , b , k~ , l~ , y~ , j , i
      fmul    st,st(1);              A*b , b , k~ , l~ , y~ , j , i
      faddp    st(5),st;              b , k~ , l~ , y~ , j+A*b , i
      fmul    QWORD PTR [eax+esi*8+8];      B*b , k~ , l~ , y~ , j , i
      faddp    st(5),st;              k~ , l~ , y~ , j , i+B*b
      fld      QWORD PTR [edx+16];          c , k~ , l~ , y~ , j , i
      fmul    st,st(1);              c*k~ , k~ , l~ , y~ , j , i
      fld      st(0);                c*k~ , c*k~ , k~ , l~ , y~ , j , i
      frndint;
      fsubp;                    c*k~ mod 1 , k~ , l~ , y~ , j , i
      fsub    st(0),st(2);            (c*k~ mod 1)-l~ , k~ , l~ , y~ , j , i
      fabs;                    abs((c*k~ mod 1)-l~) , k~ , l~ , y~ , j , i
      fmul    st(0),st(3);            y~*abs((c*k~ mod 1)-l~) , k~ , l~ , y~ , j , i
      fistp    DWORD PTR [ebp];          k~ , l~ , y~ , j , i
      mov      esi,DWORD PTR [ebp]
      add      esi,esi
      fld      QWORD PTR [edx+24];          d , k~ , l~ , y~ , j , i
      mov      edi,DWORD PTR [edx+32];        //alternative prefetch
      ;prefetcht1    BYTE PTR [edx+32];
      fld      QWORD PTR [eax+esi*8];        C , d , k~ , l~ , y~ , j , i
      fmul    st,st(1);              C*d , d , k~ , l~ , y~ , j , i
      faddp    st(5),st;              d , k~ , l~ , y~ , j+C*d , i
      fmul    QWORD PTR [eax+esi*8+8];      D*d , k~ , l~ , y~ , j , i
      faddp    st(5),st;              k~ , l~ , y~ , j , i+D*d
      fld      QWORD PTR [edx+32];          e , k~ , l~ , y~ , j , i
      fmul    st,st(1);              e*k~ , k~ , l~ , y~ , j , i
      fld      st(0);
      frndint;
      fsubp;                    e*k~ mod 1, k~ , l~ , y~ , j , i
      fsub    st(0),st(2);            (e*k~ mod 1)-l~ , k~ , l~ , y~ , j , i
      fabs;                    abs((e*k~ mod 1)-l~) , k~ , l~ , y~ , j , i
      fmul    st(0),st(3);            y~*abs((e*k~ mod 1)-l~) , k~ , l~ , y~ , j , i
      fistp    DWORD PTR [ebp];          k~ , l~ , y~ , j , i
      mov      esi,DWORD PTR [ebp]
      add      esi,esi
      fld      QWORD PTR [edx+40];          f , k~ , l~ , y~ , j , i
      fld      QWORD PTR [eax+esi*8];        E , f , k~ , l~ , y~ , j , i
      fmul    st,st(1);              E*f , f , k~ , l~ , y~ , j , i
      faddp    st(5),st;              f , k~ , l~ , y~ , j+E*f , i
      fmul    QWORD PTR [eax+esi*8+8];      F*f , k~ , l~ , y~ , j , i
      faddp    st(5),st;              k~ , l~ , y~ , j , i+F*f
      fld      QWORD PTR [edx+48];          g , k~ , l~ , y~ , j , i
      fmul    st,st(1);              g*k~ , k~ , l~ , y~ , j , i
      fld      st(0);
      frndint;
      fsubp;                    g*k~ mod 1 , k~ , l~ , y~ , j , i
      fsub    st(0),st(2);            (g*k~ mod 1)-l~ , k~ , l~ , y~ , j , i
      fabs
      fmul    st(0),st(3)
      fistp   DWORD PTR [ebp];
      mov     esi,DWORD PTR [ebp]
      add     esi,esi
      fld      QWORD PTR [edx+56];          h , k~ , l~ , y~ , j , i
      mov      edi,DWORD PTR [edx+64];        //alternative prefetch
      ;prefetcht1    BYTE PTR [edx+64];
      fld      QWORD PTR [eax+esi*8];        G , h , k~ , l~ , y~ , j , i
      fmul    st,st(1);              G*h , h , k~ , l~ , y~ , j , i
      faddp    st(5),st;              h , k~ , l~ , y~ , j+G*h , i
      fmul    QWORD PTR [eax+esi*8+8];      H*h ,  k~ , l~ , y~ , j , i
      faddp    st(5),st;              k~ , l~ , y~ , j , i+H*h
      add     edx,ebx                                       ;114.5
      cmp     edx,ecx; DWORD PTR [ebx+20]                       ;115.16
      jb      $B1$3         ; Prob 99%                      ;115.16
      pop ebx
      mov    ecx,DWORD PTR [ebx+36]
      fxch  st(4);                i , l~ , y~ , j , k~
      fst    QWORD PTR [ecx];
      fxch  st(3);
      fst    QWORD PTR [ecx+8];
      finit
      mov       ebp, ebx
    }
  }
}

static bool isSSE2Available()
{
  int SSE2AvailableLocal = 0;
  __asm {
    pushfd
    pop eax
    xor eax,0x200000
    mov ebx,eax
    push eax
    popfd
    pushfd
    pop eax
    cmp eax,ebx
    jne $nosse2
    xor eax,0x200000      //CPUID is available
    push eax
    popfd
    /*xor  eax,eax
    cpuid
    cmp ebx,0x756e6547    //"Genu"
    jne $nosse2
    cmp edx,0x49656e69    //"ineI"
    jne $nosse2
    cmp ecx,0x6c65746e    //"ntel"
    jne $nosse2*/
    mov eax,1
    cpuid
    and edx,0x6000000
    cmp edx,0x6000000
    jne $nosse2
    mov eax,1
    mov DWORD PTR SSE2AvailableLocal,eax
$nosse2:
  }
  return (SSE2AvailableLocal != 0);
}
#elif _M_IX86 >= 300 && defined(_MSC_VER)
static void fastSumZ(double t, const lnSqrt* p, const lnSqrt* q, double thetaMod, const double* cosValue, double* zLowHigh)
//; parameter 1: 8 + ebx
//; parameter 2: 16 + ebx
//; parameter 3: 20 + ebx
//; parameter 4: 24 + ebx
//; parameter 5: 32 + ebx
//; parameter 6: 36 + ebx
// icl -O3 -G7 -Qrcd -c -S fastSum.cpp
// 332 floating-point operations
// change:
// 1. remove push at begin and pop at end
// 2. mov ebx,esp -> mov ebx,ebp at begin
// 3. mov esp,ebx -> mov ebp,ebx at end
// 4. remove and esp,-8
// 5. add "fsub HALF" after fabs, fmul
//
// %15     ebp-184   ebp-40
// %14 <-> ebp-208   ebp-24
// %13     ebp-160   ebp-48
// %12     ebp-136   ebp-56
// %11     ebp-96    ebp-72
// %10     ebp-80
// %9      ebp-64
// %8      ebp-16
// %7      ebp-8
// %6      ebp-32,ebp-152,ebp-128
{
  __asm {
        mov       ebx, ebp                                      ;63.1
        mov       ebp, DWORD PTR [ebx+4]                        ;63.1
        mov       ebp, esp                                      ;63.1
        and       ebp, -8
        fld       QWORD PTR [ebx+8]                             ;57.6
        mov       edx, DWORD PTR [ebx+16]                       ;57.6
        fld       QWORD PTR [ebx+24]                            ;57.6
        mov       ecx, DWORD PTR [ebx+32]                       ;57.6
        mov       eax, DWORD PTR [ebx+36]                       ;57.6
        fstp      st(0)                                         ;57.6
        fstp      st(0)                                         ;57.6
        ALIGN     4
                                ; LOE eax edx ecx
$B1$2:                          ; Preds .B1.6 .B1.1
        fld       QWORD PTR TWO_PI                              ;71.54
        fld       QWORD PTR [edx+8]                             ;66.16
        fld       QWORD PTR [edx+24]                            ;66.41
        fld       QWORD PTR [edx+40]                            ;66.66
        fld       QWORD PTR [edx+56]                            ;66.91
        fld       QWORD PTR [edx]                               ;71.54
        fmul      QWORD PTR [ebx+8]                             ;71.54
        fxch      st(4)                                         ;66.5
        fstp      QWORD PTR [ebp-24]                            ;66.5
        fxch      st(2)                                         ;66.30
        fstp      QWORD PTR [ebp-40]                            ;66.30
        fstp      QWORD PTR [ebp-48]                            ;66.55
        fstp      QWORD PTR [ebp-56]                            ;66.80

        fprem                                                   ;71.54
        fstp      st(1)                                         ;71.54

        fsubr     QWORD PTR [ebx+24]                            ;71.54
        fld       QWORD PTR [edx+16]                            ;72.54
        fmul      QWORD PTR [ebx+8]                             ;72.54
        fstp      QWORD PTR [ebp-72]                            ;72.54
        fabs                                                    ;71.54
        fmul      QWORD PTR GRID_INV                            ;71.64
        fsub      QWORD PTR HALF                                ;71.74
        fistp     DWORD PTR [ebp-32]                            ;71.74
        mov       ecx, DWORD PTR [ebp-32]                       ;71.74
        add       ecx,ecx

        fld       QWORD PTR TWO_PI                              ;72.54
        fld       QWORD PTR [ebp-72]                            ;72.54
        fprem                                                   ;72.54
        fstp      st(1)                                         ;72.54

        fsubr     QWORD PTR [ebx+24]                            ;72.54
        fld       QWORD PTR [edx+32]                            ;73.54
        fmul      QWORD PTR [ebx+8]                             ;73.54
        fstp      QWORD PTR [ebp-80]                            ;73.54
        fabs                                                    ;72.54
        fmul      QWORD PTR GRID_INV                            ;72.64
        fsub      QWORD PTR HALF                                ;72.74
        fistp     DWORD PTR [ebp-32]                            ;72.74
        mov       esi, DWORD PTR [ebp-32]                       ;72.74
        add       esi,esi

        fld       QWORD PTR TWO_PI                              ;73.54
        fld       QWORD PTR [ebp-80]                            ;73.54
        fprem                                                   ;73.54
        fstp      st(1)                                         ;73.54

        fsubr     QWORD PTR [ebx+24]                            ;73.54
        fld       QWORD PTR [edx+48]                            ;74.54
        fmul      QWORD PTR [ebx+8]                             ;74.54
        fstp      QWORD PTR [ebp-64]                            ;74.54
        fabs                                                    ;73.54
        fmul      QWORD PTR GRID_INV                            ;73.64
        fsub      QWORD PTR HALF                                ;73.74
        fistp     DWORD PTR [ebp-32]                            ;73.74
        mov       edi, DWORD PTR [ebp-32]                       ;73.74
        add       edi,edi

        fld       QWORD PTR TWO_PI                              ;74.54
        fld       QWORD PTR [ebp-64]                            ;74.54
        fprem                                                   ;74.54
        fstp      st(1)                                         ;74.54

        fsubr     QWORD PTR [ebx+24]                            ;74.54
        mov       eax, DWORD PTR [ebx+32]                       ;108.16
        fld       QWORD PTR [eax+ecx*8]                         ;108.16
        fld       QWORD PTR [eax+ecx*8+8]                       ;108.43
        fld       QWORD PTR [ebp-24]                            ;112.29
        mov       ecx, DWORD PTR [ebx+36]                       ;112.89
        fld       QWORD PTR [eax+esi*8+8]                       ;109.43
        fld       QWORD PTR [eax+edi*8]                         ;110.16
        fld       QWORD PTR [eax+edi*8+8]                       ;110.43
        fxch      st(5)                                         ;108.5
        fxch      st(4)                                         ;108.32
        fmul      st, st(3)                                     ;112.29
        fxch      st(4)                                         ;113.29
        fmulp     st(3), st                                     ;113.29
        fxch      st(1)                                         ;109.32
        fxch      st(1)                                         ;110.16
        fst       QWORD PTR [ebp-32]                            ;110.16
        fstp      QWORD PTR [ebp-16]                            ;110.16
        fxch      st(3)                                         ;110.32
        fxch      st(4)                                         ;74.54
        fabs                                                    ;74.54
        fmul      QWORD PTR GRID_INV                            ;74.64
        fsub      QWORD PTR HALF                                ;74.74
        fistp     DWORD PTR [ebp-32]                            ;74.74
        fld       QWORD PTR [eax+esi*8]                         ;109.16
        mov       esi, DWORD PTR [ebp-32]                       ;111.17
        add       esi,esi
        fld       QWORD PTR [eax+esi*8]                         ;111.17
        fld       QWORD PTR [eax+esi*8+8]                       ;111.45
        fxch      st(2)                                         ;109.5
        fxch      st(1)                                         ;111.17
        fstp      QWORD PTR [ebp-8]                             ;111.17
        fxch      st(1)                                         ;111.33
        fld       QWORD PTR [ebp-40]                            ;112.49
        fmul      st(5), st                                     ;112.49
        fmulp     st(2), st                                     ;113.49
        fxch      st(4)                                         ;112.5
        faddp     st(3), st                                     ;112.5
        faddp     st(1), st                                     ;113.5
        fld       QWORD PTR [ebp-48]                            ;112.69
        fmul      st(4), st                                     ;112.69
        fmul      QWORD PTR [ebp-16]                            ;113.69
        fxch      st(4)                                         ;112.49
        faddp     st(2), st                                     ;112.49
        faddp     st(3), st                                     ;113.49
        fld       QWORD PTR [ebp-56]                            ;112.89
        fmul      st(2), st                                     ;112.89
        fmul      QWORD PTR [ebp-8]                             ;113.89
        fxch      st(2)                                         ;112.69
        faddp     st(1), st                                     ;112.69
        fadd      QWORD PTR [ecx]                               ;112.89
        fstp      QWORD PTR [ecx]                               ;112.5
        faddp     st(1), st                                     ;113.69
        fadd      QWORD PTR [ecx+8]                             ;113.89
        fstp      QWORD PTR [ecx+8]                             ;113.5
        add       edx, 64                                       ;114.5
        cmp       edx, DWORD PTR [ebx+20]                       ;115.16
        jb        $B1$2         ; Prob 99%                      ;115.16
        mov       ebp, ebx
  }
}

static bool isSSE2Available()
{
  return false;
}
#elif (defined (__i386__) || defined (__i486__)) && __GNUC__ >= 3
double HALF = 0.5;
double CONST_TWO_PI = TWO_PI;
static void fastSumZ(double t, const lnSqrt* p, const lnSqrt* q, double thetaMod, const double* cosValue, double* zLowHigh)
// 1. removle 'DWORD PTR '
// 2. removle 'DWORD PTR '
// 3. change 'mov' to 'movl'
// 4. change '[' to '('
// 5. change ']' to ')'
// 6. change 'ebx' to '%%ebx'
// 7. change 'ebp' to '%%ebp'
// 8. change 'esp' to '%%esp'
// 9. change 'esi' to '%%esi'
// 10. change 'edi' to '%%edi'
// 11. change 'edx' to '%%edx'
// 12. change 'ecx' to '%%ecx'
// 13. change 'eax' to '%%eax'
// 14. change 'sub' to 'subl'
// 15. change 'add' to 'addl'
// 16. change 'cmp' to 'cmpl'
// 17. swap first and second argument
// 18. change for example '(%%eax+%%esi*8+8)' to '8(%%eax,%%esi,8)'
// 19. change 'ALIGN' to '.align'
// 20. change 'st' to '%%st'
// 21. change '$B1$2' to 'B12'
// 22. zLowHigh: 36(%%ebx)
//     cosValue: 32(%%ebx)
//     thetaMod: 24(%%ebx)
//     q:        20(%%ebx)
//     p:        16(%%ebx) -> %%edx
//     t:        8(%%ebx)
{
  double value[10];
  __asm__ __volatile__(
        "fldl       %0                \n"
        "fldl       %3                \n"
        "fstp       %%st(0)           \n"
        "fstp       %%st(0)           \n"
        ".align     4                 \n"
        "B12:                         \n"
        "fldl       CONST_TWO_PI      \n"
        "fldl       8(%%edx)          \n"
        "fldl       24(%%edx)         \n"
        "fldl       40(%%edx)         \n"
        "fldl       56(%%edx)         \n"
        "fldl       (%%edx)           \n"
        "fmull      %0                \n"
        "fxch       %%st(4)           \n"
        "fstpl      %14               \n"
        "fxch       %%st(2)           \n"
        "fstpl      %15               \n"
        "fstpl      %13               \n"
        "fstpl      %12               \n"
        "fprem                        \n"
        "fstp       %%st(1)           \n"
        "fsubrl     %3                \n"
        "fldl       16(%%edx)         \n"
        "fmull      %0                \n"
        "fstpl      %11               \n"
        "fabs                         \n"
        "fmull      GRID_INV          \n"
        "fsubl      HALF              \n"
        "fistpl     %6                \n"
        "movl       %6,%%ecx          \n"
        "addl       %%ecx,%%ecx       \n"
        "fldl       CONST_TWO_PI      \n"
        "fldl       %11               \n"
        "fprem                        \n"
        "fstp       %%st(1)           \n"
        "fsubrl     %3                \n"
        "fldl       32(%%edx)         \n"
        "fmull      %0                \n"
        "fstpl      %10               \n"
        "fabs                         \n"
        "fmull      GRID_INV          \n"
        "fsubl      HALF              \n"
        "fistpl     %6                \n"
        "movl       %6,%%esi          \n"
        "addl       %%esi,%%esi       \n"
        "fldl       CONST_TWO_PI      \n"
        "fldl       %10               \n"
        "fprem                        \n"
        "fstp       %%st(1)           \n"
        "fsubrl     %3                \n"
        "fldl       48(%%edx)         \n"
        "fmull      %0                \n"
        "fstpl      %9                \n"
        "fabs                         \n"
        "fmull      GRID_INV          \n"
        "fsubl      HALF              \n"
        "fistpl     %6                \n"
        "movl       %6,%%edi          \n"
        "addl       %%edi,%%edi       \n"
        "fldl       CONST_TWO_PI      \n"
        "fldl       %9                \n"
        "fprem                        \n"
        "fstp       %%st(1)           \n"
        "fsubrl     %3                \n"
        "movl       %4,%%eax          \n"
        "fldl       (%%eax,%%ecx,8)   \n"
        "fldl       8(%%eax,%%ecx,8)  \n"
        "fldl       %14               \n"
        "movl       %5,%%ecx          \n"
        "fldl       8(%%eax,%%esi,8)  \n"
        "fldl       (%%eax,%%edi,8)   \n"
        "fldl       8(%%eax,%%edi,8)  \n"
        "fxch       %%st(5)           \n"
        "fxch       %%st(4)           \n"
        "fmul       %%st(3),%%st      \n"
        "fxch       %%st(4)           \n"
        "fmulp      %%st,%%st(3)      \n"
        "fxch       %%st(1)           \n"
        "fxch       %%st(1)           \n"
        "fstl       %8                \n"
        "fstpl      %6                \n"
        "fxch       %%st(3)           \n"
        "fxch       %%st(4)           \n"
        "fabs                         \n"
        "fmull      GRID_INV          \n"
        "fsubl      HALF              \n"
        "fistpl     %6                \n"
        "fldl       (%%eax,%%esi,8)   \n"
        "movl       %6,%%esi          \n"
        "addl       %%esi,%%esi       \n"
        "fldl       (%%eax,%%esi,8)   \n"
        "fldl       8(%%eax,%%esi,8)  \n"
        "fxch       %%st(2)           \n"
        "fxch       %%st(1)           \n"
        "fstl       %7                \n"
        "fstpl      %6                \n"
        "fxch       %%st(1)           \n"
        "fldl       %15               \n"
        "fmul       %%st,%%st(5)      \n"
        "fmulp      %%st,%%st(2)      \n"
        "fxch       %%st(4)           \n"
        "faddp      %%st,%%st(3)      \n"
        "faddp      %%st,%%st(1)      \n"
        "fldl       %13               \n"
        "fmul       %%st,%%st(4)      \n"
        "fmull      %8                \n"
        "fxch       %%st(4)           \n"
        "faddp      %%st,%%st(2)      \n"
        "faddp      %%st,%%st(3)      \n"
        "fldl       %12               \n"
        "fmul       %%st,%%st(2)      \n"
        "fmull      %7                \n"
        "fxch       %%st(2)           \n"
        "faddp      %%st,%%st(1)      \n"
        "faddl      (%%ecx)           \n"
        "fstpl      (%%ecx)           \n"
        "faddp      %%st,%%st(1)      \n"
        "faddl      8(%%ecx)          \n"
        "fstpl      8(%%ecx)          \n"
        "addl       $64,%%edx         \n"
        "cmpl       %2,%%edx          \n"
        "jb         B12"
      :
      : "m" (t), "d" (p), "m" (q), "m" (thetaMod), "m" (cosValue), "m" (zLowHigh),
        "m" (value[0]), "m" (value[1]), "m" (value[2]), "m" (value[3]), "m" (value[4]), "m" (value[5]),
        "m" (value[6]), "m" (value[7]), "m" (value[8]), "m" (value[9])
      : "eax", "ebx", "ecx", "edi", "esi", "st", "st(1)", "st(2)", "st(3)", "st(4)", "st(5)", "st(6)", "memory", "cc"
    );
}

static bool isSSE2Available()
{
  return false;
}

#else
static void fastSumZ(double t, const lnSqrt* p, const lnSqrt* q, double thetaMod, const double* cosValue, double* zLowHigh)
{
  double value[12];
  do {
    value[0] = p[0].sqrtinv; value[3] = p[1].sqrtinv; value[6] = p[2].sqrtinv; value[9] = p[3].sqrtinv;
    const int idx0 = fastInnerSumZ(thetaMod, t*p[0].ln);
    const int idx1 = fastInnerSumZ(thetaMod, t*p[1].ln);
    const int idx2 = fastInnerSumZ(thetaMod, t*p[2].ln);
    const int idx3 = fastInnerSumZ(thetaMod, t*p[3].ln);
    value[1] = cosValue[idx0]; value[2] = cosValue[idx0+1];
    value[4] = cosValue[idx1]; value[5] = cosValue[idx1+1];
    value[7] = cosValue[idx2]; value[8] = cosValue[idx2+1];
    value[10] = cosValue[idx3]; value[11] = cosValue[idx3+1];
    zLowHigh[0] += value[0]*value[2] + value[3]*value[5] + value[6]*value[8] + value[9]*value[11];
    zLowHigh[1] += value[0]*value[1] + value[3]*value[4] + value[6]*value[7] + value[9]*value[10];
    p += 4;
  } while (p < q);
}

static bool isSSE2Available()
{
  return false;
}
#endif


static void fastSumZSleep(double t, const lnSqrt* p, const lnSqrt* q, double thetaMod, const double* cosValue, double* zLowHigh)
{
  double value[12];
  do {
    value[0] = p[0].sqrtinv; value[3] = p[1].sqrtinv; value[6] = p[2].sqrtinv; value[9] = p[3].sqrtinv;
    const int idx0 = fastInnerSumZ(thetaMod, t*p[0].ln);
    const int idx1 = fastInnerSumZ(thetaMod, t*p[1].ln);
    const int idx2 = fastInnerSumZ(thetaMod, t*p[2].ln);
    const int idx3 = fastInnerSumZ(thetaMod, t*p[3].ln);
    value[1] = cosValue[idx0]; value[2] = cosValue[idx0+1];
    value[4] = cosValue[idx1]; value[5] = cosValue[idx1+1];
    value[7] = cosValue[idx2]; value[8] = cosValue[idx2+1];
    value[10] = cosValue[idx3]; value[11] = cosValue[idx3+1];
    zLowHigh[0] += value[0]*value[2] + value[3]*value[5] + value[6]*value[8] + value[9]*value[11];
    zLowHigh[1] += value[0]*value[1] + value[3]*value[4] + value[6]*value[7] + value[9]*value[10];
    sleepCounter += 4;
    if (sleepCounter >= sleepMode) {
      _sleep(0);
      sleepCounter = 0;
    }
    p += 4;
  } while (p < q);
}

EvalZeta::EvalZeta(Output* o)
{
  ++FreeSqrtinvMem::active; // Thread-safe
  output = o;
  sleepCounter = 0;
  ndzevalu = nzevalu = 0;
  dztime = ztime = 0;
  sqrtinvLn = 0;
  lnDD = 0;
  sqrtinvDD = 0;
  running = this;
  SSE2Available = isSSE2Available();
}

void EvalZeta::sumZ(double t, int m, double thetaMod, double* zLowHigh)
{
  const lnSqrt* q = sqrtinvLn+m;
  switch (m&3) {
    case 0: {
              const int idx = fastInnerSumZ(thetaMod, t*q->ln);
              zLowHigh[0] += q->sqrtinv*cosValue[idx+1];
              zLowHigh[1] += cosValue[idx]*q->sqrtinv;
              --q;
            }
    case 3: {
              const int idx = fastInnerSumZ(thetaMod, t*q->ln);
              zLowHigh[0] += q->sqrtinv*cosValue[idx+1];
              zLowHigh[1] += cosValue[idx]*q->sqrtinv;
              --q;
            }
    case 2: {
              const int idx = fastInnerSumZ(thetaMod, t*q->ln);
              zLowHigh[0] += q->sqrtinv*cosValue[idx+1];
              zLowHigh[1] += cosValue[idx]*q->sqrtinv;
            }
    default: break;
  }
  if (sqrtinvLn+2 < q) {
    if (sleepMode > 0) {
      fastSumZSleep(t, sqrtinvLn+2, q, thetaMod, cosValue, zLowHigh);
    } else {
      fastSumZ(t, sqrtinvLn+2, q, thetaMod, cosValue, zLowHigh);
    }
  }
  zLowHigh[0] *= 2; zLowHigh[1] *= 2;
}

void EvalZeta::sumDZ(double t, int m, double thetaMod, double* zLowHigh)
// checkmin{c1, c2} < cos(theta-t*ln(i)) < max{c1, c2}
{
  const double tHigh = high(t);
  const double tLow = low(t);
  for (int i = 2; i <= m; ++i) {
    double c1 = posFastMod2Pi(tHigh*sqrtinvLn[i].ln);
    double c2 = posFastMod2Pi(tLow*sqrtinvLn[i].ln);
    c1 = cos(thetaMod-c1);
    c2 = cos(thetaMod-c2);
    if (c1 < c2) {
      zLowHigh[0] += c1*low(sqrtinvLn[i].sqrtinv);
      zLowHigh[1] += c2*high(sqrtinvLn[i].sqrtinv);
    } else {
      zLowHigh[0] += c2*low(sqrtinvLn[i].sqrtinv);
      zLowHigh[1] += c1*high(sqrtinvLn[i].sqrtinv);
    }
  }
  zLowHigh[0] *= 2; zLowHigh[1] *= 2;
}

void EvalZeta::sumDZSleep(double t, int m, double thetaMod, double* zLowHigh)
// checkmin{c1, c2} < cos(theta-t*ln(i)) < max{c1, c2}
{
  const double tHigh = high(t);
  const double tLow = low(t);
  for (int i = 2; i <= m; ++i) {
    if (++sleepCounter >= sleepMode) {
      _sleep(0);
      sleepCounter = 0;
    }
    double c1 = posFastMod2Pi(tHigh*sqrtinvLn[i].ln);
    double c2 = posFastMod2Pi(tLow*sqrtinvLn[i].ln);
    c1 = cos(thetaMod-c1);
    c2 = cos(thetaMod-c2);
    if (c1 < c2) {
      zLowHigh[0] += c1*low(sqrtinvLn[i].sqrtinv);
      zLowHigh[1] += c2*high(sqrtinvLn[i].sqrtinv);
    } else {
      zLowHigh[0] += c2*low(sqrtinvLn[i].sqrtinv);
      zLowHigh[1] += c1*high(sqrtinvLn[i].sqrtinv);
    }
  }
  zLowHigh[0] *= 2; zLowHigh[1] *= 2;
}

void EvalZeta::sumDD(double t, int m, const doubledouble& theta, doubledouble& zLow, doubledouble& zHigh)
{
  for (int i = 2; i <= m; ++i) {
    if (sleepMode > 0 && ++sleepCounter >= sleepMode) {
      _sleep(0);
      sleepCounter = 0;
    }
    doubledouble d = lnDD[i];
    d *= t; d -= theta; d = cos(d);
    d *= sqrtinvDD[i];
    zLow += d; zHigh += d;
  }
  zLow *= 2.0; zHigh *= 2.0;
}

void EvalZeta::sumDD(const doubledouble& t, int m, const doubledouble& theta, doubledouble& zLow, doubledouble& zHigh)
{
  for (int i = 2; i <= m; ++i) {
    if (sleepMode > 0 && ++sleepCounter >= sleepMode) {
      _sleep(0);
      sleepCounter = 0;
    }
    doubledouble d = lnDD[i];
    d *= t; d -= theta; d = cos(d);
    d *= sqrtinvDD[i];
    zLow += d; zHigh += d;
  }
  zLow *= 2.0; zHigh *= 2.0;
}

/*
#include "qd.h"

void EvalZeta::sumQD(const doubledouble& t, int m, const doubledouble& theta, doubledouble& zLow, doubledouble& zHigh)
{
  qd_real result = 0.0;
  for (int i = 2; i <= m; ++i) {
    qd_real k(i);
    qd_real d = log(k);
    d *= t; d -= theta; d = cos(d);
    k = sqrt(k);
    d /= k;
    result += k;
  }
  zHigh += result;
  zLow += result;
  zLow *= 2.0; zHigh *= 2.0;
}
*/

void EvalZeta::evalZ(double t, double& zLow, double& zHigh)
{
  clock_t time = clock();
  doubledouble tau = TWO_PI_INV_DD*t;
  doubledouble rho = sqrt(tau);
  int m = int(rho);
  if (tau < doubledouble(m)*m) { // We now determine m such that m^2 <= tau < (m+1)^2.
    --m; // This might happen if tau has the form (k^2)*(1-epsilon), m=k.
  } else if (tau >= doubledouble(m+1)*(m+1)) {
    ++m; // This might happen if tau has the form (k^2)*(1+epsilon), m=k-1.
  }
  // Here we have used the notation of Haselgrove and Miller.
  doubledouble thetaDD;
  theta(t, thetaDD);
  doubledouble thetaModDD = thetaDD;
  thetaModDD *= TWO_PI_INV_DD; thetaModDD = floor(thetaModDD); thetaModDD *= TWO_PI_DD;
  thetaDD -= thetaModDD;
  zLow = cos(double(thetaDD));
  double d[2];
  d[0] = low(zLow); d[1] = high(zLow);
  sumZ(t, m, double(thetaDD), d);
  zLow = d[0]; zHigh = d[1];
  doubledouble errorLow,errorHigh;
  errorComputation(tau, rho, m, errorLow, errorHigh);
  if ((m&1) == 0) {
    zHigh -= double(errorLow); zLow -= double(errorHigh);
  } else {
    zHigh += double(errorHigh); zLow += double(errorLow);
  }
  ztime += clock()-time; ++nzevalu;
  if (fabs(zLow) > 190.0) {
    static double lastLargeValueAt = 0.0;
    if (fabs(lastLargeValueAt-t) > 1.0) {
      LOGLN(*output, ".... Large value at " << t << ": " << zLow);
    }
    lastLargeValueAt = t;
  }
}

void EvalZeta::evalDZ(double t, double& zLow, double& zHigh)
{
  clock_t time = clock();
  doubledouble tau = TWO_PI_INV_DD*t;
  doubledouble rho = sqrt(tau);
  int m = int(rho);
  if (tau < doubledouble(m)*m) { // We now determine m such that m^2 <= tau < (m+1)^2.
    --m; // This might happen if tau has the form (k^2)*(1-epsilon), m=k.
  } else if (tau >= doubledouble(m+1)*(m+1)) {
    ++m; // This might happen if tau has the form (k^2)*(1+epsilon), m=k-1.
  }
  // Here we have used the notation of Haselgrove and Miller.
  doubledouble thetaDD;
  theta(t, thetaDD);
  doubledouble sdLow = cos(thetaDD);
  doubledouble sdHigh = high(double(sdLow));
  sdLow = low(double(sdLow));
  LOG(*output, ".... Call sumMZ at " << t);
  sumDD(t, m, thetaDD, sdLow, sdHigh);
  doubledouble errorLow,errorHigh;
  errorComputation(tau, rho, m, errorLow, errorHigh);
  if ((m&1) == 0) {
    sdHigh -= errorLow; sdLow -= errorHigh;
  } else {
    sdHigh += errorHigh; sdLow += errorLow;
  }
  zLow = double(sdLow); zHigh = double(sdHigh);
  LOGLN(*output, ", MZ=" << sdLow << ',' << sdHigh);
  dztime += clock()-time; ++ndzevalu;
}

void EvalZeta::evalDZ(const doubledouble& t, double& zLow, double& zHigh)
{
  clock_t time = clock();
  doubledouble tau = TWO_PI_INV_DD*t;
  doubledouble rho = sqrt(tau);
  int m = int(rho);
  if (tau < doubledouble(m)*m) { // We now determine m such that m^2 <= tau < (m+1)^2.
    --m; // This might happen if tau has the form (k^2)*(1-epsilon), m=k.
  } else if (tau >= doubledouble(m+1)*(m+1)) {
    ++m; // This might happen if tau has the form (k^2)*(1+epsilon), m=k-1.
  }
  // Here we have used the notation of Haselgrove and Miller.
  doubledouble thetaDD;
  theta(t, thetaDD);
  doubledouble sdLow = cos(thetaDD);
  doubledouble sdHigh = high(double(sdLow));
  sdLow = low(double(sdLow));
  LOG(*output, ".... Call sumMZ at " << t);
  sumDD(t, m, thetaDD, sdLow, sdHigh);
  doubledouble errorLow,errorHigh;
  errorComputation(tau, rho, m, errorLow, errorHigh);
  if ((m&1) == 0) {
    sdHigh -= errorLow; sdLow -= errorHigh;
  } else {
    sdHigh += errorHigh; sdLow += errorLow;
  }
  zLow = double(sdLow); zHigh = double(sdHigh);
  LOGLN(*output, ", MZ=" << sdLow << ',' << sdHigh);
  dztime += clock()-time; ++ndzevalu;
}

void EvalZeta::errorComputation(const doubledouble& tau, const doubledouble& rho, int m, doubledouble& errorLow, doubledouble& errorHigh)
{
  doubledouble drksi = rho;
  drksi -= m; drksi *= 2.0; drksi -= 1.0; drksi = low(drksi);
  doubledouble drksisq = low(sqr(drksi));
  doubledouble ddphi0 = DC0_LOW[DCI_SIZE-1];
  doubledouble ddphi1 = DC1_HIGH[DCI_SIZE-1];
  doubledouble ddphi2 = DC2_LOW[DCI_SIZE-1];
  doubledouble ddphi3 = DC3_HIGH[DCI_SIZE-1];
  int i;
  for (i = DCI_SIZE-2; i >= 0; --i) {
    ddphi0 *= drksisq; ddphi0 += DC0_LOW[i];
    ddphi1 *= drksisq; ddphi1 += DC1_HIGH[i];
    ddphi2 *= drksisq; ddphi2 += DC2_LOW[i];
    ddphi3 *= drksisq; ddphi3 += DC3_HIGH[i];
  }
  doubledouble drhoinv(low(recip(rho)));
  drksi *= drhoinv;
  const doubledouble tauHigh = high(tau);
  ddphi1 *= drksi; ddphi2 /= tauHigh;
  ddphi3 *= drksi; ddphi3 /= tauHigh;
  errorLow = ddphi0;
  errorLow -= ddphi1; errorLow += ddphi2; errorLow -= ddphi3;
  errorLow *= low(sqrt(drhoinv));

  drksi = rho; drksi -= m; drksi *= 2.0; drksi -= 1.0; drksi = high(drksi);
  drksisq = high(sqr(drksi));
  ddphi0 = DC0_HIGH[DCI_SIZE-1];
  ddphi1 = DC1_LOW[DCI_SIZE-1];
  ddphi2 = DC2_HIGH[DCI_SIZE-1];
  ddphi3 = DC3_LOW[DCI_SIZE-1];
  for (i = DCI_SIZE-2; i >= 0; --i) {
    ddphi0 *= drksisq; ddphi0 += DC0_HIGH[i];
    ddphi1 *= drksisq; ddphi1 += DC1_LOW[i];
    ddphi2 *= drksisq; ddphi2 += DC2_HIGH[i];
    ddphi3 *= drksisq; ddphi3 += DC3_LOW[i];
  }
  drhoinv = high(recip(rho));
  drksi *= drhoinv;
  const doubledouble tauLow = low(tau);
  ddphi1 *= drksi; ddphi2 /= tauLow;
  ddphi3 *= drksi; ddphi3 /= tauLow;
  errorHigh = ddphi0; errorHigh -= ddphi1; errorHigh += ddphi2; errorHigh -= ddphi3;
  errorHigh *= high(sqrt(drhoinv));
}

double EvalZeta::evalZ(double tau1, double& tau2)
// The Riemann-Siegel formula (on sigma=1/2) for tau2
// Shift the value tau2 in direction to tau1, if |Z(tau2)| is smaller than EPS.
{
  double z = evalZ(tau2);
  if (z != 0) return z;
  z = evalDZ(tau2);
  if (z != 0) return z;
  for (int nz = 1; nz <= 10; ++nz) {
    LOG(*output, ".... We make a shift at " << tau2);
    tau2 = tau1+(tau2-tau1)*0.99;
    z = evalZ(tau2);
    LOGLN(*output, "....to" << tau2);
    if (z != 0) return z;
    z = evalDZ(tau2);
    if (z != 0) return z;
  }
  LOGLN(*output, "Serious difficulties in finding a good dz. We stop!");
  exit(1);
  return z;
}

bool EvalZeta::init(startType LASTN, int NRANGE)
// Preparations
{
  int i,j;
  // nzevalu is set to 0 and hence "ecorate" is measured locally.
  ndzevalu = nzevalu = 0; dztime = ztime = 0;
  time_t t;
  time(&t);
  tm* gmt = gmtime(&t);
  const startType nmax = LASTN+(NRANGE+10);
  const int MDIMENS = max(int(10+sqrt(Gram(nmax, (TWO_PI_HIGH*nmax)/log(double(nmax)))*TWO_PI_INV_HIGH)), 500);
  LOG(*output, "This run (LASTN=" << LASTN <<", NRANGE=" << NRANGE << ", MDIMENS=" << MDIMENS << ", VERSION=" << VERSION << ") was started at " << asctime(gmt));
  sqrtinvMem.get(MDIMENS, sqrtinvLn, lnDD, sqrtinvDD);

  if (cosValue[2*NPREP+1] != 0.0) { // short path (tables initialized)
    return true;
  }

  TWO_PI_DD = 2.0*doubledouble::Pi;
  TWO_PI_INV_DD = recip(TWO_PI_DD);

  // The arrays DCI(30) can easily be extended to DCI (>30).
  // See: High precision coefficients related to the zeta function
  // by F.D. Crary and J.Barkley Rosser (Univ.Wisconsin, Report#1344).
  // Also see: W.Gabcke, Dissertation, Gottingen, 1979.

  // Maple:       evalf(series(cos(Pi/8*(4*z^2+3))/cos(Pi*z), z=0,78));
  // Mathematica: N[Series[Cos[Pi/8*(4*z^2+3)]/Cos[Pi*z], {z,0,78}], 100]
  DC0[0] = "+0.382683432365089771728459984030398866761344562485627041433800635627546033960089692237013785342283547148424288661";
  DC0[1] = "+0.437240468077520449360296467371331987073041501042363031866378610690404508264026362304924073435669639094324286079";
  DC0[2] = "+0.1323765754803435233240352673915105554743229955586736726649426880559566435420738501669778293204183675017943241749";
  DC0[3] = "-0.01360502604767418865498318870909990766070687027421894648326187950903716176818633068962485278356136895679047020906";
  DC0[4] = "-0.01356762197010358088791567058349920618602959696188054686917829168674934087357724499903115819721107719785647775895";
  DC0[5] = "-0.001623725323144465282854625294133649725659201718172548305398980308243971457730734393999432889383665122685176399873";
  DC0[6] = "+0.0002970535373337969078312728339951586690679333334505619667872265698183319369203982805177634566104207720143400458262";
  DC0[7] = "+0.0000794330087952146958801639026487950144873099152560321495071752950226903173395878801705671344477374930716659999123";
  DC0[8] = "+4.65561246145045050370634021603476231240414569015306013316932838935348879193481820928464518626366832313236741193e-7";
  DC0[9] = "-1.432725163095510575408246312062615888246258029228068660731581897805436577213074435556392472287142822769748329508e-6";
  DC0[10] = "-1.035484711231294607500741567738403498882724615888283179447948653075217984108710847647177577896120460086478764151e-7";
  DC0[11] = "+1.235792708386173805612576262312530316510117762098112867236451610214808402930282161805187583832632897001667458326e-8";
  DC0[12] = "+1.788108385795490498566678140706904566454558839254644214800304281524529021049731788926650722192519086137173526827e-9";
  DC0[13] = "-3.391414389927035906940621897884455615248397316288972845006785754803502806891904570703993463969380667640255995188e-11";
  DC0[14] = "-1.632663390256590510137405297104810281346405431822126827413147731265198687738077289595025493824604361493389459115e-11";
  DC0[15] = "-3.78510931854122038285464720018504502639038535531200438309272315224985279176248298240356571256856962726814475742e-13";
  DC0[16] = "+9.32742325920172484566232063986986360002139698116915631937705187400020105037249065099538843442588188664675147467e-14";
  DC0[17] = "+5.22184301597813685531389314785302371037675394827206477433177228406371981681645773099043136444005380798369532226e-15";
  DC0[18] = "-3.350673072744263789515090357947326053042838022398065784111555892808740716677888930078701970602503964936006395104e-16";
  DC0[19] = "-3.412426522811726494080987104562058778608283456254096935869001796289917525710367617748381397622542683846888672015e-17";
  DC0[20] = "+5.75120334143239916033950179516459231161537829470064807231431453813486388545094592961884811901724585064764962844e-19";
  DC0[21] = "+1.489530136321150545475627775734689089370735009741635415160709048603295381414367364949760330404014020213930338814e-19";
  DC0[22] = "+1.256537271702141685330428176609282176536606717440674080535520482356810764118743819183518519587390237199100704507e-21";
  DC0[23] = "-4.72129525014342566895398813667305340706330304767749675702994903089879756535834541307555978776385327369051553303e-22";
  DC0[24] = "-1.32690693630396199927354130926183589457507684264465236751272493873018443686610022790536188919297368265742721815e-23";
  DC0[25] = "+1.105343999512141834453782254227205003182486780211827086005486828970265089129731688133254857575539823725195683922e-24";
  DC0[26] = "+5.49964637752746551114010449998398178325210538268839922240622794248949462106984664855058759404246021418007498035e-26";
  DC0[27] = "-1.823137650231802628064108980945407064129881555145279779343173540693674436633740912616549635547508188700365680468e-27";
  DC0[28] = "-1.568940373772088014686829823192433140971355533415866343941007167915490728982359363699569882728024654791588147859e-28";
  DC0[29] = "+1.583963508823801161065976053779295278390321164235705734225919985692892191820446152269080950811170989257202082911e-30";
  DC0[30] = "+3.4346207254372040220415367076516947462098483922500503564813783179382274162068573263112671201649692317453615905e-31";
  DC0[31] = "+1.702103350031701775318130755271536920853455319760507766090796235001978260644018065519766552941782676624037679422e-33";
  DC0[32] = "-5.99511930495781673363972633565278514210564502273708131802609647284822583102487894658236367081899707846320368051e-34";
  DC0[33] = "-1.04876827540944523668427321724480041261517736197636621170912258890766184956298037903925915158527893326057340676e-35";
  DC0[34] = "+8.42213517834932107854815160735950929394240415164747688538014234387628955118839912704841909631604183775608950552e-37";
  DC0[35] = "+2.584703859771955713160011615401679072208538467158450776918252923628843991474698009272117251533209327471142444888e-38";
  DC0[36] = "-9.34763937488998521367903976236981418745369692048179313134504611480555205177899495504433350362542373205050278136e-40";
  DC0[37] = "-4.56941922524370129765254861055742309974472681838538268606496265105749673629290338481038140628402537476198769926e-41";
  DC0[38] = "+7.5455973947653905212085611627422817804352042098369215797696318278534625566582905601529992674332954823406110951e-43";

  // Maple:       evalf(series(diff(cos(Pi/8*(4*z^2+3))/cos(Pi*z), z$3)/(12*Pi^2), z=0,78));
  // Mathematica: N[Series[D[Cos[Pi/8*(4*z^2+3)]/Cos[Pi*z], {z,3}]/(12*Pi^2), {z,0,78}], 80]
  DC1[0] = "+0.026825102628375347029991403955666749659270472430643220749776752805867359134188269214280683052";
  DC1[1] = "-0.013784773426351853049870452589896162365948225597532513294465798867691807541688152085576031383";
  DC1[2] = "-0.03849125048223508222873641536318936689609880749450906478327860153836847426887901787229257659";
  DC1[3] = "-0.00987106629906207647201214704618854069280421459666950839994786868965757411195190486323582121";
  DC1[4] = "+0.0033107597608584043329090769513006978028020918561175309707694861178448645426345718015500843326";
  DC1[5] = "+0.0014647808577954150824977965619831119780775457722862078933453303143287255048972099659207209621";
  DC1[6] = "+0.000013207940624876963675161447494430967824291835406154372613530235724535011475016069716813294177";
  DC1[7] = "-0.00005922748701847141323223499528189568406802912492160850522823303196158210932726481656944581815";
  DC1[8] = "-5.980242585373448587710835074515858419335890174202825600668342888976410900581355969992952868e-6";
  DC1[9] = "+9.641322456169826352672985329851666875707836639273182783168607374528344107916458627247029283e-7";
  DC1[10] = "+1.8334733722714411760016793657832219080753603339709992766273715660151990962666208683124437739e-7";
  DC1[11] = "-4.467087562717833599560794227150551934657469384377660055067166840151970252514746820463948005e-9";
  DC1[12] = "-2.7096350821772743216926283987091937259316030722995844412753447458314425593181090759150020757e-9";
  DC1[13] = "-7.785288654315851046294823085209610006727820577278800382831566727931867764632910377867374876e-11";
  DC1[14] = "+2.3437626010893688532484550487104512273133964049734205404375275618829488523490151174368404654e-11";
  DC1[15] = "+1.5830172789987521642162226426287421196746979819448013053513508455771578055396438744193146485e-12";
  DC1[16] = "-1.2119941573723791246646344738017572576448530651660888461727279790245362198776996808868093029e-13";
  DC1[17] = "-1.4583781161108307017582854816989993171964777297933249751938379402944425481261805530630616476e-14";
  DC1[18] = "+2.8786305258131917504558212800208760753536483952839345119896953142223473009317304761040685377e-16";
  DC1[19] = "+8.662862902123724122528252887933104042807961436294852178958442068342689422294037865607742406e-17";
  DC1[20] = "+8.43072272713704127156002253146274997727634288682530999309124260276609727264253556324577217e-19";
  DC1[21] = "-3.630807223097346200173246181103281136955871882367415423137780078494511015098068311048451748e-19";
  DC1[22] = "-1.1626698212838296719413888629248324378808802522154009317733711360471269908689980684964899997e-20";
  DC1[23] = "+1.0975486711527531815901832833980075157643671464693844798841337840675643501161428099083592646e-21";
  DC1[24] = "+6.157399020468427103881470790974945857407651738028111860714322669192813619603136094113741154e-23";
  DC1[25] = "-2.2909280067678471513963826309991269307343977096746438696025932017457239554824102031775874946e-24";
  DC1[26] = "-2.2032811748848795343795982704373537544376537775704078003688163099187931890241159108319322546e-25";
  DC1[27] = "+2.476025180040278508285274215182918632587580796467112641727687468810950715262244429469791761e-27";
  DC1[28] = "+5.954277215583657802272682863953454743730467881559988360025759772968743994237177956667902438e-28";
  DC1[29] = "+3.2612020746795952615337563190661874830891135980274339139845795592240636373843954612255006828e-30";
  DC1[30] = "-1.2654035591041162243650179791261536112558798012347744231540654284163522268689446590266925924e-30";
  DC1[31] = "-2.4312846965496981901634636363338047426353348019050697893343812128678861564371131634064246015e-32";
  DC1[32] = "+2.1383011387546953739564195753197140950296216408034685099183785890974590986138498294625891461e-33";
  DC1[33] = "+7.167799413941061690328338683692708749333075099712339753173535550674454000240550835751326567e-35";
  DC1[34] = "-2.8242936072336665615525326594221813904617133033516236406598931104936386534532483771217816036e-36";
  DC1[35] = "-1.5006074196069282189178370454993947599747135926005292679869580497329724359381439103658938812e-37";
  DC1[36] = "+2.687318940531486108260118827557915092685193539193918356025160117503267595083118443609927278e-39";
  DC1[37] = "+2.4904195007933094154169676810430522646696390103991181635576185062669795340798460145976606456e-40";
  DC1[38] = "-1.1605389825678419639763678032899844219760188058814234948874818004278848389077335785169161417e-42";

  // Maple:       evalf(series(diff(cos(Pi/8*(4*z^2+3))/cos(Pi*z), z$2)/(16*Pi^2)+diff(cos(Pi/8*(4*z^2+3))/cos(Pi*z), z$6)/(288*Pi^4), z=0, 38));
  // Mathematica: N[Series[D[Cos[Pi/8*(4*z^2+3)]/Cos[Pi*z], {z,2}]/(16*Pi^2)+D[Cos[Pi/8*(4*z^2+3)]/Cos[Pi*z], {z,6}]/(288*Pi^4), {z,0,78}], 80]
  DC2[0] = "0.005188542830293168493784581519230959565968684337910516563725522452072126841897897";
  DC2[1] = "0.00030946583880634746033456743609587882366950030794878439289358718588031842799211307";
  DC2[2] = "-0.011335941078229373382182435255883513410249474890261540938858667942986863079981311";
  DC2[3] = "0.0022330457419581447720571255275803681570983979981642796951293105725719455469601119";
  DC2[4] = "0.005196637408862330205116926953068191888515832107618595474609879576976564736555503";
  DC2[5] = "0.0003439914407620833669465591357991809598418589002147317484417304617440685730817159";
  DC2[6] = "-0.0005910648427470582821732252303077395276588375610173232690309594523280267574834747";
  DC2[7] = "-0.00010229972547935857454427867522727787133943747273471360465084634256262134151394488";
  DC2[8] = "0.000020888392216992755408073296174175415931186305360443691531185576321472947573577868";
  DC2[9] = "5.927665493096535957891996484982863335742249862441772318721993759532135400499668e-6";
  DC2[10] = "-1.6423838362436275977690302847783780496161212669336910235253514740438053597308203e-7";
  DC2[11] = "-1.5161199700940682861734605397187381660081084155970349774087695236188185367222967e-7";
  DC2[12] = "-5.907803698206667962922790253978962060716281592010072012867608882567102850848194e-9";
  DC2[13] = "2.0911514859478188977745555189722580395885704419004240100593948634461745082130407e-9";
  DC2[14] = "1.7815649583292351053799701878847486656009684349096795930345702159393431817224899e-10";
  DC2[15] = "-1.6164072455353830752855769444473857776802820362311211738301273256347933803317603e-11";
  DC2[16] = "-2.3806962496667615707210740380135849781560242513320681741897741346182257287784557e-12";
  DC2[17] = "5.398265295542594918182004148336822987325682997025679714114209585960532400298625e-14";
  DC2[18] = "1.9750142196969515273308733588451725185221802033544774174325263225894059186101604e-14";
  DC2[19] = "2.3332868732882634831048153005923547597095168404970213704335126869670513579626541e-16";
  DC2[20] = "-1.1187517610048080208200483808971615892736704619952328379264891944667482662087555e-16";
  DC2[21] = "-4.164009488883767188501122836433308161241527720525451717634563860759792248049601e-18";
  DC2[22] = "4.446081109291883028903043500928743324161025016190532684886900866475208961810096e-19";
  DC2[23] = "2.8546114783637144545733874269779565433096490605491768968430556415535467088959418e-20";
  DC2[24] = "-1.19132314300378943049718475052661810410453703351011923973272939551201153470687e-21";
  DC2[25] = "-1.2981634360736498946709902313291029349864968257004043970653583551747007944948404e-22";
  DC2[26] = "1.6123763178033262338779658663222193262651519787452057819826151396265479872321981e-24";
  DC2[27] = "4.382497519887344059655258424644950704152853920980276818591850390318129841119647e-25";
  DC2[28] = "2.7186389576555759138820356271448872774947633995904411012405376808492919628590506e-27";
  DC2[29] = "-1.1458896506774580369743945579295750204092666668699824045661401768984212546138787e-27";
  DC2[30] = "-2.4415318181927522978909188671073094149415439741835244151526434958860319358446682e-29";
  DC2[31] = "2.3505675086790434606664221960472243599181441248526662245549521169413326741733033e-30";
  DC2[32] = "8.669258995621298717800714563004282859479548850038258850838724790295883213896554e-32";
  DC2[33] = "-3.723977985489462680382745552671764711614778698594250205029269305778538368069886e-33";
  DC2[34] = "-2.1646033266321799468220479128490325581663125661515692147696595451120812363930903e-34";
  DC2[35] = "4.203457751935555749203270075437513679883585299088583490129455804307114122286297e-36";
  DC2[36] = "4.244052494804297215797687543359423065727104705085375433170467492601781347640104e-37";
  DC2[37] = "-2.1231392753906157383843052537658427398583379292318669518502402669687755858823324e-39";
  DC2[38] = "-6.813496373118564864349052387612632119903467123774393160507783275039315832205749e-40";

  // Maple:       evalf(series(diff(cos(Pi/8*(4*z^2+3))/cos(Pi*z), z)/(32*Pi^2)+diff(cos(Pi/8*(4*z^2+3))/cos(Pi*z), z$5)/(120*Pi^4)+diff(cos(Pi/8*(4*z^2+3))/cos(Pi*z), z$9)/(10368*Pi^6), z=0, 38));
  // Mathematica: N[Series[D[Cos[Pi/8*(4*z^2+3)]/Cos[Pi*z], z]/(32*Pi^2)+D[Cos[Pi/8*(4*z^2+3)]/Cos[Pi*z], {z,5}]/(120*Pi^4)+D[Cos[Pi/8*(4*z^2+3)]/Cos[Pi*z], {z,9}]/(10368*Pi^6), {z,0,78}], 80]
  DC3[ 0] = "0.0013397160907194569042698357299452281238563539531678386569289925594833800871811798";
  DC3[ 1] = "-0.003744215136379393704664161864462396581284315042446001020488345395501319713045551";
  DC3[ 2] = "0.001330317891932146812031854722402410509897088246099457996340123183515196888263071";
  DC3[ 3] = "0.0022654660765471787114760319905210068874119513448871865405689358390986959711214106";
  DC3[ 4] = "-0.0009548499998506730415112255157650113355104637663298519791633424726946992806658956";
  DC3[ 5] = "-0.0006010038458963603912075805875795611286932555907537579329769784072390871266471527";
  DC3[ 6] = "0.00010128858286776621953344349418087858288813181266544865585870025117392863374779861";
  DC3[ 7] = "0.00006865733449299825642457428364865218534328592530073865273869547284481941105060958";
  DC3[ 8] = "-5.985366791538598159305933853289474476033254319520777140355079450311296076398332e-7";
  DC3[ 9] = "-3.33165985123994712904355366983830793171285955443637487051019057060568872956295e-6";
  DC3[10] = "-2.1919289102435081057184842192253694457056301094787176328502199928079472310677209e-7";
  DC3[11] = "7.890884245681494410555248261568885233534195350876097985016870653592807297722941e-8";
  DC3[12] = "9.41468508129526215165246515670888721434440703062285787060390697195816617233733e-9";
  DC3[13] = "-9.570116210883480301880722847736899414920424990844021810115468872811044469914093e-10";
  DC3[14] = "-1.8763137453470662796812970577763318771497261610950370939934010326350481628028716e-10";
  DC3[15] = "4.437837679323399327464708984967982039427175136450134427877701103829299216728465e-12";
  DC3[16] = "2.2426738505617353248411068573063743908847573555967383640946296048845725322165884e-12";
  DC3[17] = "3.627686865735243689408255637923200993091627857438909649329814867831900563004191e-14";
  DC3[18] = "-1.7639809550821581607831121498067405612829056369595996874090447022130186603011647e-14";
  DC3[19] = "-7.960765246786777757290345179277877672969068290389354910445419703993693619905826e-16";
  DC3[20] = "9.419651490589690763914895025694423958555439910507587271684011367592679056089096e-17";
  DC3[21] = "7.133103854569657824556667924637208733076137253777613086648835921659278195880674e-18";
  DC3[22] = "-3.289910584554624321179665258492719604389867767706730329578387560456883113849552e-19";
  DC3[23] = "-4.180730374898459291362924870562363545456098499893508469921618986694281682179048e-20";
  DC3[24] = "5.550542071646333789782116402662976359558940175392938033703401483591298082182587e-22";
  DC3[25] = "1.7870441906260123858717636353127488582980187518683339734105072221029271545889173e-22";
  DC3[26] = "1.331280396465609428629734301456596911495766449193494655875984603950395330646233e-24";
  DC3[27] = "-5.818610611090987516179216596088520206267870242969322822752311337488525847711228e-25";
  DC3[28] = "-1.4019036088526555374364967097960412447326079695863802828805235344426680330368205e-26";
  DC3[29] = "1.4641320211626254148997752501860798089334733220831445783786891177657679796555688e-27";
  DC3[30] = "6.02332655108914231894545302168540534775476763292822981011798811478407558676743e-29";
  DC3[31] = "-2.8064472319113607480413277200041961058924502875599284388627977593567952984627978e-30";
  DC3[32] = "-1.8065060055924548468166679975772190204281290392132976714058746943222867824547636e-31";
  DC3[33] = "3.779508331934081109538275143960178072816211713033459437067101283961906998805713e-33";
  DC3[34] = "4.214558052947562754928267311962627941300036513813646793136217341959757887604597e-34";
  DC3[35] = "-2.2110619283398807703089110108495251373369608027039372389420927090703989012463229e-36";
  DC3[36] = "-7.977857191491540240197869206644086240230293731794895306614670662176495711819498e-37";
  DC3[37] = "-5.134879815416697465299965219246691353718718876508043721203193132000758105946362e-39";
  DC3[38] = "1.2486406302153718700908292104194463596234890838129830954576262777222039185771739e-39";

  for (i = 0; i < DCI_SIZE; ++i) {
    DC0_LOW[i] = low(double(DC0[i])); DC0_HIGH[i] = high(double(DC0[i]));
    DC1_LOW[i] = low(double(DC1[i])); DC1_HIGH[i] = high(double(DC1[i]));
    DC2_LOW[i] = low(double(DC2[i])); DC2_HIGH[i] = high(double(DC2[i]));
    DC3_LOW[i] = low(double(DC3[i])); DC3_HIGH[i] = high(double(DC3[i]));
    DDC0_LOW[i] = low(DC0[i]); DDC0_HIGH[i] = high(DC0[i]);
    DDC1_LOW[i] = low(DC1[i]); DDC1_HIGH[i] = high(DC1[i]);
    DDC2_LOW[i] = low(DC2[i]); DDC2_HIGH[i] = high(DC2[i]);
    DDC3_LOW[i] = low(DC3[i]); DDC3_HIGH[i] = high(DC3[i]);
  }

  // fill array such that
  // max(cos((i-1)*GRID), cos((i+1)*GRID)) = cosValue[2*i]
  // and min(cos((i-1)*GRID), cos((i+1)*GRID)) = cosValue[2*i+1]
  // for all 0 <= i <= NPREP
  double cos0 = 1.0;//cos(-GRID);
  double cos1 = 1.0;
  for (i = 1, j = 0; i <= NPREP2+1; ++i, j += 2) {
    cosValue[j] = high(cos0);
    cos0 = cos1;
    cos1 = cos(i*GRID);
    cosValue[j+1] = low(cos1);
  }
  for (; i <= NPREP+1; ++i, j += 2) {
    cosValue[j+1] = low(cos0);
    cos0 = cos1;
    cos1 = cos(i*GRID);
    cosValue[j] = high(cos1);
  }
  cosValue[NPREP+3] = -1.0;
  cosValue[j-2] = 1.0;
  /*int idxLow,idxHigh;
  for (idxLow = 2; idxLow <= NPREP; ++idxLow) {
    double c1,c2;
    minmax3(cosValue+idxLow-1, cosValue+idxLow-1, c1, c2);
    if (c1 != cosValue[idxLow+1] || c2 != cosValue[idxLow-1]) break;
  }
  for (idxHigh = NPREP; idxHigh > 2; --idxHigh) {
    double c1,c2;
    minmax3(cosValue+idxHigh-1, cosValue+idxHigh-1, c1, c2);
    if (c2 != cosValue[idxHigh+1] || c1 != cosValue[idxHigh-1]) break;
  }
  if (idxLow != idxHigh || idxLow != NPREP2+1) {
    cerr << "Internal Error:" << idxLow << ',' << idxHigh << ',' << (NPREP2+1) << endl;
    exit(8);
  }*/

  return true;
}

void EvalZeta::destroy()
{
  sqrtinvMem.free(sqrtinvLn, lnDD, sqrtinvDD);
}

EvalZeta::~EvalZeta()
{
  destroy();
  running = 0;
  --FreeSqrtinvMem::active; // Thread-safe
}
