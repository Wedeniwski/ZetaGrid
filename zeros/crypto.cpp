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


#include "natural.h"
#include "modulo.h"
#include "key.h"

#include <jni.h>
#include <fstream>
#include <strstream>
#include <string>
#include <cstdio>

using namespace std;


extern "C" void bzip2Compress(const char* filename);
extern "C" void bzip2Uncompress(const char* filename);


// BETA == 32
void hashCode(istream& in, const Natural& p, Natural& hash)
{
  in.seekg(0, ios::end);
  const size_t sz = p.length()-1;
  size_t size = in.tellg();
  in.seekg(0);
  Natural n = 0;
  hash = 0;
  size_t i = 0;
  size_t j = 0;
  while (i < size) {
    unsigned char stream[4];
    in.get((char&)stream[0]); in.get((char&)stream[1]);
    in.get((char&)stream[2]); in.get((char&)stream[3]);
    Digit d = stream[0];
    if (i+3 < size) d |= (Digit(stream[1]) << 8) | (Digit(stream[2]) << 16) | (Digit(stream[3]) << 24);
    else if (i+2 < size) d |= (Digit(stream[1]) << 8) | (Digit(stream[2]) << 16);
    else if (i+1 < size) d |= stream[1] << 8;
    n.lmove(1); n |= d; i += 4;
    if (++j == sz || i >= size) {
      hash += n;
      if (hash >= p) hash -= p;
      n = 0; j = 0;
    }
  }
}


void generateSignature(int randomize, istream& privateKey, istream& in,
                       const Natural& p, const Natural& g, ostream& out)
{
  Natural a,k;
  if (!a.scan(privateKey)) return;
  k.rand(randomize);
  Natural q = p;
  --q;
  do {
    k.rand(1022);
  } while (gcd(k, q) > 1);
  Natural r = pow(g, k, p);
  out << print(r);
  Natural h;
  hashCode(in, p, h);
  r *= a; r %= q;
  if (h < r) h += q;
  h -= r;
  h *= inverse(k, q);
  h %= q;
  out << print(h);
}

bool verification(istream& signature, istream& in, const Natural& p, const Natural& g, const Natural& A)
{
  Natural r,s;
  if (!r.scan(signature) || !s.scan(signature)) return false;
  if (r == 0 || r >= p) return false;
  r = pow(A, r, p)*pow(r, s, p);
  r %= p;
  Natural h;
  hashCode(in, p, h);
  return (r == pow(g, h, p));
}

// BETA == 32
void zeta_encrypt(istream& in, ostream& out, const Natural& g, const Natural& E, const Natural& p)
{
  in.seekg(0, ios::end);
  const size_t sz = p.length()-1;
  size_t size = in.tellg();
  out << size << ';';
  Natural x;
  do {
    x.rand(KEY_LENGTH-1);
    x %= key.p;
    x -= 2;
  } while (x.length() < 3);

  out << print(pow(g, x, p), true);
  x = pow(E, x, p);
  in.seekg(0);
  Natural n = 0;
  size_t i = 0;
  size_t j = 0;
  while (i < size) {
    unsigned char stream[4];
    in.get((char&)stream[0]); in.get((char&)stream[1]);
    in.get((char&)stream[2]); in.get((char&)stream[3]);
    Digit d = stream[0];
    if (i+3 < size) d |= (Digit(stream[1]) << 8) | (Digit(stream[2]) << 16) | (Digit(stream[3]) << 24);
    else if (i+2 < size) d |= (Digit(stream[1]) << 8) | (Digit(stream[2]) << 16);
    else if (i+1 < size) d |= stream[1] << 8;
    n.lmove(1); n |= d; i += 4;
    if (++j == sz || i >= size) {
      n *= x; n %= p;
      out << print(n, true);
      n = 0; j = 0;
    }
  }
}

bool decrypt(istream& in, ostream& out, const Natural& D, const Natural& p)
{
  const size_t sz = p.length()-1;
  size_t size = 0;
  char c;
  while (in.get(c) && isdigit(c)) size = size*10 + c-'0';
  if (size == 0) return false;
  Natural x;
  if (!x.scan(in)) return false;
  x = inverse(pow(x, D, p), p);
  unsigned char* result = new unsigned char[4*sz];
  size_t j = 0;
  Natural n;
  while (!in.eof()) {
    if (!n.scan(in)) break;
    n *= x; n %= p;
    Natural::rep r = print(n);
    const Digit* p = r.p;
    j += 4*sz;
    if (j >= size) {
      j -= 4*sz; size -= j;
      unsigned char* tmp = result+size;
      size_t i = r.size;
      Digit d = p[--i];
      switch (size&3) {
        case 3: *--tmp = (d >> 16)&0xff;
        case 2: *--tmp = (d >> 8)&0xff;
        case 1: *--tmp = d&0xff; break;
        case 0: ++i;
      }
      while (tmp != result) {
        tmp -= 4;
        if (i == 0) {
          tmp[0] = tmp[1] = tmp[2] = tmp[3] = 0;
        } else {
          const Digit d = p[--i];
          tmp[0] = d&0xff; tmp[1] = (d >> 8)&0xff;
          tmp[2] = (d >> 16)&0xff; tmp[3] = (d >> 24)&0xff;
        }
      }
      out.write((const char*)result, size);
    } else {
      unsigned char* tmp = result+4*sz;
      size_t i = r.size;
      do {
        tmp -= 4;
        if (i == 0) {
          tmp[0] = tmp[1] = tmp[2] = tmp[3] = 0;
        } else {
          const Digit d = p[--i];
          tmp[0] = d&0xff; tmp[1] = (d >> 8)&0xff;
          tmp[2] = (d >> 16)&0xff; tmp[3] = (d >> 24)&0xff;
        }
      } while (tmp != result);
      out.write((const char*)result, 4*sz);
    }
  }
  delete[] result;
  return true;
}

extern "C" void generateSignature(int randomize, const char* privateKey, const char* in, const char* out)
{
  ifstream finKey(privateKey);
  if (!finKey) { cerr << "wrong file " << privateKey << '\n'; return; }
  ifstream fin(in, ios::binary | ios::in);
  if (!fin) { cerr << "wrong file " << in << '\n'; return; }
  ofstream fout(out);
  if (!fout) { cerr << "wrong file " << out << '\n'; return; }
  generateSignature(randomize, finKey, fin, key.p, key.g, fout);
}

extern "C" bool verification(const char* signature, const char* in)
{
  ifstream finSig(signature);
  if (!finSig) { cerr << "wrong file " << signature << '\n'; return false; }
  ifstream fin(in, ios::binary | ios::in);
  if (!fin) { cerr << "wrong file " << in << '\n'; return false; }
  return verification(finSig, fin, key.p, key.g, key.A);
}

extern "C" void zeta_encrypt(int randomize, const char* in, const char* out)
{
  while (--randomize > 0) rand();
  string inZip = in;
  inZip += ".bz2";
  remove(inZip.c_str());
  bzip2Compress(in);
  ifstream fin(inZip.c_str(), ios::binary | ios::in);
  if (!fin) {
    cerr << "wrong file " << in << '\n'; return;
  }
  ofstream fout(out, ios::binary | ios::out);
  if (!fout) {
    cerr << "wrong file " << out << '\n'; return;
  }
  zeta_encrypt(fin, fout, keyEncrypt.g, keyEncrypt.A, keyEncrypt.p);
  fin.close();
  remove(inZip.c_str());
}

static bool fileExists(const char* name)
{
  FILE* tmp = fopen(name, "rb");
  bool exists = (tmp != NULL);
  if (tmp != NULL) fclose (tmp);
  return exists;
}

extern "C" void decrypt(const char* privateKey, const char* in, const char* out)
{
  ifstream fin(in, ios::binary | ios::in);
  if (!fin) {
    cerr << "wrong file " << in << '\n'; return;
  }
  ofstream fout(out, ios::binary | ios::out);
  if (!fout) {
    cerr << "wrong file " << out << '\n'; return;
  }
  string outUnZip = out;
  outUnZip += ".bz2";
  if (!decrypt(fin, fout, atoN(privateKey, 32), keyEncrypt.p)) {
    cerr << "file " << out << " not encrypted!\n";
    fout.close();
    fin.close();
    remove(outUnZip.c_str());
    rename(in, outUnZip.c_str());
    remove(out);
  } else {
    fout.close();
    remove(outUnZip.c_str());
    rename(out, outUnZip.c_str());
  }
  bzip2Uncompress(outUnZip.c_str());
  if (!fileExists(out)) {
    cerr << "unzip error!\n";
    rename(outUnZip.c_str(), out);
  }
}

extern "C" JNIEXPORT void JNICALL Java_zeta_tool_NewVersion_generateSignature(JNIEnv* pJNIEnv, jclass, jint randomize,
                                                                    jstring privateKey, jstring inName, jstring outName)
{
  const char* key = pJNIEnv->GetStringUTFChars(privateKey, 0);
  const char* in = pJNIEnv->GetStringUTFChars(inName, 0);
  const char* out = pJNIEnv->GetStringUTFChars(outName, 0);
  generateSignature(randomize, key, in, out);
  pJNIEnv->ReleaseStringUTFChars(privateKey, key);
  pJNIEnv->ReleaseStringUTFChars(inName, in);
  pJNIEnv->ReleaseStringUTFChars(outName, out);
}

extern "C" JNIEXPORT jboolean JNICALL Java_zeta_ZetaClient_verification(JNIEnv* pJNIEnv, jclass, jstring signature,
                                                                        jbyteArray file)
{
  const char* sig = pJNIEnv->GetStringUTFChars(signature, 0);
  jboolean isCopy = JNI_FALSE;               // no copy is needed
  char* c = (char*)pJNIEnv->GetByteArrayElements(file, &isCopy);
  size_t sz = pJNIEnv->GetArrayLength((jarray)file);
  istrstream in(c, sz);
  istrstream inSig(sig);
  jboolean result = verification(inSig, in, key.p, key.g, key.A);
  pJNIEnv->ReleaseByteArrayElements(file, (jbyte*)c, 0);
  pJNIEnv->ReleaseStringUTFChars(signature, sig);
  return result;
}

extern "C" JNIEXPORT void JNICALL Java_zeta_ZetaClient_encrypt(JNIEnv* pJNIEnv, jclass, jint randomize, jstring inName,
                                                               jstring outName)
{
  const char* in = pJNIEnv->GetStringUTFChars(inName, 0);
  const char* out = pJNIEnv->GetStringUTFChars(outName, 0);
  zeta_encrypt(randomize, in, out);
  pJNIEnv->ReleaseStringUTFChars(inName, in);
  pJNIEnv->ReleaseStringUTFChars(outName, out);
}

extern "C" JNIEXPORT void JNICALL Java_zeta_tool_GetData_decrypt(JNIEnv* pJNIEnv, jclass, jstring privateKey, jstring inName, jstring outName)
{
  const char* key = pJNIEnv->GetStringUTFChars(privateKey, 0);
  const char* in = pJNIEnv->GetStringUTFChars(inName, 0);
  const char* out = pJNIEnv->GetStringUTFChars(outName, 0);
  decrypt(key, in, out);
  pJNIEnv->ReleaseStringUTFChars(inName, in);
  pJNIEnv->ReleaseStringUTFChars(outName, out);
  pJNIEnv->ReleaseStringUTFChars(privateKey, key);
}

extern "C" JNIEXPORT void JNICALL Java_zeta_tool_ZetaCD_compress(JNIEnv* pJNIEnv, jclass, jstring filename)
{
  const char* file = pJNIEnv->GetStringUTFChars(filename, 0);
  bzip2Compress(file);
  pJNIEnv->ReleaseStringUTFChars(filename, file);
}

int main(int argc, char** argv)
{
  if (argc != 3) {
    cerr << "USAGE: " << argv[0] << " <randomize> <filename>\n";
    return 1;
  }
  int randomize = atoi(argv[1]);
  if (randomize == 0) {
    cerr << "wrong parameter\n";
    return 1;
  }
  zeta_encrypt(randomize, argv[2], "e.$$$");
  //generateSignature(randomize, "private_key.txt", argv[2], "signature.txt");
  //if (verification("signature.txt", argv[2])) cout << "OK!" << endl;
  //else cout << "Error!" << endl;
  return 0;
}
