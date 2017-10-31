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


#ifndef __zeta_zeros_h__
#define __zeta_zeros_h__

#include "constants.h"
#include "output.h"
#include "eval_zeta.h"

// History:   Version:  Comment
// 2001-09-14 0107:     no shifts, and usage of doubledouble
// 2001-09-21 0108:     fix for Gram blocks of type '032'
// 2001-09-25 0109:     search2c ncycles from 7 to 9 since n=963347839
// 2001-10-01 0110:     low/high calculation in doubledouble (sumMZ)
// 2001-10-15 0111:     add search2c after search2b in search2a if "no zero was found" since n=2465882374
//                      extend new rule to detect Gram block of type "0132" since n=2622913795
// 2001-10-22 0112:     extend search2c by depth-search since n=3928336523
// 2001-10-26 0113:     bug-fix in search2c (depth-search array to small) since n=6670521771
// 2001-11-02 0114:     sumZ, search2c, search2d, and search3 improved
//                      add search3b since n=6977331390
// 2001-11-10 0115:     add searchND, and change search2d since n=7182428423
//                      sumZ improved
// 2001-11-15 0116:     search2c finds local minimum in a Gram interval instead of global minimum since n=8136521010
// 2001-11-18 0117:     search2c can handle two local minimum since n=8150548697
//                      replace sumDZ by sumDD since n=8547405564
// 2001-11-24 0118:     thread safe
// 2001-11-30 0119:     bug-fix in output, e.g. OBM e-mail
// 2001-12-14 0120:     fix a memory leak (destructor of EvalZeta), search2d finds wrong local minimum since n=10931594655
// 2001-12-30 0121:     fix for output of Gram blocks of type '032' (one zero lost) e.g. since n=11833576681
//                      reduceFilesize was not thread safe
//                      change ',' to '.' in reduceFilesize for some German computers
// 2002-01-03 0122:     improvement of sumZ and sumDD (6% Windows x86, 40% Linux x86, 0.7% AIX ppc)
//                      add Assembler code for Windows (2.14 times faster)
// 2002-01-17 0123:     deeper search in search2d since n=15989133267
// 2002-01-24 0124:     set nbreak=4 in search2c since n=17913470332
// 2002-01-26 0125:     fix for output of Gram blocks of type '0312' (one zero was wrong if it change from type '0310')
//                      since n=18776201993
// 2002-01-30 0126:     rewrite (and deactivate) search3b (found just one zero) since n=19987208408
//                      search2d determine a too small interval since n=20601789292
//                      evalZ use doubledouble for computation of tau,rho, and error terms
//                      remove low/high calculation in sumDD since it is not needed
// 2002-02-04 0127:     search2d ncycles from 7 to 8 since n=23279559198
// 2002-04-02 0128:     search2d ignore local minimum in first cycle since n=40381610677
//                      search3a check last interval by search2c since n=42583815545
// 2002-04-20 0129:     add adjustPreviousOutput since n=53365784978 (extreme S(t))
// 2002-05-27           extend newRule for activateNewRule=4 since n=67976501145 (extreme S(t))
// 2002-06-12 0130:     improvement: call search2d earlier inside of zigzag (i = 128) instead after zigzag (i = 256)
// 2002-06-19 0131:     replace Gram points in reduceFileSize by '.' (reduces the package size by a factor 2.2 (4.3 compressed))
//                      bug fix of version 0130 search2d was not called for a Gram block of length 2, e.g. n=75040300956
// 2002-09-19 0132:     switch in search3a from search2c to search2d if depth=256, e.g. n=90357923677
//                      generate for every thread a status file "zeta_zeros.tmp"
//                      log large values, log close pair of zeros
//                      improve function fmod for x86
// 2002-12-12 0133:     extend interface to set the method (to use more memory)
// 2002-12-26 0134:     reduceFilesize more stabile
//                      add exit in JNI
// 2003-01-06 0135:     searchND searches for too many zeros (e.g. two zeros instead of one zero), e.g. n=231101998207
//                      computing a better bound in checkClosePairOfZeros (two values in the middle)
// 2003-02-21 0136:     generate only one cos table for every process (to reduce memory usage for multi-processors)
//                      use variable sleepCounter for a better way to control processor usage
//                      bug fixed that checkClosePairOfZeros was called for a wrong interval in search3a (last case), e.g. n=270390102588
//                      posFastMod2Pi improves generic fmod, e.g. 30% AIX ppc
// 2003-04-23 0137:     using shared tables to evaluate the function Z (speed-up the initialization and reduce memory usage for multi-processors)
//                      bug in iterateZero fixed (evalDZ was called with type double and not with doubledouble)
//                      searchN reduces delta if the Z-values are small (<0.00001) since 4 close zeros at n=158366484202
// 2003-10-22 0138:     improve fastSumZ, also in Assembler for Windows x86 and Linux x86
//                      generate file "zeta_zeros.pid" with the process id
//                      fix an endless loop in searchND and checkClosePairOfZeros
// 2004-02-07 0139:     computation can be interrupted or terminated
// 2004-02-29 0140:     J. Diermeier improves fastSumZ Assembler for Windows x86
// 2004-07-03 0141:     in the new rule does not adjust a previous output correctly for extreme S(t), e.g. n=377535715746
//                      log extrem S(t)
//                      fix a bug which removes one upper bound in specific pattern like 0312 with an upper bound which is equal to an integer, e.g. n=418937753840


#define VERSION "0141"

bool isExit();


class ZetaZeros {
private:
  static int activeThreads;
  bool logHappened;
  int result;
  Output output;

  void iterateZero(doubledouble& a, doubledouble& b, double& za, double& zb, double delta);
  void checkClosePairOfZeros(double a, double b, double c, double za, double zb, double zc);
  bool search2b(double a, double b, double za, double zb, double& t, double& zt);
  bool search2c(double a, double b, double za, double zb, double& t, double& zt);
  bool searchND(int n, double a, double b, double za, double zb, double h, double* t, double* z);
  bool search2d(double a, double b, double za, double zb, double& t, double& zt);
  bool parabola(int index, double* at, double* az, int* next, bool* accept, int& indout, bool& convex);
  bool search2Zeros(double a, double za, double b, double zb, double& t, double& zt);
  bool search2a(double a, double b, double za, double zb, bool reverse, double& t, double& zt);
  bool search3b(double a, double b, double za, double zb, double* t, double* z);
  bool search3a(double a, double b, double za, double zb, int depth, double* t, double* z);
  bool searchN(int n, double a, double b, double za, double zb, int depth, double* t, double* z);
  bool zigzag(gramBlockType& gramBlock, int gramBlockLength);
  int  evalGramBlockLength(startType& n, gramBlockType& gramBlock);
  bool newRule(gramBlockType& gramBlock, int activateNewRule);
  int  searchZeros(startType LASTN, int NRANGE, int sleepN);

public:
  EvalZeta evalZeta;
  ZetaZeros(const startType& LASTN, int NRANGE, int sleepN);
  int getResult() const;
};

inline ZetaZeros::ZetaZeros(const startType& LASTN, int NRANGE, int sleepN)
 : output(LASTN, NRANGE), evalZeta(&output)
{
  result = (output.getInitResult() >= 0)? searchZeros(LASTN, NRANGE, sleepN) : 1;
}

inline int ZetaZeros::getResult() const
{
  return result;
}

#endif
