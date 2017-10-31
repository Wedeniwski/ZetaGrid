//
// Seperation of Zeros of the Riemann Zeta-Function
//
// Author: Sebastian Wedeniwski
// Date:   02/06/2001 (start)
//

#include "natural.h"
#include "modulo.h"
#include "nmbrthry.h"

#include <iostream>

using namespace std;

//const int KEY_LENGTH = 4096;
//const int KEY_SPLIT = 2040;
const int KEY_LENGTH = 1024;
const int KEY_SPLIT = 500;


bool ispspprime(const Natural& n)
// check: n <= 999997
{
  static const bool primes[32] = { 0, 0, 1, 1, 0, 1, 0, 1, 0, 0,
                                   0, 1, 0, 1, 0, 0, 0, 1, 0, 1,
                                   0, 0, 0, 1, 0, 0, 0, 0, 0, 1,
                                   0, 1 };
  static const bool mod[30] = { 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1,
                                1, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0 };
  if (n < 32) return primes[n&31];
  if (mod[n%30]) return false;

  Primes p;
  Digit prim = p.firstPrime();
  p.nextPrime(prim); p.nextPrime(prim);
  while (p.nextPrime(prim) && prim < 1000)
    if (n%prim == 0) return (n == prim);
  if (n < 997*997) return true;

  Natural x,y;
  sqrt(n, x, y);
  if (y == 0) return false;

  Natural b = 1;
  Natural c = 3;
  Natural d = 2;
  while (true) {
    const int x = jacobi(d, n);
    if (x == 0) return false;
    else if (x < 0) break;
    d += c; c += 2; ++b;
  }

  Natural m = n-1;
  m >>= 1;
  x = pow(d, m, n);
  y = n-1;
  if (x != y) return false;
  // (b + sqrt(d))^(n+1):
  c = n + 1;
  Natural t,t2,t3,b2 = 1;
  y = 1;
  Natural x2 = Digit(0);
  while (c != 0) {
    while ((c&1) == 0) {
      t = b*b; t2 = b2*b2; t += t3 = d*t2;
      t2 = b*b2; t2 <<= 1;
      b = t%n; b2 = t2%n;
      c >>= 1;
    }
    t = b+b2; t2 = y+x2; t3 = b2*x2;
    x2 = t*t2; x2 -= t = b*y; x2 -= t3;
    x2 %= n;
    t += t2 = d*t3;
    y = t%n;
    --c;
  }
  return (x2 == 0 && x == y);
}

int main(int argc, char** argv)
{
  if (argc != 2) {
    cerr << "USAGE: " << argv[0] << " <randomize>\n";
    return 1;
  }
  int randomize = atoi(argv[1]);
  if (randomize == 0) {
    cerr << "wrong parameter\n";
    return 1;
  }
  Natural p;
  p.rand(randomize);
  p.rand(KEY_LENGTH/2);
  p -= 1234220; p -= 81782;
  if (p.even()) --p;
  while (!ispspprime(p)) { cout << ':' << flush; p -= 2; }
  Natural q,n;
  q.rand(randomize);
  q.rand(KEY_LENGTH/2);
  q += 1112112;
  if (q < p) q += p;
  if (q.even()) --q;
  while (true) {
    if (ispspprime(q)) {
      n = p*q; n <<= 1; n |= 1;
      if (ispspprime(n)) break;
      cout << '.' << flush;
    }
    q -= 2;
  }
  Natural g;
  Natural m = n;
  n >>= 1; p <<= 1; q <<= 1;
  Natural b = 1;
  do {
    g.rand(KEY_LENGTH-1); g %= m;
    cout << ':' << flush;
  } while (pow(g, p, m) == b || pow(g, q, m) == b || pow(g, n, m) == b);

  Natural a;
  a.rand(KEY_LENGTH-1); a %= m;
  p >>= 1; q >>= 1;

  char c[10000];
  cout << "\nstruct Key {\n//private: p := 2*p*q+1, A == g^a (mod p)\n//  p = atoN(\"" << Ntoa(p, c, 32) << "\", 32);\n";
  cout << "//  q = atoN(\"" << Ntoa(q, c, 32) << "\", 32);\n";
  cout << "//  g = atoN(\"" << Ntoa(g, c, 32) << "\", 32);\n";
  cout << "//  a = atoN(\"" << Ntoa(a, c, 32) << "\", 32);\n  Natural p,g,A;\n\n  Key()\n  {  \n";
  Natural A = pow(g, a, m);
  n = m >> KEY_SPLIT; m -= n << KEY_SPLIT;
  cout << "    //public:\n    p = atoN(\"" << Ntoa(n, c, 32) << "\", 32);\n    p <<= " << KEY_SPLIT << "; p |= atoN(\"";
  cout << Ntoa(m, c, 32) << "\", 32);\n\n";
  n = g >> KEY_SPLIT; g -= n << KEY_SPLIT;
  cout << "    g = atoN(\"" << Ntoa(n, c, 32) << "\", 32);\n    g <<= " << KEY_SPLIT << "; g |= atoN(\"";
  cout << Ntoa(g, c, 32) << "\", 32);\n\n";
  n = A >> KEY_SPLIT; A -= n << KEY_SPLIT;
  cout << "    A = atoN(\"" << Ntoa(n, c, 32) << "\", 32);\n    A <<= " << KEY_SPLIT << "; A |= atoN(\"";
  cout << Ntoa(A, c, 32) << "\", 32);\n  }\n} key;" << endl;
  return 0;
}
