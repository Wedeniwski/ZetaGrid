// trydd.cc
// Keith Briggs 1996 Dec 04
// Wayne Hayes  1998 Jan 12
// Wayne Hayes  1998 Jan 19 SGI fixes
// 97 Jul 11 added TestQtoa (random tests, should probably do some hard-coded)
// 98 Jan 10 Added Verify function, to make it pseudo-automagic.

#include "doubledouble.h"
#include <stdlib.h>
#include <string.h>

void TestQtoa(int numTests, int digits, int exps);

typedef char Boolean;
#define false (Boolean)0
#define true  (Boolean)1

#define MAX(a,b) ((a)>(b)?(a):(b))

// Assumpution: if it works, don't even bother doing output
int Verify(char msg[], doubledouble correct, doubledouble computed,
    doubledouble delta = 1.1e-31) // default fractional error allowed.
{
    if(correct == computed)
  return true;
    else if(correct.h() == 0.0) // can't do a relative test
    {
  cout << msg << " should be " << correct << ", got " << computed << '\n';
  return false;
    }
    else if(fabs((correct-computed)/correct) <= delta)
  return true;
    else
    {
  cout << msg << " should be " << correct << ", got " << computed << '\n';
  return false;
    }
}
    
int main() {
  cout <<setprecision(31);
  doubledouble one=1.0,two=2.0,ten=10.0,x,y,z,e,small=1.1e-30; int i;
  base_and_prec();
  // Shoup floor test: floor((2^53-2)/2^53+3/2^55)=0:
  double x1=0.9999999999999997779553950749686919152737;
  double x2=0.8326672684688674053177237510681152343750e-16;
  x1=(pow(2.0,53.0)-2)/pow(2.0,53.0);
  x2=(3.0)/pow(2.0,55.0);
  Verify("Test -2: should be 0: ", 0, floor(doubledouble(x1)+doubledouble(x2)));
  Verify("Test -1: should be 0: ", 0,
      floor(doubledouble("0.9999999999999997779553950749686919152737")
  +doubledouble("0.00000000000000008326672684688674053177237510681152343750")));
  Verify("Test 0: 1+1.0e-40 = ", 1.0, doubledouble(1.0)+doubledouble(1.0e-40));
  cout << "\nI/O conversion tests: verify values by eyeball" << endl;
  cout << "This should be 0.333333333: " << doubledouble("0.333333333") <<endl;
  cout << "This should be 0.333333333e-4: " << doubledouble("00000.0000333333333") <<endl;
  cout << "This should be 3.33333333e8: " << doubledouble("333333333") <<endl;
  cout << "This should be 3.33333333e20: " << doubledouble("333333333e12") <<endl;
  cout << "This should be -3.333333333333e8: " << doubledouble("-333333333.3333") <<endl;
  cout << "This should be -3.3333333333e4: " << doubledouble("-333333333.33e-04") <<endl <<endl;
  cout << "The remainder of the tests will be silent if successful.\n\n";
  z = 0.0;
  Verify("Test 2: fmod(\",10)\n", 0.0, fmod(z,10));
  cout <<setprecision(32);

  z="123456789"; 
  Verify("Test 3a: floor(123456789) gives...\n", "123456789", floor(z));
  z="0.1234567890123456789012345678901234567890"; 
  Verify("Test 3b: floor(0.1234567890123456789012345678901234567890)",
      0.0, floor(z));
  z="1234567890123456789012345678901234567890";
  Verify("Test 3c: floor(1234567890123456789012345678901234567890)",
      "1234567890123456789012345678901234567890", floor(z));
  z= "12345678901234567890.12345678901234567890"; 
  Verify("Test 3d: floor(12345678901234567890.12345678901234567890)",
      "12345678901234567890", floor(z));
  z="12345678901234567890.12345678901234567890"; z=-z;
  Verify("Test 3e: floor(-12345678901234567890.12345678901234567890)",
      "-12345678901234567891", floor(z));

  Verify("Test 4: exp(1)", "2.71828182845904523536028747135266",
      z=exp(one));

  e=exp(one); 
  z=log(recip(e*e)); Verify("log(1/e/e): ", -2, z);
  z=log(recip(e)); Verify("log(1/e): ", -1, z);
  z=log(doubledouble(1)); Verify("log(1)", 0, z);
  z=log(e); Verify("log(e): ", 1, z);
  y=log(sqr(e)); Verify("log(e^2): ", 2, y);
  y=log(e*e*e); Verify("log(e^3): ", 3, y);
  z="0.159";
  Verify("log(0.159): ", "-1.8388510767619055265142599881015", log(z));
  z="0.2107210222156525610500017104882905489049";
  Verify("Test 6: exp: exp(0.2107210222156525610500017104882905489049)",
      "1.23456789", z=exp(z));
  Verify("Test 7a: reciprocal of previous result",
      "0.8100000073710000670761006103925", z=one/z);
  Verify("Test 7b: reciprocal of previous result", "1.23456789", z=one/z);
  z="1.4142135623730950488016887242096980785696718753769";
  Verify("Test 8: 1.41421356237309504880168872420969807856967^2", 2, (z*z));
  Verify("sqrt(2)", " 1.4142135623730950488016887242096980785696718753769",
    sqrt(doubledouble(2)));
  // "Test 9: Start with 2, sqrt five times and sqr five times\n";
  Verify("rooting 2", "1.4142135623730950488016887242097", x=sqrt(two));
  Verify("rooting 2", "1.1892071150027210667174999705604", x=sqrt(x));
  Verify("rooting 2", "1.0905077326652576592070106557607", x=sqrt(x));
  Verify("rooting 2", "1.0442737824274138403219664787399", x=sqrt(x));
  Verify("rooting 2", "1.0218971486541166782344801347832", x=sqrt(x));
  Verify("squaring rooted 2s", "1.0442737824274138403219664787399", x=x*x);
  Verify("squaring rooted 2s", "1.0905077326652576592070106557606", x=x*x);
  Verify("squaring rooted 2s", "1.1892071150027210667174999705604", x=x*x);
  Verify("squaring rooted 2s", "1.4142135623730950488016887242096", x=x*x);
  Verify("squaring rooted 2s", 2, x=x*x);
  if (fabs(x.h()-2)>1.0e-31) { cerr<<"Something wrong, not getting 31dp!!\n"; exit(1); } 
  x="123456789.1234567890123456789";
  y="123456789.1234567891";
  Verify("compare x<y", 1, (x<y), 0); // zero fractional error allowed.
  Verify("compare y>=x", 1, (y>=x), 0);
  Verify("compare y!=x", 1, (y!=x), 0);
  Verify("compare y==x", 0, (y==x), 0);
  Verify("compare x>y", 0, (x>y), 0);
  const doubledouble exact_sin[]={
    "0.000099999999833333333416666666646825396828152557318973",
    "0.0009999998333333416666664682539710097001513147348086",
    "0.0099998333341666646825424382690997290389643853601692",
    "0.099833416646828152306814198410622026989915388017982",
    "0.84147098480789650665250232163029899962256306079837",
    "-0.54402111088936981340474766185137728168364301291622",
    "-0.50636564110975879365655761045978543206503272129066",
    "0.82687954053200256025588742910921814121272496784779",
    "-0.30561438888825214136091003523250697423185004386181",
  };
  x="0.0001";
  /*
  ** Testing trig functions is tricky.  If x is of order 2*Pi or less, then we
  ** can expect to get full precision (about 30 digits).  But if x >> 1, then
  ** *any* sin algorithm will lose precision due to argument reduction,
  ** so we allow more leeway.  Thus the use of MAX below.
  */
  for (i=0; i<9; i++) {
    char sx[1000] = "sin(";
    strcat(sx, qtoa(NULL, 31, 'g', x));
    strcat(sx, ")");
    y=sin(x);
    Verify(sx, exact_sin[i], y, small*MAX(ten,x));
    x = 10.0*x;
  }
  const doubledouble exact_atan[]={ //exact_atan[i]=atan(10^(i-4))
    "0.000099999999666666668666666652380952492063491154401162",
    "0.00099999966666686666652380963492054401162093455426801",
    "0.0099996666866652382063401162092795485613693525443766",
    "0.099668652491162027378446119878020590243278322504315",
    "0.78539816339744830961566084581987572104929234984378",
    "1.4711276743037345918528755717617308518553063771832",
    "1.5607966601082313810249815754304718935372153471432",
    "1.5697963271282297525647978820048308980869637651333",
    "1.5706963267952299525626550249873704896065212085332"
  };
  x="0.0001";
  for (i=0; i<8; i++) {
    doubledouble S=atan(x);
    char sx[1000] = "atan(";
    strcat(sx, qtoa(NULL, 31, 'g', x));
    strcat(sx, ")");
    Verify(sx, exact_atan[i], S, 2e-30); // heuristic delta works for SPARC.
    x = 10.0*x;
  }
  x="0.123456789"; y="0.3"; 
  Verify("Test 12.5: 0.123456789+0.3", "0.423456789", (x+=y));
  x="0.1"; 
  cout<< setprecision(35);
  Verify("Test 13: tan(arctan(0.1))=", "0.1", sin(atan(x))/cos(atan(x)));
  Verify("Test 14a: floor(1234567.89)",
      "1234567", floor(doubledouble(1234567.89)));
  Verify("floor(doubledouble(1234567.89, -123.0))",
      "1234444", floor(doubledouble(1234567.89, -123.0)));
  cout<<setprecision(30);
  Verify("hypot(1,1)", "1.4142135623730950488016887242096981",
      hypot(doubledouble(1),doubledouble(1)));
  Verify("hypot(0,1)", 1, hypot(doubledouble(0),doubledouble(1)));
  Verify("hypot(1,0)", 1, hypot(doubledouble(1),doubledouble(0)));
  Verify("hypot(3,4)", 5, hypot(doubledouble(3),doubledouble(4)));
  Verify("hypot(1e200,1e200)", "1.4142135623730950488016887242096981e200",
    hypot(doubledouble("1e200"),doubledouble("1e200")));
  //cout << "Test 15: input conversion\n";
  //cout<<"Please enter a number, terminate with ctrl-d: ";  cin>>x;
  //cout<<"\nYou entered "<<x<<endl;

  // Let's leave out the TESTQTOA that give incorrect results.
  //TestQtoa(1000, 30, 296); // this one gives a few minor errors
  TestQtoa(1000, 30, 295);
  //cout << "Test 16: \n";
  x=powint(two,52)+one;
  y=(powint(two,20)-one)/powint(two,21);
  Verify("lower order bits", -doubledouble(1048575)/doubledouble(2097152),
                        powint(two,52)+one-(x+y));
  doubledouble rem; int n;
  modr(doubledouble::Pi,doubledouble(2.0),n,rem);
  Verify("modr", 0, doubledouble::Pi-(2*n+rem));
  modr(doubledouble(1213.12312312),doubledouble(3.0),n,rem);
  Verify("modr", 0, doubledouble(1213.12312312)-(3*n+rem));
  return 0;
}


///////////////////////////////// testqtoa.cc ////////////////////////////////
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
//#include <unistd.h> /* for getpid, used to seed random number generator */
#include <string.h>
#include "doubledouble.h"

/* return number of chars s1 and s2 agree to */
int strcmplen(const char *s1, const char *s2)
{
    int k = 0;
    while(*s1 && *s2 && *s1++ == *s2++)
  ++k;
    return k;
}

/* return number of chars s1 and s2 agree to, no more than n */
int strncmplen(const char *s1, const char *s2, int n)
{
    int k = 0;
    while(n-- > 0 && *s1 && *s2 && *s1++ == *s2++)
  ++k;
    return k;
}

#define GCHAR 7 /* max of 7 extra chars in %g: -.e+NNN */

/*
** if TEST_DIGITS <= 30 and TEST_EXP <= 295, it should always be precise.
** ie., you get at least 30 digits of precision between 1e-295 and 1e+295.
** The error rate goes up slowly at you increase TEST_EXP up to 300.
** The error rate is high if you ask for >30 digits of precision, regardless
** of TEST_EXP.
*/
void TestQtoa(int numTests,
    int TEST_DIGITS,  /* test for this many digits of precision, <= 31 */
    int TEST_EXP) /* maximum exponent to test for, <= 300 */
{
    doubledouble a = 1;
    int kCorrect = 0, digitsCorrect=0, testNum;
    int sz=TEST_DIGITS+GCHAR+1;

    assert(TEST_DIGITS>0 && TEST_DIGITS<=31 && TEST_EXP>0 && TEST_EXP<=300);

#if INTERACTIVE
    while(fgets(line, sizeof(line), stdin)) {
  char num[256];
  int prec;
  doubledouble q;
  sscanf(line, "%s %d", num, &prec);
  q = atoq(num);
  printf("%s %.17g %.17g\n", qtoa(num, prec, 'g', q), q.h(), q.l());
    }
#elif 1 /*RANDOM*/
    //srand48(time(0)+getpid());

    //printf("pi=%s\n", qtoa(NULL, 31, 'g', doubledouble::Pi));
    //printf("qrand=%s\n", qtoa(line, 31, 'g', doubledoubleRand48()));
    //printf("qrand=%s\n", qtoa(line, 31, 'g', doubledoubleRand48()));

    //printf("TestQtoa: %d trials, %d digits, %d exponent\n",
  //numTests, TEST_DIGITS, TEST_EXP);

    for(testNum=0; testNum<numTests; testNum++) {
      char* num = new char[sz];
      char* line = new char[sz];
      char* s;
      int i = 0;
      if ((double(rand())/RAND_MAX) < .5) num[i++] = '-';
      num[i] = 0;
      num[i++] = '1' + (int)((double(rand())/RAND_MAX) * 9);
      num[i++] = '.';
      for(; i<TEST_DIGITS+1 + (num[0] == '-'); i++) num[i] = '0' + (int)((double(rand())/RAND_MAX) * 10);
      num[i++] = 'e';
      sprintf(num+i, "%+0.2d", (int)((double(rand())/RAND_MAX)*(2*TEST_EXP)-TEST_EXP));
      a = atodd(num);
      s = qtoa(line, TEST_DIGITS-1, 'e', a);
      if(strncmp(num, s, strlen(num)) != 0)
      {
        int nCorrect = strcmplen(num, s)-1-(a<doubledouble(0));
        printf("TESTQTOA error: %d correct, then\n", kCorrect);
        kCorrect = 0;
        printf("high=  %.17g, low=%.17g\nstring=%s\nconvrt=%s agree=%d\n", a.h(), a.l(), num, s, nCorrect);
        digitsCorrect += nCorrect;
        return;
      } else {
        ++kCorrect;
        digitsCorrect += TEST_DIGITS;
      }
      delete[] line;
      delete[] num;
    }
    //printf("Avg correct digits = %g\n", digitsCorrect/(double)numTests);
#endif
}

