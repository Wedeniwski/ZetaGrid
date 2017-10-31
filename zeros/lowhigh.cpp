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


#include <algorithm>
#include "lowhigh.h"

using namespace std;

double low(const double t)
{
  if (t == 0) return t;
  ieee_double_extract x;
  x.d = t;
  if (t > 0) {
    if (x.s.manl-- == 0 && x.s.manh-- == 0 && x.s.exp-- == 0) return t;
  } else {
    if (++x.s.manl == 0 && ++x.s.manh == 0 && ++x.s.exp == 0) return t;
  }
  return x.d;
}

double high(const double t)
{
  if (t == 0) return t;
  ieee_double_extract x;
  x.d = t;
  if (t > 0) {
    if (++x.s.manl == 0 && ++x.s.manh == 0 && ++x.s.exp == 0) return t;
  } else {
    if (x.s.manl-- == 0 && x.s.manh-- == 0 && x.s.exp-- == 0) return t;
  }
  return x.d;
}

void lowHigh(const double t, double& low, double& high)
{
  if (t == 0) {
    low = high = t;
  } else {
    ieee_double_extract x;
    x.d = t;
    if (t > 0) {
      if (++x.s.manl == 0 && ++x.s.manh == 0 && ++x.s.exp == 0) high = x.d = t;
      else { high = x.d; x.d = t; }
      low = (x.s.manl-- == 0 && x.s.manh-- == 0 && x.s.exp-- == 0)? t : x.d;
    } else {
      if (++x.s.manl == 0 && ++x.s.manh == 0 && ++x.s.exp == 0) low = x.d = t;
      else { low = x.d; x.d = t; }
      high = (x.s.manl-- == 0 && x.s.manh-- == 0 && x.s.exp-- == 0)? t : x.d;
    }
  }
}

void minmax3(const double* a, const double* b, double& min3, double& max3)
{
  if (a[0] < a[1]) {
    if (a[1] < a[2]) { // a[0] < a[1] < a[2]
      if (b[0] < b[1]) {
        if (b[1] < b[2]) { // b[0] < b[1] < b[2]
          min3 = min(a[0], b[0]);
          max3 = max(a[2], b[2]);
        } else if (b[2] < b[0]) { // b[2] < b[0] < b[1]
          min3 = min(a[0], b[2]);
          max3 = max(a[2], b[1]);
        } else { // b[0] <= b[2] <= b[1]
          min3 = min(a[0], b[0]);
          max3 = max(a[2], b[1]);
        }
      } else {
        if (b[2] < b[1]) { // b[2] < b[1] <= b[0]
          min3 = min(a[0], b[2]);
          max3 = max(a[2], b[0]);
        } else if (b[2] < b[0]) { // b[1] <= b[2] < b[0]
          min3 = min(a[0], b[1]);
          max3 = max(a[2], b[0]);
        } else { // b[1] <= b[0] <= b[2]
          min3 = min(a[0], b[1]);
          max3 = max(a[2], b[2]);
        }
      }
    } else if (a[2] < a[0]) { // a[2] < a[0] < a[1]
      if (b[0] < b[1]) {
        if (b[1] < b[2]) { // b[0] < b[1] < b[2]
          min3 = min(a[2], b[0]);
          max3 = max(a[1], b[2]);
        } else if (b[2] < b[0]) { // b[2] < b[0] < b[1]
          min3 = min(a[2], b[2]);
          max3 = max(a[1], b[1]);
        } else { // b[0] <= b[2] <= b[1]
          min3 = min(a[2], b[0]);
          max3 = max(a[1], b[1]);
        }
      } else {
        if (b[2] < b[1]) { // b[2] < b[1] <= b[0]
          min3 = min(a[2], b[2]);
          max3 = max(a[1], b[0]);
        } else if (b[2] < b[0]) { // b[1] <= b[2] < b[0]
          min3 = min(a[2], b[1]);
          max3 = max(a[1], b[0]);
        } else { // b[1] <= b[0] <= b[2]
          min3 = min(a[2], b[1]);
          max3 = max(a[1], b[2]);
        }
      }
    } else { // a[0] <= a[2] <= a[1]
      if (b[0] < b[1]) {
        if (b[1] < b[2]) { // b[0] < b[1] < b[2]
          min3 = min(a[0], b[0]);
          max3 = max(a[1], b[2]);
        } else if (b[2] < b[0]) { // b[2] < b[0] < b[1]
          min3 = min(a[0], b[2]);
          max3 = max(a[1], b[1]);
        } else { // b[0] <= b[2] <= b[1]
          min3 = min(a[0], b[0]);
          max3 = max(a[1], b[1]);
        }
      } else {
        if (b[2] < b[1]) { // b[2] < b[1] <= b[0]
          min3 = min(a[0], b[2]);
          max3 = max(a[1], b[0]);
        } else if (b[2] < b[0]) { // b[1] <= b[2] < b[0]
          min3 = min(a[0], b[1]);
          max3 = max(a[1], b[0]);
        } else { // b[1] <= b[0] <= b[2]
          min3 = min(a[0], b[1]);
          max3 = max(a[1], b[2]);
        }
      }
    }
  } else { // a[1] <= a[0]
    if (a[2] < a[1]) { // a[2] < a[1] <= a[0]
      if (b[0] < b[1]) {
        if (b[1] < b[2]) { // b[0] < b[1] < b[2]
          min3 = min(a[2], b[0]);
          max3 = max(a[0], b[2]);
        } else if (b[2] < b[0]) { // b[2] < b[0] < b[1]
          min3 = min(a[2], b[2]);
          max3 = max(a[0], b[1]);
        } else { // b[0] <= b[2] <= b[1]
          min3 = min(a[2], b[0]);
          max3 = max(a[0], b[1]);
        }
      } else {
        if (b[2] < b[1]) { // b[2] < b[1] <= b[0]
          min3 = min(a[2], b[2]);
          max3 = max(a[0], b[0]);
        } else if (b[2] < b[0]) { // b[1] <= b[2] < b[0]
          min3 = min(a[2], b[1]);
          max3 = max(a[0], b[0]);
        } else { // b[1] <= b[0] <= b[2]
          min3 = min(a[2], b[1]);
          max3 = max(a[0], b[2]);
        }
      }
    } else if (a[2] < a[0]) { // a[1] <= a[2] < a[0]
      if (b[0] < b[1]) {
        if (b[1] < b[2]) { // b[0] < b[1] < b[2]
          min3 = min(a[1], b[0]);
          max3 = max(a[0], b[2]);
        } else if (b[2] < b[0]) { // b[2] < b[0] < b[1]
          min3 = min(a[1], b[2]);
          max3 = max(a[0], b[1]);
        } else { // b[0] <= b[2] <= b[1]
          min3 = min(a[1], b[0]);
          max3 = max(a[0], b[1]);
        }
      } else {
        if (b[2] < b[1]) { // b[2] < b[1] <= b[0]
          min3 = min(a[1], b[2]);
          max3 = max(a[0], b[0]);
        } else if (b[2] < b[0]) { // b[1] <= b[2] < b[0]
          min3 = min(a[1], b[1]);
          max3 = max(a[0], b[0]);
        } else { // b[1] <= b[0] <= b[2]
          min3 = min(a[1], b[1]);
          max3 = max(a[0], b[2]);
        }
      }
    } else { // a[1] <= a[0] <= a[2]
      if (b[0] < b[1]) {
        if (b[1] < b[2]) { // b[0] < b[1] < b[2]
          min3 = min(a[1], b[0]);
          max3 = max(a[2], b[2]);
        } else if (b[2] < b[0]) { // b[2] < b[0] < b[1]
          min3 = min(a[1], b[2]);
          max3 = max(a[2], b[1]);
        } else { // b[0] <= b[2] <= b[1]
          min3 = min(a[1], b[0]);
          max3 = max(a[2], b[1]);
        }
      } else {
        if (b[2] < b[1]) { // b[2] < b[1] <= b[0]
          min3 = min(a[1], b[2]);
          max3 = max(a[2], b[0]);
        } else if (b[2] < b[0]) { // b[1] <= b[2] < b[0]
          min3 = min(a[1], b[1]);
          max3 = max(a[2], b[0]);
        } else { // b[1] <= b[0] <= b[2]
          min3 = min(a[1], b[1]);
          max3 = max(a[2], b[2]);
        }
      }
    }
  }    
}

/*int main()
{
  double a[6],b,c;
  for (int i = 0; i < 6; ++i) a[i] = i;
  int iter = 0;
  do {
    minmax3(a, a+3, b, c);
    if (b != 0 || c != 5) { cout << "ERROR!" << endl; break; }
    ++iter;
  } while (next_permutation(a, a+6));
  cout << "iter=" << iter << endl;
  return 0;
}*/
