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


#include <cstdio>
#include <cstring>
#include <fstream>
#include <string>
#include <signal.h>
#include "output.h"
#include "statistic.h"
#include "eval_zeta.h"
#include "zeta_zeros.h"

using namespace std;

//#define _OUTPUT_EXACT_ZEROS_

const int NUMBER_OF_ZEROS_UNTIL_FLUSH = 10000;
const int NUMBER_OF_LINES_UNTIL_FLUSH = 100;
const int BUFFER_ZEROS = NUMBER_OF_ZEROS_UNTIL_FLUSH*70;
const int BUFFER_LOG = NUMBER_OF_LINES_UNTIL_FLUSH*800;

bool coutLog = true;


void setCoutLog(bool log)
{
  coutLog = log;
}


extern "C" void exitCalculation2(int terminate);

static void signalCatcher(int signal)
{
  //LOGLN(*this, "Control-C or similar caught");
  exitCalculation2(1);
}


Output::Output(startType LASTN, int NRANGE)
{
  logOut = 0;
  outLess = 10.0;
  zerosBuffer = 0;
  zerosOut = 0;
  zerosFileOut = 0;
  logBuffer = 0;
  logFileOut = 0;
  outPrecision = 4;
  numberOfZeros = 0;
  numberOfLines = 0;
  previousZerosPos = 0;
  lastLine = new char[255];
  prevLastLine = new char[255];
  *zerosFilename = *previousZeros = *lastLine = *prevLastLine = '\0';
  initResult = initOutput(LASTN, NRANGE);
}

Output::~Output()
{
  destroy(true);
  delete[] lastLine;
  lastLine = 0;
  delete[] prevLastLine;
  prevLastLine = 0;
}

void Output::destroy(bool reduce)
{
  if (!logOut || !*logOut || !logFileOut || !*logFileOut) return;
  zerosFlush(true);
  zerosFileOut->close();
  delete zerosFileOut;
  zerosFileOut = 0;
  delete zerosOut;
  zerosOut = 0;
  delete[] zerosBuffer;
  zerosBuffer = 0;
  if (reduce) {
    if (reduceFilesize(this, zerosFilename) != 2) LOG(*this, '@');
  }
  logLn(true);
  logFileOut->close();
  delete logFileOut;
  logFileOut = 0;
  delete logOut;
  logOut = 0;
  delete[] logBuffer;
  logBuffer = 0;
}

int Output::initOutput(startType LASTN, int NRANGE)
{
  if (LASTN < 0 || NRANGE <= 0) return -1;

  signal(SIGINT, signalCatcher);
  signal(SIGTERM, signalCatcher);

  char c[255];
  char* buffer = new char[255];
  char* buffer2 = new char[255];
  lltoa(LASTN, buffer, 10);
  //qtoa(buffer, 0, 'f', LASTN);
  sprintf(c, "zeta_zeros_%s_%d.log", buffer, NRANGE);
  ifstream finLog(c);
  if (finLog.good() && !finLog.eof()) {
    finLog.seekg(0, ios::end);
    const int pos = finLog.tellg();
    if (pos > 1) {
      finLog.seekg(pos-1);
      if (finLog.get() == '@') return -1;
    }
  }
  int i = strlen(c);
  c[i-3] = 't'; c[i-2] = 'x'; c[i-1] = 't';
  strcpy(zerosFilename, c);
  ifstream fin(c);
  bool appendMode = false;
  //longlong origLASTN = LASTN;
  int newZeros = 0;
  *lastLine = *prevLastLine = *buffer = *buffer2 = '\0';
  bool lastLineCorrect = true;
  if (fin.good() && !fin.eof()) {
    fin.getline(buffer, 255);
    strcpy(lastLine, buffer);
    if (*buffer == '.') {     // error case
      c[i-3] = 'l'; c[i-2] = 'o'; c[i-1] = 'g';
      remove(c);
      c[i-3] = 't'; c[i-2] = 'x'; c[i-1] = 't';
    } else {
      appendMode = (*buffer != '\0');
      double lastZero = 0.0;
      if (appendMode) {
        ++newZeros;
        const char* str = strchr(buffer, '.');
        if (str) lastZero = atof(str+1);
      }
      bool count = true;
      /* ToDo: Bug!
      c[i-3] = 'l'; c[i-2] = 'o'; c[i-1] = 'g';
      ifstream finLog(c);
      c[i-3] = 't'; c[i-2] = 'x'; c[i-1] = 't';
      finLog.seekg(0, ios::end);
      const int pos = finLog.tellg(); // check last line of LOG
      if (pos > 100) {
        finLog.seekg(pos-100);
        finLog.getline(buffer, 255);
        while (finLog.good() && !finLog.eof()) {
          finLog.getline(buffer, 255);
          if (strncmp("Exit n=", buffer, 7) == 0 && strlen(buffer) > 7) {
            LASTN = atoll(buffer+7);
            if (LASTN >= origLASTN && LASTN <= origLASTN+NRANGE) LASTN -= origLASTN;
            else LASTN = 0;
            break;
          }
        }
      }
      finLog.close();
      if (LASTN > 0) {
        fin.seekg(0, ios::end);
        const int pos = fin.tellg();
        if (pos > 100) {
          fin.seekg(pos-100);
          count = false;
          fin.getline(buffer2, 255);
        } else fin.seekg(0, ios::beg);
      }*/
      while (!fin.eof()) {
        //if (isExit()) return -1;
        fin.getline(buffer2, 255);
        lastLineCorrect = false;
        if (fin.fail()) {
          fin.clear();
          string s;
          getline(fin, s);
          *buffer2 = '\0';
          if (s.length() < 255) strcpy(buffer2, s.c_str());
        }
        if (*buffer2) {
          char* x = prevLastLine;
          prevLastLine = lastLine; lastLine = buffer2; buffer2 = x;
          const char* strDot = strchr(lastLine, '.');
          double d = (strDot)? atof(strDot+1) : 0.0;
          if (d < lastZero) {
            // duplicate zeros are skiped
            while (!fin.eof() && strcmp(buffer, lastLine)) {
              fin.getline(buffer2, 255);
              if (fin.fail()) {
                fin.clear();
                string s;
                getline(fin, s);
                *buffer2 = '\0';
                if (s.length() < 255) strcpy(buffer2, s.c_str());
              }
              if (*buffer2 == '\0') break;
              strDot = strchr(buffer2, '.');
              if (strDot && lastZero > 0.0 && atof(strDot+1) > lastZero) {
                x = prevLastLine; prevLastLine = lastLine; lastLine = buffer2; buffer2 = x;
                break;
              }
            }
          } else {
            if (count) ++newZeros;
            strcpy(buffer, buffer2);
          }
          lastZero = d;
        } else lastLineCorrect = true;
      }
      if (!lastLineCorrect) *lastLine = '\0';
    }
  }
  if (newZeros > 15) newZeros -= 16;
  if (newZeros > 1) --newZeros;
  fin.close();
  zerosBuffer = new char[BUFFER_ZEROS];
  if (!zerosBuffer) return -1;
  memset(zerosBuffer, 0, BUFFER_ZEROS);
  zerosOut = new strstream2(zerosBuffer, BUFFER_ZEROS-1);
  zerosFileOut = (appendMode)? new ofstream(c, ios::app) : new ofstream(c);
  if (!lastLineCorrect) *zerosFileOut << endl;
  if (!zerosOut || !*zerosOut) return -1;
  c[i-3] = 'l'; c[i-2] = 'o'; c[i-1] = 'g';
  logBuffer = new char[BUFFER_LOG];
  if (!logBuffer) return -1;
  memset(logBuffer, 0, BUFFER_LOG);
  logOut = new strstream2(logBuffer, BUFFER_LOG-1);
  logFileOut = new ofstream(c, ios::app);
  if (!logOut || !*logOut || !logFileOut || !*logFileOut) return -1;
  zerosOut->precision(outPrecision);
  logOut->precision(outPrecision);
  logFileOut->precision(outPrecision);
  previousZerosPos = numberOfZeros = numberOfLines = 0;
  delete[] buffer;
  delete[] buffer2;
  return newZeros;
}

void Output::zerosFlush(bool flush)
{
  if (flush || numberOfZeros >= NUMBER_OF_ZEROS_UNTIL_FLUSH) {
    logLn(true);
    numberOfZeros = 0;
    if (*lastLine && *prevLastLine) {
      const char* c = strstr(zerosBuffer, prevLastLine);
      if (c) {
        c = strstr(c, lastLine);
        if (c) {
          while (*c != '\n' && *c != '\r') ++c;
          while (*c == '\n' || *c == '\r') ++c;
        }
      }
      *lastLine = *prevLastLine = '\0';
      if (!c) c = zerosBuffer;
      *zerosFileOut << c;
    } else *zerosFileOut << zerosBuffer;
    zerosFileOut->flush();
    memset(zerosBuffer, 0, BUFFER_ZEROS);
    zerosOut->rdbuf2()->seekoff2(0, ios::beg, ios::in|ios::out);
    zerosOut->freeze(false);
  }
  previousZerosPos = zerosOut->pcount();
  *previousZeros = '\0';
}

void Output::logLn(bool flush)
{
  if (++numberOfLines >= NUMBER_OF_LINES_UNTIL_FLUSH || flush) {
    numberOfLines = 0;
    strstream2* sOut = (strstream2*)logOut;
    *logFileOut << logBuffer;
    logFileOut->flush();
    memset(logBuffer, 0, BUFFER_LOG);
    sOut->rdbuf2()->seekoff2(0, ios::beg, ios::in|ios::out);
    sOut->freeze(false);
  }
}

void Output::setupPrecision(double g)
{
  while (g >= outLess) {
    outLess *= 10;
    zerosOut->precision(++outPrecision);
    cout.precision(outPrecision);
    logOut->precision(outPrecision);
  }
}

void Output::checkPrecision(ostream* fout, double g)
{
  if (OUTPUT_ZEROS) {
    if (g >= outLess) {
      outLess *= 10;
      fout->precision(++outPrecision);
      cout.precision(outPrecision);
      logOut->precision(outPrecision);
    }
    *fout << '.' << g;
  }
}

#ifdef _OUTPUT_EXACT_ZEROS_
ofstream foutExactZeros("exact_zeta_zeros");

void outputExactZero(double a, double za, double b, double zb)
{
  if (!EvalZeta::running) return;
  while (b-a > 0.0005) {
    double c = (a+b)/2;
    double t = EvalZeta::running->evalZ(c);
    if (t == 0) t = EvalZeta::running->evalDZ(c);
    if (za > 0 && t > 0 || za < 0 && t < 0) {
      a = c; za = t;
    } else {
      b = c; zb = t;
    }
  }
  a = (a+b)/2;
  longlong l = a;
  a -= l; a *= 10000; a += 5; a /= 10;
  foutExactZeros << l << '.';
  foutExactZeros.width(3);
  foutExactZeros.fill('0');
  foutExactZeros << int(a) << endl;
}

void outputExactZero(double a, double b)
{
  if (!EvalZeta::running) return;
  double at = EvalZeta::running->evalZ(a);
  if (at == 0) at = EvalZeta::running->evalDZ(a);
  double bt = EvalZeta::running->evalZ(b);
  if (bt == 0) bt = EvalZeta::running->evalDZ(b);
  outputExactZero(a, at, b, bt);
}
#endif

void Output::output(gramBlockType& gramBlock)
{
#ifdef _OUTPUT_EXACT_ZEROS_
  outputExactZero(gramBlock[0].t, gramBlock[0].z, gramBlock[1].t, gramBlock[1].z);
#endif
  zerosFlush(false);
  gramBlock[1].zeros = 1;
  *zerosOut << '1';
  checkPrecision(zerosOut, gramBlock[1].t);
  *zerosOut << '\n';
  ++numberOfZeros;
}

void Output::output(double g2, gramBlockType& gramBlock)
{
  char prevZeros[255];
  strcpy(prevZeros, previousZeros);
  zerosFlush(false);
  int j;
  bool foundZeros = true;
  const int lbloc = gramBlock.size()-1;
  for (int i = 0; i < lbloc; ++i) {
    double g0 = gramBlock[i].t;
    double g1 = gramBlock[i+1].t;
    if (i == 0 || i == lbloc-1) {
      if (g2 >= g0 && g2 <= g1) {
#ifdef _OUTPUT_EXACT_ZEROS_
        outputExactZero(g0, g2);
        outputExactZero(g2, g1);
#endif
        foundZeros = (i > 0);
        gramBlock[i+1].zeros = 2;
        *zerosOut << ((foundZeros)? '0' : '2');
        for (j = 1; j < lbloc-1; ++j) *zerosOut << ((*prevZeros)? prevZeros[j] : '1');
        *zerosOut << ((foundZeros)? '2' : '0');
        checkPrecision(zerosOut, g2);
        *zerosOut << '\n' << ((foundZeros)? '0' : '2');
        for (j = 1; j < lbloc-1; ++j) *zerosOut << ((*prevZeros)? prevZeros[j] : '1');
        *zerosOut << ((foundZeros)? '2' : '0');
        checkPrecision(zerosOut, g1);
        *zerosOut << '\n';
      } else gramBlock[i+1].zeros = 0;
    } else if (*prevZeros) {
      gramBlock[i+1].zeros = prevZeros[i]-'0';
      char* posZeros = prevZeros;
      if (i > 1) {
        int anz = (g2 < g1)? -2 : 0;
        for (int k = 0; k < i; ++k) {
          anz += gramBlock[k+1].zeros;  // since n=18776201993
        }
        while (*posZeros && (*posZeros++ != '\n' || --anz));  // cannot search for '.' since n=418937753840 (one upper bound is equal to an integer)
      }
      if (*posZeros) {
        for (int k = 0; k < gramBlock[i+1].zeros; ++k) {
          posZeros = strchr(posZeros, '.');
          if (posZeros) {
            char* p = strchr(posZeros, '\n');
            if (p) {
              *p = '\0';
              *zerosOut << ((foundZeros)? '0' : '2');
              for (int j = 1; j < lbloc-1; ++j) *zerosOut << prevZeros[j];
              *zerosOut << ((foundZeros)? '2' : '0');
              *zerosOut << posZeros << '\n';
              posZeros = p+1;
              *p = '\n';
            }
          }
        }
      }
    } else {
#ifdef _OUTPUT_EXACT_ZEROS_
  outputExactZero(g0, gramBlock[i].z, g1, gramBlock[i+1].z);
#endif
      gramBlock[i+1].zeros = 1;
      *zerosOut << ((foundZeros)? '0' : '2');
      for (j = 1; j < lbloc-1; ++j) *zerosOut << '1';
      *zerosOut << ((foundZeros)? '2' : '0');
      checkPrecision(zerosOut, g1);
      *zerosOut << '\n';
    }
  }
  numberOfZeros += lbloc;
}

void Output::output2(double g2, double g3, gramBlockType& gramBlock)
// Rosser's rule are not satisfied
{
#ifdef _OUTPUT_EXACT_ZEROS_
  cerr << "ERROR in output!" << endl;
  exit(1);
#endif
  zerosFlush(false);
  int j;
  bool foundZeros = true;
  const int lbloc = gramBlock.size()-1;
  for (int i = 0; i < lbloc; ++i) {
    double g0 = gramBlock[i].t;
    double g1 = gramBlock[i+1].t;
    if (i == 0 || i == lbloc-1) {
      if (g2 >= g0 && g2 <= g1) {
        foundZeros = (i > 0);
        gramBlock[i+1].zeros = 2;
        *zerosOut << '2';
        for (j = 1; j < lbloc-1; ++j) *zerosOut << '1';
        *zerosOut << '2';
        checkPrecision(zerosOut, g2);
        *zerosOut << '\n' << '2';
        for (j = 1; j < lbloc-1; ++j) *zerosOut << '1';
        *zerosOut << '2';
        checkPrecision(zerosOut, g1);
        *zerosOut << '\n';
        g2 = g3;
      } else gramBlock[i+1].zeros = 0;
    } else {
      gramBlock[i+1].zeros = 1;
      *zerosOut << '2';
      for (j = 1; j < lbloc-1; ++j) *zerosOut << '1';
      *zerosOut << '2';
      checkPrecision(zerosOut, g1);
      *zerosOut << '\n';
    }
  }
  numberOfZeros += lbloc;
}

void Output::output(int n, const double* t, gramBlockType& gramBlock)
// sizeof(t) == n where in 3,4
{
  char prevZeros[255];
  strcpy(prevZeros, previousZeros);
  zerosFlush(false);

  const int lbloc = gramBlock.size()-1;
  int idx;
  for (idx = lbloc-2; idx > 0; --idx) {
    if (gramBlock[idx+1].zeros == 0) gramBlock[idx+1].zeros = 1;
  }
  gramBlock[1].zeros = (*prevZeros)? (prevZeros[0]-'0') : 0;
  gramBlock[lbloc].zeros = (*prevZeros)? (prevZeros[lbloc-1]-'0') : 0;
  for (idx = 0; idx < lbloc; ++idx) {
    if (t[0] >= gramBlock[idx].t && t[0] <= gramBlock[idx+1].t) break;
  }
  for (int i = 0; i < lbloc; ++i) {
    double g0 = gramBlock[i].t;
    double g1 = gramBlock[i+1].t;
    if (i == idx) {
      gramBlock[i+1].zeros = n;
      for (int k = 0; k < n; ++k) {
        for (int j = 0; j < lbloc; ++j) {
          if (j == idx) *zerosOut << n;
          else if (j == 0 || j == lbloc-1) *zerosOut << ((*prevZeros)? prevZeros[j] : '0');
          else *zerosOut << gramBlock[j+1].zeros;
        }
#ifdef _OUTPUT_EXACT_ZEROS_
  if (k == 0) outputExactZero(g0, t[0]);
  else if (k == n-1) outputExactZero(t[n-2], g1);
  else outputExactZero(t[k-1], t[k]);
#endif
        checkPrecision(zerosOut, (k == n-1)? g1 : t[k]);
        *zerosOut << '\n';
      }
    } else {
      char* posZeros = prevZeros;
      char* p = 0;
      if (*posZeros) {
        int anz = 0;
        if (i == 0 && prevZeros[0] == '0') continue;
        for (int j = 0; j < i; ++j) anz += prevZeros[j]-'0';
        if (anz) while (*posZeros && (*posZeros++ != '\n' || --anz));
        posZeros = strchr(posZeros, '.');
        p = (posZeros)? strchr(posZeros, '\n') : 0;
      }
      for (int k = 0; k < gramBlock[i+1].zeros; ++k) {
        for (int j = 0; j < lbloc; ++j) {
          if (j == idx) *zerosOut << n;
          else if (j == 0 || j == lbloc-1) *zerosOut << ((*prevZeros)? prevZeros[j] : '0');
          else *zerosOut << gramBlock[j+1].zeros;
        }
        if (posZeros && *posZeros && p) {
          *p = '\0';
          *zerosOut << posZeros << '\n';
          posZeros = strchr(p+1, '.');
          *p = '\n';
          p = (posZeros)? strchr(posZeros, '\n') : 0;
#ifdef _OUTPUT_EXACT_ZEROS_
  cerr << "ERROR in output!" << endl;
  exit(1);
#endif
        } else {
#ifdef _OUTPUT_EXACT_ZEROS_
          outputExactZero(g0, gramBlock[i].z, g1, gramBlock[i+1].z);
#endif
          checkPrecision(zerosOut, g1);
        }
        *zerosOut << '\n';
      }
    }
  }
  numberOfZeros += lbloc;
}

void Output::outputError(const gramBlockType& gramBlock)
{
  const int lbloc = gramBlock.size()-1;
  for (int i = 1; i <= lbloc; ++i) {
    *zerosOut << '0';
    for (int j = 1; j < lbloc-1; ++j) *zerosOut << '1';
    *zerosOut << '0';
    checkPrecision(zerosOut, gramBlock[i].t);
    *zerosOut << '\n';
  }
  numberOfZeros += lbloc;
  zerosFlush(false);
}

void Output::removePreviousOutput()
{
  int idx = zerosOut->pcount();
  if (idx > previousZerosPos) {
    strncpy(previousZeros, zerosBuffer+previousZerosPos, idx-previousZerosPos);
    previousZeros[idx-previousZerosPos] = '\0';
    memset(zerosBuffer+previousZerosPos, 0, idx-previousZerosPos);
    zerosOut->rdbuf2()->seekoff2(previousZerosPos, ios::beg, ios::in|ios::out);
    zerosOut->freeze(false);
  }
}

void Output::adjustPreviousOutput()
{
  int idx = zerosOut->pcount();
  if (idx > previousZerosPos) {
    int i = idx;
    while (--i > 0 && zerosBuffer[i] != '.');
    while (--i > 0 && zerosBuffer[i] != '.');
    char* p = strstr(zerosBuffer, zerosBuffer+i);
    if (p && p != zerosBuffer+i) {  // ToDo: works only when the last line is equal; maybe pattern "20" is wrong
      while (--p > zerosBuffer && *p != '\r' && *p != '\n');
      if (*++p == '1' && p[1] == '.' || p[2] == '.' && (p[0] == '0' && p[1] == '2' || p[0] == '2' && p[1] == '0')) { // ToDo: not fix
        char* q = new char[idx-previousZerosPos];
        if (q) {
          strncpy(q, zerosBuffer+previousZerosPos, idx-previousZerosPos);
          char* p2 = p;
          if (p[2] == '.') {
            while (p > zerosBuffer && *--p != '\r' && *p != '\n');
            while (p > zerosBuffer && (*--p == '\r' || *p == '\n'));
            while (p > zerosBuffer && *--p != '\r' && *p != '\n');
            if (*p == '\r' || *p == '\n') ++p;
          }
          while (*++p2 != '\r' && *p2 != '\n');
          while (*++p2 == '\r' || *p2 == '\n');
          char* p3 = zerosBuffer+idx-(p2-p);
          while (p3 != zerosBuffer+idx) *p3++ = '\0';
          p3 = zerosBuffer+idx-(p2-p);
          const char* p4 = zerosBuffer+previousZerosPos;
          while (p4 != p2) *--p3 = *--p4;
          strncpy(p, q, idx-previousZerosPos);
          delete[] q;
          previousZerosPos = idx -= p2-p;
          *previousZeros = '\0';
          zerosOut->rdbuf2()->seekoff2(previousZerosPos, ios::beg, ios::in|ios::out);
          zerosOut->freeze(false);
        }
      }
    }
  }
}

void Output::debug(longlong n, const char* filename)
{
  ofstream fout(filename, ios::app);
  ofstream* zerosFileOutBackup = zerosFileOut;
  fout << n << ':' << '\n';
  zerosFileOut = &fout;
  zerosFlush(true);
  zerosFileOut = zerosFileOutBackup;
}
