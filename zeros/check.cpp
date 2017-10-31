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


#include <iostream>
#include <fstream>
#include <string>

#include "check.h"
#include "theta.h"
#include "eval_zeta.h"

using namespace std;

void testEvalZ(startType LASTN, int NRANGE)
/*
282.465:395.86:-0.00025841,-0.000809056
282.465:395.86:3.47811e-06,-0.000547151
282.465:395.861:0.000265294,-0.000285322
282.465:395.861:0.000527071,-2.35691e-05
282.465:395.861:0.000788796,0.000238107
282.465:395.861:0.00105042,0.000499707
282.466:395.861:0.00131193,0.000761231
282.466:395.862:0.00157337,0.00102268
282.466:395.862:0.00183473,0.00128405
282.466:395.862:0.00209602,0.00154534
282.466:395.862:0.00235724,0.00180656
*/
{
  Output output(LASTN, NRANGE);
  EvalZeta evalZeta(&output);
  double d = 282.465;
  while (d < 282.466) {
    cout << d << ':' << theta(d) << ':' << evalZeta.evalZ(d) << ',' << evalZeta.evalDZ(d) << endl;
    d += 0.0001;
  }

  cout.precision(20);

  double thetaLow,thetaHigh;
  for (double t = 10.0; t < 10000000000.0; t *= 10.0) {
    theta(t, thetaLow, thetaHigh);
    cout << theta(t) << ':' << thetaLow << ',' << thetaHigh << endl;
  }
}

void checkZeros()
{
  ifstream fin("zeta_zeros");
  ifstream fin2("zeta_zeros.txt");
  string s;
  int lines = 0;
  double a = 14.1;
  cout.precision(10);
  while (!fin.eof() && !fin2.eof()) {
    getline(fin, s);
    double b = atof(s.c_str());
    getline(fin2, s);
    if (b > 0.0 && s.size() > 0) {
      int idx = s.find('.')+1;
      if (a >= b && a-b > 0.001) {
        cout << "1:" << a << ',' << b << endl;
        return;
      }
      double a2 = atof(s.substr(idx).c_str());
      if (a2 <= b && b-a2 > 0.001) {
        cout << "2:" << a2 << ',' << b << endl;
        return;
      }
      if (a2 <= a) {
        cout << "3:" << a << ',' << a2 << endl;
        return;
      }
      a = a2; ++lines;
    }
  }
  cout << lines << " lines are successfully checked." << endl;
}

void verify(const char* small, const char* large)
{
  char s[255],s2[255];
  ifstream fin(small);
  ifstream fin2(large);
  *s2 = 0;
  if (!fin.eof()) {
    fin.getline(s, 255);
    cout << "start with line:" << s << endl;
    while (!fin2.eof() && strcmp(s, s2)) {
      fin2.getline(s2, 255);
    }
    if (strcmp(s, s2)) cout << "ERROR" << endl;
    else {
      int idx = 1;
      while (!fin.eof() && !fin2.eof() && strcmp(s, s2) == 0) {
        fin.getline(s, 255);
        fin2.getline(s2, 255);
        ++idx;
      }
      cout << idx << " equal lines" << endl;
      if (strcmp(s, s2)) {
        cout << "Error: " << s << ", " << s2 << endl;
      }
    }
  }
}
