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


#include <cmath>
#include <cstdio>
#include <fstream>
#include <map>
#include <vector>

#include "gram.h"
#include "statistic.h"
#include "zeta_zeros.h"


static ofstream* foutStatistic = 0;

static void closeStatistic()
{
/*  if (foutStatistic) {
    foutStatistic->close();
    delete foutStatistic;
    foutStatistic = 0;
  }*/
}

// Format tmp:
// <lastZero>;<n>;<zeros>;<zero>;<anz>;<types>;<typesCount>
//
// <lastZero>  ::= <Rosser block length>.<zero>
// <n>         ::= index of the last known zero
// <zeros>     ::= <zeroX>{:<zeroX>}^*
// <zeroX>     ::= number of Gram intervals containing excatly m zeros{,dito}^*
// <zero>      ::= number of Gram intervals containing excatly m zeros{,dito}^*
// <anz>       ::= number of Rosser blocks of given length
// <types>     ::= <type>{:<type>}^*
// <typesCount>::= <type>{:<type>}^*
// <type>      ::= <string>,<int>
string statistic(const char* tmp, const char* directory, bool removeLine)
{
  if (foutStatistic == 0) {
    foutStatistic = new ofstream("statistic.log", ios::app);
  }

#ifdef _STATISTIC_FILE_
  cout = *foutStatistic;
#endif
  cout.precision(15);
  cerr.precision(15);
  map<string, longlong> types,typesCount;
  vector<vector<longlong> > zeros;
  string dir = directory;
  dir += "zeta_zeros.txt";
  ifstream fin2(dir.c_str());
  if (!fin2) {
    cerr << "Fatal error ('zeta_zeros.txt')!." << endl;
    exit(1);
  }
  char s[255],s2[255],t[255];
  // find last line (maybe Rossers rule)
  fin2.seekg(0, ios::end);
  const int pos = fin2.tellg();
  if (pos <= 1000) {
    cerr << "File too small ('zeta_zeros.txt')!." << endl;
    exit(1);
  }
  *s2 = *s = '\0';
  fin2.seekg(pos-1000);
  while (!fin2.eof()) {
    fin2.getline(t, 255);
    if (fin2.fail()) break;
    if (t[0] == '1' && t[1] == '.' && (s2[0] == '\0' || s2[0] == '1' && s2[1] == '.')) {
      strcpy(s, t);
    } else {
      strcpy(s2, t);
    }
  }
  fin2.close();
  if (*s == '\0') {
    cerr << "No safe last line found ('zeta_zeros.txt')!." << endl;
    exit(1);
  }
  string lastLine = s;
  ifstream fin(dir.c_str());
  if (!fin) {
    cerr << "Fatal error ('zeta_zeros.txt')!." << endl;
    exit(1);
  }
  cout << "Number of Rosser blocks of given length" << endl;
  vector<longlong> zero,anz;
  longlong n = -2;
  string result = "";
  double lastZero = 14.1;
  if (tmp) {
    while (*tmp != ';') result += *tmp++;
    if (result.length() > 0) {
      lastZero = atof(result.substr(result.find('.')+1).c_str());
    }
    n = 0;
    while (*++tmp != ';') n = n*10 + (*tmp-'0');
    ++tmp;
    while (true) {
      vector<longlong> z;
      while (*tmp != ':' && *tmp != ';') {
        longlong k = 0;
        while (*tmp != ',' && *tmp != ':' && *tmp != ';') { k = k*10 + (*tmp-'0'); ++tmp; }
        z.push_back(k);
        if (*tmp == ',') ++tmp;
      }
      zeros.push_back(z);
      if (*tmp == ';') break;
      ++tmp;
    }
    ++tmp;
    while (true) {
      longlong k = 0;
      while (*tmp != ',' && *tmp != ';') { k = k*10 + (*tmp-'0'); ++tmp; }
      zero.push_back(k);
      if (*tmp == ';') break;
      ++tmp;
    }
    ++tmp;
    while (true) {
      longlong k = 0;
      while (*tmp != ',' && *tmp != ';') { k = k*10 + (*tmp-'0'); ++tmp; }
      anz.push_back(k);
      if (*tmp == ';') break;
      ++tmp;
    }
    ++tmp;
    while (true) {
      string s;
      while (*tmp && *tmp != ',') s += *tmp++;
      if (*tmp == '\0') {
        cerr << "Fatal error ('tmp')!" << endl;
        exit(1);
      }
      longlong k = 0;
      bool minus = false;
      if (tmp[1] == '-') {
        minus = true;
        ++tmp;
      }
      while (*++tmp != ':' && *tmp && *tmp != ';') k = k*10 + (*tmp-'0');
      if (*tmp == '\0') {
        cerr << "Fatal error ('tmp')!" << endl;
        exit(1);
      }
      if (minus) k = -k;
      types.insert(make_pair(s, k));
      if (*tmp == ';') break;
      ++tmp;
    }
    ++tmp;
    while (true) {
      string s;
      while (*tmp != ',') s += *tmp++;
      longlong k = 0;
      while (*++tmp && *tmp != ':') k = k*10 + (*tmp-'0');
      typesCount.insert(make_pair(s, k));
      if (*tmp == 0) break;
      ++tmp;
    }
  } else {
    zero.push_back(0); anz.push_back(0);
    zero.push_back(-1); anz.push_back(-1);
  }
  int k = 1;
  int idxZero = -1;
  char stringZero[200];
  int stringZeroLength = 0;
  bool first = true;
  double addition = 0.0;
  char frontResult[255];
  *frontResult = '\0'; *t = '\0';
  if (result.length() > 0) {  // skip to previous calculated zero
    const int lenResult = strlen(result.c_str());
    bool resultPoint = (lenResult > 3 && (result[lenResult-2] == '.' || result[lenResult-3] == '.' || result[lenResult-4] == '.'));
    while (!fin.eof() && result != t) {
      const int l = strlen(t);
      if (l > 1 && t[l-1] != '.' && t[l-2] != '.') {
        if (lenResult == l && resultPoint && (t[l-3] == '.' || t[l-4] == '.')) {
          if (strncmp(t, result.c_str(), l-1) == 0) break;
        } else if (lenResult == l && !resultPoint && l > 4 && t[l-3] != '.' && t[l-4] != '.') {
          if (strncmp(t, result.c_str(), l) == 0) break;
        } else if (strncmp(t, result.c_str(), min(lenResult-1, l-1)) == 0) {
          const char* s2 = strchr(t, '.');
          const char* s3 = strchr(result.c_str(), '.');
          if (s2 && s3 && fabs(atof(s2+1)-atof(s3+1)) < 0.01) break;
        }
      }
      fin.getline(s, 255);
      if (*s == '\0') continue;
      char* s2 = strchr(s, '.');
      if (s2 == 0) continue;
      if (first) {
        if (*s == '.') {
          strcpy(frontResult, s+1);
          addition = atof(frontResult);
          fin.getline(s, 255);
        }
        s2 = strchr(s, '.');
        const char* s3 = strchr(s2+1, '.');
        if (addition > 0) {
          int count = (s3)? s3-s2 : strlen(s2);
          while (--count > 0) addition *= 10;
        }
        first = false;
      }
      if (*frontResult == '\0') {
        strcpy(t, s);
      } else {
        const char x = s2[1];
        s2[1] = '\0'; strcpy(t, s); s2[1] = x; strcat(t, frontResult); strcat(t, s2+1);
      }
    }
  }
  longlong nstart = 100;
  while (nstart*10 <= n) nstart *= 10;
  longlong n2 = nstart;
  if (9*nstart <= n) n2 = nstart *= 10;
  else {
    for (k = 9; k > 1; --k) {
      if ((k-1)*nstart <= n) { n2 *= k; break; }
    }
  }
  k = 1;
  double g0,g1 = Gram(n+1, lastZero);
  bool exception = false;
  longlong countZeros = 0;
  longlong nProblem = 0;
  char sProblem[255];
  *s = 0;
  while (!fin.eof()) {
    fin.getline(s, 255);
    if (first) {
      if (*s == '.') {
        strcpy(frontResult, s+1);
        addition = atof(frontResult);
        fin.getline(s, 255);
      }
    }
    if (*s == '\0' || *s == '.') continue;
    if (lastLine == s) break;
    // Rosser block B_n = [g_n,g_(n+k)[ of length k
    char* s2 = s;
    while (*s2 != '\0' && isdigit(*s2)) ++s2;
    if (*s2 != '.') continue;
    bool reducedGramPoints = (s2[1] == '.' || s2[1] == '\0');
    double d = atof(s2+1);
    const char* s3 = strchr(s2+1, '.');
    if (!reducedGramPoints) {
      if (first && addition > 0) {
        int count = (s3)? s3-s2 : strlen(s2);
        while (--count > 0) addition *= 10;
      }
      first = false; d += addition;
      if (s3 == 0 && d < lastZero) {    // Error in file
        cout << "Error in file: n=" << n << ", d=" << d << ", lastZero=" << lastZero << ", s=" << s << endl;
        continue;
      }
      if (d < lastZero || d == lastZero && s2-s == 1 && *s == '1') {
        // duplicate zeros
        cout << "duplicate zeros: n=" << n << ", d=" << d << ", lastZero=" << lastZero << ", s=" << s << endl;
        char c[25];
        result += ';';
        result += lltoa(n, c, 10);
        closeStatistic();
        return result;
      }
    }
    if (idxZero+1 < stringZeroLength) {
      if (stringZeroLength != s2-s) {
        cout << "Error in file: n=" << n << ", stringZeroLength=" << stringZeroLength << ", s=" << s << ", lastZero=" << lastZero << endl;
        char c[25];
        result += ';';
        result += lltoa(n, c, 10);
        closeStatistic();
        return result;
      }
      int k2 = stringZero[++idxZero]-'0';
      while (k2 >= zero.size()) zero.push_back(0);
      ++zero[k2];
      if (exception && ++n >= n2) {
        ofstream foutGram("rosser_blocks.txt", ios::app);
        foutGram << setw(20) << n << ':';
        cout << setw(20) << n << ':';
        vector<longlong> z;
        z.push_back(zero[0]);
        int i;
        for (i = 1; i < anz.size(); ++i) {
          foutGram << setw(20) << anz[i];
          cout << setw(20) << anz[i];
        }
        foutGram << endl;
        cout << endl;
        for (i = 1; i < zero.size(); ++i) {
          z.push_back(zero[i]);
        }
        n2 += nstart;
        if (nstart*10 == n2) nstart *= 10;
        zeros.push_back(z);
      }
    }
    if (!reducedGramPoints) lastZero = d;
    if (*frontResult == '\0') result = s;
    else {
      const char x = s2[1];
      s2[1] = '\0'; result = s; s2[1] = x; result += frontResult; result += s2+1;
    }
    if (exception && k > 1) { --k; continue; }
    if (++n >= n2) {
      ofstream foutGram("rosser_blocks.txt", ios::app);
      foutGram << setw(20) << n << ':';
      cout << setw(20) << n << ':';
      vector<longlong> z;
      z.push_back(zero[0]);
      int i;
      for (i = 1; i < anz.size(); ++i) {
        foutGram << setw(20) << anz[i];
        cout << setw(20) << anz[i];
      }
      foutGram << endl;
      cout << endl;
      for (i = 1; i < zero.size(); ++i) {
        z.push_back(zero[i]);
      }
      n2 += nstart;
      if (nstart*10 == n2) nstart *= 10;
      zeros.push_back(z);
    }
    g0 = g1; g1 = Gram(n+1, lastZero);
    if (--k == 0) {
      k = strchr(s, '.')-s;
      if (k == 1 && *s == '1' && !reducedGramPoints && (d <= g0-0.001 || d >= g1+0.001)) {   // Error! Missing zeros.
        cout << "Error: n=" << n << ", g0=" << g0 << ", d=" << d << ", g1=" << g1 << endl;
        ofstream fout("error.log", ios::app);
        fout << n << endl;
        fout.close();
        char c[25];
        result += ';';
        result += lltoa(n, c, 10);
        closeStatistic();
        return result;
      }
      if ((n&1023) == 0) {
        longlong sum = 0;
        for (int i = 0; i < anz.size(); ++i) sum += i*anz[i];
        if (sum != n) {
          cout << "Error: n=" << n << ", sum=" << sum << endl;
          exit(1);
        }
      }

      int k2 = s[0]-'0';
      while (k2 >= zero.size()) zero.push_back(0);
      while (k >= anz.size()) anz.push_back(0);
      idxZero = 0; ++zero[k2]; ++anz[k];
      // check if it is an exception to Rosser's rule
      for (int k3 = 1; k3 < k; ++k3) k2 += s[k3]-'0';
      exception = false;
      strncpy(stringZero, s, k); stringZero[k] = '\0';
      char s3[31];
      memset(s3, ' ', 30);
      strcpy(s3+30-k, stringZero);
      string s2(s3);
      stringZeroLength = k;
      map<string, longlong>::iterator i = types.find(s2);
      if (i == types.end()) types.insert(make_pair(s2, n));
      i = typesCount.find(s2);
      if (i == typesCount.end()) typesCount.insert(make_pair(s2, longlong(1)));
      else ++(*i).second;
      countZeros += k2-k;
      if (nProblem > 0 && n-nProblem > 20) break;
      if (countZeros == 0) nProblem = 0;
      else if (nProblem == 0) {
        nProblem = n;
        strcpy(sProblem, s);
      }
      exception = (k2 != k);
      if (k2 > k) k = k2;
    }
  }
  if (nProblem > 0) {
    char c[25];
    result += ';';
    result += lltoa(nProblem, c, 10);
    closeStatistic();
    return result;
  }

  char c[25];
  result += ';';
  result += lltoa(n, c, 10);
  result += ';';

  cout << "\nNumber of Gram intervals containing excatly m zeros" << endl;
  n = nstart = 100;
  for (vector<vector<longlong> >::iterator iter = zeros.begin(); iter != zeros.end(); ++iter) {
    cout << setw(20) << n << ':';
    for (vector<longlong>::iterator j = (*iter).begin(); j != (*iter).end(); ++j) {
      if (*j == 0) {
        vector<longlong>::iterator k = j;
        while (++k != (*iter).end() && *k == 0);
        if (k == (*iter).end()) break;
      }
      cout << setw(20) << *j;
    }
    cout << endl;
    n += nstart;
    if (nstart*10 == n) nstart *= 10;
  }
  cout << "\nFirst occurrences and number of Rosser blocks of various types" << endl;
  countZeros = 0;
  map<string, longlong>::iterator iter2;
  for (iter2 = types.begin(); iter2 != types.end(); ++iter2) {
    map<string, longlong>::iterator j = typesCount.find((*iter2).first);
    const char* c = (*iter2).first.c_str();
    cout << c << setw(20) << (*iter2).second;
    longlong factor = 0;
    while (*c) {
      if (isdigit(*c)) factor += *c-'0'-1;
      ++c;
    }
    if (j != typesCount.end()) {
      cout << setw(20) << (*j).second;
      countZeros += factor*(*j).second;
    }
    cout << endl;
  }
  cout << "typesCount=" << typesCount.size() << ", countZeros=" << countZeros << endl;
  if (countZeros != 0) {
    longlong posN = 0;
    int delta = 10;
    string s,s2,line;
    do {
      delta -= 5;
      dir = directory;
      dir += "zeta_zeros.log";
      ifstream fin(dir.c_str());
      while (!fin.eof()) {
        getline(fin, s);
        if (strncmp(s.c_str(), "This happened between ", 22) == 0) {
          longlong n = atoll(s.c_str()+22);
          bool exist = false;
          ifstream finError("error.log");
          if (!finError) {
            cerr << "Fatal error ('error.log')!." << endl;
            exit(1);
          }
          while (!exist && !finError.eof()) {
            getline(finError, s2);
            if (s2[0] && isdigit(s2[0]) && atoll(s2.c_str()) == n) exist = true;
          } 
          finError.close();
          if (!exist) {
            if (posN+delta > n) posN = 0;
            else if (posN == 0) { posN = n; line = s; }
            else { delta = 0; break; }
          }
        }
      }
    } while (delta > 0 && posN == 0);
    if (posN == 0) {  // bug fixed in 0108
      cout << "bug fixed in 0108" << endl;
      dir = directory;
      dir += "zeta_zeros.log";
      ifstream fin(dir.c_str());
      while (!fin.eof()) {
        getline(fin, s);
        if (strncmp(s.c_str(), "Call the new rule for a Rosser block", 34) == 0) {
          getline(fin, s);
          double t = atof(s.c_str()+8);
          if (t > 0) {
            longlong n = getStartN(t)-1;
            if (posN+100 > n) posN = 0;
            else if (posN == 0) { posN = n; line = s; }
            else break;
          }
        }
      }
    }
    cout << "countZeros=" << countZeros << ", posN=" << posN << ", line=" << line.c_str() << endl;
    if (posN > 0) {
      dir = directory;
      dir += "zeta_zeros.log";
      ifstream fin(dir.c_str());
      string dir2 = directory;
      dir2 += "tmp.txt";
      ofstream fout(dir2.c_str());
      while (!fin.eof()) {
        getline(fin, s);
        if (s != line) fout << s.c_str() << '\n';
      }
      fin.close();
      fout.close();
      if (remove(dir.c_str()) != 0 || rename(dir2.c_str(), dir.c_str()) != 0) {
        cerr << "Fatal error!" << endl;
        exit(1);
      }
      result = result.substr(0, result.find(';')+1);
      result += lltoa(posN, c, 10);
      closeStatistic();
      return result;
    } else {
      exit(1);
    }
  }

  // generating result string
  int i;
  for (i = 0; i < zeros.size(); ++i) {
    if (zeros[i].size() > 0) {
      result += lltoa(zeros[i][0], c, 10);
      for (int j = 1; j < zeros[i].size(); ++j) {
        result += ',';
        result += lltoa(zeros[i][j], c, 10);
      }
    }
    if (i+1 < zeros.size()) result += ':';
  }
  result += ';';
  if (zero.size() > 0) {
    result += lltoa(zero[0], c, 10);
    for (i = 1; i < zero.size(); ++i) {
      result += ',';
      result += lltoa(zero[i], c, 10);
    }
  }
  result += ';';
  if (anz.size() > 0) {
    result += lltoa(anz[0], c, 10);
    for (i = 1; i < anz.size(); ++i) {
      result += ',';
      result += lltoa(anz[i], c, 10);
    }
  }
  result += ';';
  iter2 = types.begin();
  if (iter2 != types.end()) {
    result += (*iter2).first;
    result += ',';
    result += lltoa((*iter2).second, c, 10);
cerr << result.length() << endl;   // Bug in Visual C++ 6.0
    while (++iter2 != types.end()) {
      result += ':';
      result += (*iter2).first;
      result += ',';
      result += lltoa((*iter2).second, c, 10);
    }
  }
  result += ';';
  iter2 = typesCount.begin();
  if (iter2 != typesCount.end()) {
    result += (*iter2).first;
    result += ',';
    lltoa((*iter2).second, c, 10);
    result += c;
    while (++iter2 != typesCount.end()) {
      result += ':';
      result += (*iter2).first;
      result += ',';
      lltoa((*iter2).second, c, 10);
      result += c;
    }
  }

  closeStatistic();
  return result;
}

void endJob(Output& output, const startType& NFIRST, const startType& n, longlong newZeros, double g0, clock_t tottime,
            int nzevalu, clock_t ztime, int ndzevalu, clock_t dztime)
{
  // Preparation for some final output
  double gn = Gram(n, g0);
  double m = sqrt(gn*TWO_PI_INV);
  // n+1 is the "LASTN" for the next run.
  LOGLN(output, "NFIRST (was input for this run)          = " << NFIRST
        << "\nLASTN (input for next run)               = " << (n+1)
        << "\nthe corresponding Gram point             = " << gn
        << "\nnzevalu (number of Z-evaluations)        = " << nzevalu
        << "\nndzevalu (number of DZ-evaluations)      = " << ndzevalu
        << "\necorate                                  = " << double(nzevalu+ndzevalu)/newZeros
        << "\naverage time for one Z-evaluation        = " << double(ztime)/double(nzevalu)/CLOCKS_PER_SEC
        << "\naverage time for one DZ-evaluation       = " << ((ndzevalu == 0)? 0.0 : double(dztime)/double(ndzevalu)/CLOCKS_PER_SEC)
        << "\ntotal time used for all Z-evaluations    = " << double(ztime)/CLOCKS_PER_SEC
        << "\ntotal time used for all DZ-evaluations   = " << double(dztime)/CLOCKS_PER_SEC
        << "\ntotal time used in this run              = " << double(tottime)/CLOCKS_PER_SEC
        << "\naverage total time for one Z-evaluation  = " << double(tottime)/double(nzevalu+ndzevalu)/CLOCKS_PER_SEC
        << "\nlast m (= summation range in Z(t))       = " << m);
}

int reduceFilesize(Output* output, const char* filename)
// return: 0 - ok
//         1 - error
//         2 - exit or work unit is not completed
{
  char s[256],first[256],s2[256];
  *first = *s2 = *s = '\0';
  if (filename) {
    ifstream fin(filename);
    if (!fin) return 1;
    fin.getline(s, 255);
    if (*s != '\0' && *s != '.') {
      strcpy(first, s);
      if (output) {
        LOGLN(*output, "first line                               = " << first);
      } else cout << "first line                               = " << first << endl;
      fin.seekg(0, ios::end);
      const int pos = fin.tellg();
      if (pos <= 100) return 1;
      fin.seekg(pos-100);
      while (!fin.eof()) {
        //if (isExit()) return 2;
        fin.getline(s2, 255);
        if (fin.fail()) break;
        if (*s2) strcpy(s, s2);
      }
    }
  }
  if (strcmp(s, first)) {
    if (output) {
      LOGLN(*output, "last line                                = " << s);
    } else cout << "last line                                = " << s << endl;
    char* t1 = strchr(first, '.');
    char* t2 = strchr(s, '.');
    if (t1 && t2) {
      int i = 0;
      ++t1; ++t2;
      char lastLine[256];
      double gramPoint = atof(t1);
      const double firstGramPoint = gramPoint - 1.0;
      const double lastGramPoint = atof(t2) + 1.0;
      const char* t3 = strchr(filename, '_');
      if (t3) t3 = strchr(t3+1, '_');
      longlong n = (t3)? atoll(t3+1)-10 : -1;
      double g = 9.6669;
      bool isFirstGramPoint = true;
      if (n > 1) {
        for (g = Gram3(n, TWO_PI*n/log(double(n))); g+0.002 < gramPoint; g = Gram3(++n, g));
      }
      while (t1[i] && t1[i] == t2[i]) { s[i] = t1[i]; ++i; }
      while (*t1 && *t2 && *t1 != '.' && *t1 != ',' && *t2 != '.' && *t2 != ',') { ++t1; ++t2; }
      bool noDot = (*t1 == ',' && *t2 == ',');
      if ((noDot || *t1 == '.' && *t2 == '.' || *t1 == '\0' && *t2 == '.' || *t1 == '.' && *t2 == '\0') && i > 0) {
        s[i] = '\0';
        if (output) {
          LOGLN(*output, "reduction                                = " << s << "\nthe starting Gram point                  = " << n);
        } else cout << "reduction                                = " << s << "\nthe starting Gram point                  = " << n << endl;
        longlong n2 = n;
        int lines = 0;
        bool searchLine = false;
        *lastLine = '\0';
        char* tmpFilename = new char[strlen(filename)+4];
        if (!tmpFilename) return 0;
        strcat(strcpy(tmpFilename, filename), ".tmp");
        ofstream fout(tmpFilename);
        fout << '.' << s << '\n';
        ifstream fin(filename);
        while (!fin.eof()) {
          //if (isExit()) return 2;
          fin.getline(s2, 255);
          if (fin.fail()) {
            if (fin.eof()) break;
            string s;
            fin.clear();
            getline(fin, s);   // Error in file!
            continue;
          }
          if (*s2) {
            if (searchLine) {
              searchLine = (strcmp(s2, lastLine) != 0);
              continue;
            }
            if (s2[2] == '\0' && s2[1] == '.' && s2[0] == '1') {  // problem
              remove(tmpFilename);
              delete[] tmpFilename;
              return 1;
            }
            t1 = strchr(s2, '.');
            if (t1) {
              const int l = strlen(++t1);
              if (l > i) {
                double gramPoint = atof(t1);
                if (gramPoint <= firstGramPoint || gramPoint >= lastGramPoint) {
                  searchLine = true;
                  continue; // Error in file!
                }
                strcpy(lastLine, s2);
                *t1 = '\0';
                fout << s2;
                t1 += i;
                if (noDot) {
                  char* p = strchr(t1, ',');
                  if (p) *p = '.';
                }
                if (n > 1) {
                  while (g+0.002 < gramPoint) g = Gram3(++n, g);
                }
                if (fabs(g-gramPoint) < 0.002) {
                  if (isFirstGramPoint) {
                    fout << '.' << n << '\n';
                    isFirstGramPoint = false;
                  } else {
                    for (int j = n-n2; j > 0; --j) fout << '.';
                    fout << '\n';
                  }
                  n2 = n;
                } else {
                  t1[l-i] = '\n'; t1[l-i+1] = '\0';
                  fout << t1;
                }
                ++lines;
              }
            }
          }
        }
        fin.close();
        fout.close();
        if (output) {
          LOGLN(*output, "number of lines                          = " << lines);
        } else cout << "number of lines                          = " << lines << endl;
        if (remove(filename) == 0 && rename(tmpFilename, filename) == 0) {
          delete[] tmpFilename;
          return 0;
        }
        delete[] tmpFilename;
      }
    }
  }
  return 1;
}
