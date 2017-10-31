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
     S. Wedeniwski
--*/


#ifndef __constants_h__
#define __constants_h__

#include "lowhigh.h"
#include <iostream>

#ifdef _MSC_VER
typedef __int64 longlong;
#else
typedef long long longlong;
#endif

typedef longlong startType;


using namespace std;

const double PI = 3.1415926535897932384626433832795028841971693993751L;
const double PI_INV = 1.0/PI;
const double PI_SL8 = PI*0.125;
const double TWO_PI = 2*PI;
const double TWO_PI_INV = 0.5*PI_INV;

const double PI_LOW = 3.141592653589793L;
const double PI_HIGH = high(PI_LOW);
const double PI_INV_LOW = 0.31830988618379066L;
const double PI_INV_HIGH = high(PI_INV_LOW);
const double PI_SL8_LOW = 0.392699081698724139L;
const double PI_SL8_HIGH = high(PI_SL8_LOW);
const double TWO_PI_LOW = 6.2831853071795864L;
const double TWO_PI_HIGH = high(TWO_PI_LOW);
const double TWO_PI_INV_LOW = 0.15915494309189531L;
const double TWO_PI_INV_HIGH = high(TWO_PI_INV_LOW);

#endif
