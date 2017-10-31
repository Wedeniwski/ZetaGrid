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


#ifndef __output_h__
#define __output_h__


#include <iostream>
#include <fstream>

#include "gram.h"

using namespace std;


const bool OUTPUT_ZEROS = true;
extern bool coutLog;

#define LOG(a, b)  { if (coutLog) cout << b << flush; *((a).logOut) << b; }
#define LOGLN(a, b)  { if (coutLog) cout << b << endl; *((a).logOut) << b << '\n'; (a).logLn(false); }

void setCoutLog(bool log);

// strstream standard header
#ifndef _MSC_VER
#include <strstream>
#include <cstdio>

#ifdef _DEFINE_ATOLL_
#include <ctype.h>
inline longlong atoll(const char* c)
{
  int ch = int((unsigned char)*c);
  while (isspace(ch)) ch = int((unsigned char)*++c);
  const int sign = ch;
  if (ch == '-' || ch == '+') ch = int((unsigned char)*++c);
  longlong n = 0;
  while (isdigit(ch)) {
    n = 10*n + (ch-'0');
    ch = int((unsigned char)*++c);
  }
  return (sign == '-')? -n : n;
}

inline char* lltoa(longlong n, char* c, int radix)
{
  char* p = c;
  if (n < 0) { *p++ = '-'; n = -n; }
  char* q = p;
  do {
    const unsigned int m = (unsigned int)(n%radix);
    n /= radix;
    *p++ = (m > 9)? char(m-10+'a') : char(m+'0');
  } while (n > 0);
  *p-- = '\0';
  do {
    const char ch = *p;
    *p = *q; *q = ch;
  } while (++q < --p);
  return c;
}
#elif !defined(_NOT_DEFINE_LLTOA_)
inline char* lltoa(longlong n, char* c, int)
{
  ::sprintf(c, "%lld", n);
  return c;
}
#endif

#else 
#include <strstream>

inline longlong atoll(const char* c)
{
  return _atoi64(c);
}

inline char* lltoa(longlong n, char* c, int b)
{
  return _i64toa(n, c, b);
}

inline ostream& operator<<(ostream& out, const longlong& a)
{
  char c[22];
  _i64toa(a, c, 10);
  return out << c;
}

#endif  /* _MSC_VER */


struct strstreambuf2 : public strstreambuf {
  streampos seekoff2(streamoff off, ios::seekdir dir , ios::openmode mode = ios::in | ios::out) { return seekoff(off, dir, mode); }
};

struct strstream2 : public strstream {
  strstream2(char* buffer, streamsize size, openmode mode = in | out) : strstream(buffer, size, mode) {}
  strstreambuf2* rdbuf2() { return (strstreambuf2*)rdbuf(); }
};

class Output {
private:
  double outLess;
  char* zerosBuffer;
  strstream2* zerosOut;
  ofstream* zerosFileOut;
  char* logBuffer;
  ofstream* logFileOut;
  int outPrecision;
  int numberOfZeros;
  int numberOfLines;
  int previousZerosPos;
  char* lastLine;
  char* prevLastLine;
  char zerosFilename[255];
  char previousZeros[255];
  int initResult;

  int initOutput(startType LASTN, int NRANGE);
  void zerosFlush(bool flush);
  void checkPrecision(ostream* fout, double g);

public:
  ostream* logOut;

  Output(startType LASTN, int NRANGE);
  ~Output();

  void destroy(bool reduce);
  int getInitResult() const;
  void setupPrecision(double g);
  void logLn(bool flush);
  void output(gramBlockType& gramBlock);
  void output(double g2, gramBlockType& gramBlock);
  void output2(double g2, double g3, gramBlockType& gramBlock);
  void output(int n, const double* t, gramBlockType& gramBlock);
  void outputError(const gramBlockType& gramBlock);
  void removePreviousOutput();
  void adjustPreviousOutput();
  void debug(longlong n, const char* filename);
};

inline int Output::getInitResult() const
{
  return initResult;
}

#endif

