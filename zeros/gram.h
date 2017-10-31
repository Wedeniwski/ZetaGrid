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


#ifndef __gram_h__
#define __gram_h__

#include <cmath>
#include <vector>
#include "constants.h"
#include "doubledouble.h"

using namespace std;

struct GramBlock {
  double t,z;
  int zeros;

  GramBlock();
  GramBlock(const double&, const double&);
  GramBlock(const GramBlock&);

  GramBlock& operator=(const GramBlock&);
  void setZero(const double&, const double&);
};

typedef vector<GramBlock> gramBlockType;


double Gram(startType n, double a);
doubledouble Gram(const doubledouble& n, const doubledouble& a);
startType getStartN(double t);
doubledouble getStartN(const doubledouble& t);


inline double Gram3(longlong n, double a)
{
  return ::floor(Gram(n, a)*1000.0)/1000.0;
}

inline GramBlock& GramBlock::operator=(const GramBlock& b)
{
  t = b.t; z = b.z; zeros = b.zeros;
  return *this;
}

inline void GramBlock::setZero(const double& a, const double& az)
{
  t = a; z = az; zeros = 0;
}

inline GramBlock::GramBlock()
{
  t = z = 0; zeros = 0;
}

inline GramBlock::GramBlock(const double& a, const double& az)
{
  setZero(a, az);
}

inline GramBlock::GramBlock(const GramBlock& b)
{
  *this = b;
}

#endif
