/*
  cd0011-cd0170
  cd0171-cd0193 (nur Suche nach 2 x gram type 00)
  cd0194-cd0338
  cd0339-cd0352 (nur Suche nach 2 x gram type 00)
  cd0353-cd0387
  cd0388-cd0397
  cd0028        defect
  cd0196        defect
  cd0200        defect
  cd0205        defect
  cd0206        defect
  cd0220        defect
  cd0235        defect
  cd0327        defect
  cd0329        defect
  cd0331        defect
  cd0334        defect

  Lune et al. n= 1048449114, t=  388858886.002: 0.000107 < DELTA < 0.000109
              n=11624718875, t= 3800137556.831: 0.000174 < DELTA < 0.000178
              n=28082164240: DELTA < 0.0001983
              n=38333977237: DELTA < 0.0001850
              n=41341245202: t=12715490382.791: 0.0001487731934 < DELTA < 0.0001507731934
              n=41961907338: DELTA < 0.0002193
              n=42720554355: DELTA < 0.0002193
              n=44203398884: DELTA < 0.0001907
              n=53211979336: DELTA < 0.0002002
                           : DELTA < 0.000366  (max: Z(t)=6.0321003103476e-007)
              n=58625554456, t=17742359407.667: 0.0001135409179 < DELTA < 0.0001137409179
           !+ n=60917681408, t=18403609310.644: 0.0001005968262 < DELTA < 0.0001007968262
           !- n=73132021418, t=21909920354.092: 0.000114 < DELTA < 0.000118

           !+ n= 4020755337: DELTA > 1.33805
           !- n= 4219726752: DELTA > 1.34254
              n= 6811982059: DELTA > 1.28399
              n=16265396006: DELTA > 1.27759
              n=16513952046: DELTA > 1.23400
              n=17003807754: DELTA > 1.31311
              n=24081166839: DELTA > 1.22020
              n=25304589020: DELTA > 1.22531
              n=48348026883: DELTA > 1.21000
              n=50740831995: DELTA > 1.21400
              n=71117177808: DELTA > 1.17073
              n=72264805718: DELTA > 1.17563
              n=73780104948: DELTA > 1.14584




large gap 6 (zeta_zeros_76141216800_500000.zip): 22769.
large gap 6 (zeta_zeros_76141216800_500000.zip): 22769.
large gap 6 (zeta_zeros_77527945700_500000.zip): 23165.
large gap 6 (zeta_zeros_77527945700_500000.zip): 23165.


12242196025
NO ZERO FOUND!: real small value (zeta_zeros_12242033300_500000.zip): 3991774034,28, MZ=-0.948778742791e-6,-0.948778742790e-6
13255376967
NO ZERO FOUND!: real small value (zeta_zeros_13254928400_500000.zip): 4305248245,672, MZ=-0.365284164891e-6,-0.365284164891e-6
14456480324
NO ZERO FOUND!: real small value (zeta_zeros_14455981500_500000.zip): 4675422525,765, MZ=0.935572422303e-7,0.935572422310e-7
36575813727
NO ZERO FOUND!
48968981382
NO ZERO FOUND!
50293968273
NO ZERO FOUND!
50247784110
NO ZERO FOUND!
50280653078
NO ZERO FOUND!
55659059564
NO ZERO FOUND!
58636027748
NO ZERO FOUND!
61542978040
NO ZERO FOUND!
64209162684
NO ZERO FOUND!
70948668413: real small value (zeta_zeros_70948524400_300000.zip): 21285161585,622, MZ=0.9589716967324e-7,0.9589717011780e-7
NO ZERO FOUND!
72412180973: real small value (zeta_zeros_72412141100_300000.zip): 21704031345,437, MZ=0.6896762561390e-6,0.6896762565836e-6
NO ZERO FOUND!
78038389931: real small value (zeta_zeros_78038240700_300000.zip): 23310944118,591, MZ=-0.9834449935260e-6,-0.9834449930813e-6
NO ZERO FOUND!
*/

void smallGaps()
{
  remove("zeta_zeros_200000000000_1.txt");
  remove("zeta_zeros_200000000000_1.log");
  startType n = 200000000000;
  setCoutLog(false);
  cout.precision(25);
  ZetaZeros zetaZeros2(n, 1, 0);

  const char* s[] = {
    "real small value (zeta_zeros_75221401200_500000.zip): 22507033234.765, MZ=0.1673666093644e-6,0.1673666095870e-6",
    "real small value (zeta_zeros_76192306600_500000.zip): 22784324700.22, MZ=0.4024810926793e-6,0.4024810931239e-6",
    "real small value (zeta_zeros_77093933300_1000000.zip): 23041584355.111, MZ=0.3777389619438e-6,0.3777389619999e-6",
    ""
  };
  const double d = 0.00002;
  for (int i = 0; s[i][0]; ++i) {
    const char* t = strchr(s[i], ':');
    if (!t) { cerr << "ERROR: i=" << i << endl; continue; }
    double t0 = atof(t+1);
    double t1 = t0-floor(t0);
    t0 = floor(t0);
    cout << getStartN(t0+t1) << endl;
    double g1 = t0 + t1 - 0.001;
    const double g2 = t0 + t1 + 0.002;
    double g0,z0,z1 = 0.0;
    double z2,z3;
    double delta = 0.0;
    while (g1 < g2) {
      zetaZeros2.evalZeta.evalDZ(g1, z2, z3);
      //cout << (g1-t0) << ':' << z2 << ',' << z3 << endl;
      if (z1*z2 < 0) {
        if (delta == 0.0) {
          delta = g1-d;
        } else {
          delta = g1-delta;
          break;
        }
      }
      if (delta == 0.0) { g0 = g1; z0 = z2; }
      else if (g1-delta > 0.0002) break;
      g1 += d; z1 = z2;
    }
    if (g1 >= g2) {
      cout << "NO ZERO FOUND!" << endl;
    } else {
      if (delta < 0.0002) {
        cout << "delta=" << delta << endl;
        cout.precision(20);
        cout << "GOOD VALUE:\n" << g0 << ':' << z0 << '\n' << g1 << ':' << z2 << endl;
      }
    }
  }
}

void smallGap(double g1, double g2, double d = 0.00001)
{
  const startType n = getStartN(g2) + 1000;
  setCoutLog(false);
  cout.precision(25);
  ZetaZeros* zetaZeros2 = new ZetaZeros(n, 5, 0);

  const double t0 = floor(g1);
  double g0,z0,z1 = 0.0;
  double z2,z3;
  double delta = 0.0;
  while (g1 < g2) {
    zetaZeros2->evalZeta.evalDZ(g1, z2, z3);
    cout << (g1-t0) << ':' << z2 << ',' << z3 << endl;
    if (z1*z2 < 0) {
      if (delta == 0.0) {
        delta = g1-d;
      } else {
        delta = g1-delta;
        break;
      }
    }
    if (delta == 0.0) { g0 = g1; z0 = z2; }
    g1 += d; z1 = z2;
  }
  cout << "delta=" << delta << endl;
  delete zetaZeros2;
  char c[50],c2[20];
  itoa(n, c2, 10);
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.txt"));
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.log"));
}

void smallGap(doubledouble g1, doubledouble g2, double d = 0.00001)
{
  const startType n = getStartN(double(g2)) + 1000;
  setCoutLog(false);
  cout.precision(25);
  ZetaZeros* zetaZeros2 = new ZetaZeros(n, 5, 0);

  const doubledouble t0 = floor(g1);
  doubledouble g0;
  double z0,z1 = 0.0;
  double z2,z3;
  doubledouble delta = 0.0;
  while (g1 < g2) {
    zetaZeros2->evalZeta.evalDZ(g1, z2, z3);
    cout << (g1-t0) << ':' << z2 << ',' << z3 << endl;
    if (z1*z2 < 0) {
      if (double(delta) == 0.0) {
        delta = g1-d;
      } else {
        delta = g1-delta;
        break;
      }
    }
    if (double(delta) == 0.0) { g0 = g1; z0 = z2; }
    g1 += d; z1 = z2;
  }
  cout << "delta=" << delta << endl;
  delete zetaZeros2;
  char c[50],c2[20];
  itoa(n, c2, 10);
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.txt"));
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.log"));
}

void largeGap(double g1, double g2, double d)
{
  const startType n = getStartN(g2) + 1000;
  setCoutLog(false);
  cout.precision(25);
  ZetaZeros* zetaZeros2 = new ZetaZeros(n, 5, 0);

  cout << getStartN(g1) << endl;

  const double t0 = floor(g1);
  double g0,z0,z1 = 0.0;
  double z2,z3;
  double delta = 0.0;
  while (g1 < g2) {
    zetaZeros2->evalZeta.evalDZ(g1, z2, z3);
    cout << (g1-t0) << ':' << z2 << ',' << z3 << endl;
    if (z1*z2 < 0) {
      if (delta == 0.0) {
        delta = g1;
        g1 = g2; g2 += 10*d;
      } else {
        delta = g1-d-delta;
        break;
      }
    }
    if (delta == 0.0) { g0 = g1; z0 = z2; }
    g1 += d; z1 = z2;
  }
  cout << "delta=" << delta << endl;
  delete zetaZeros2;
  char c[50],c2[20];
  itoa(n, c2, 10);
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.txt"));
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.log"));
}


void plot(startType n, int count, double delta = 0.0005)
{
  if (count <= 0) return;
  const startType n2 = n+1000000;
  setCoutLog(false);
  cout.precision(25);
  ZetaZeros* zetaZeros2 = new ZetaZeros(n2, 5, 0);
  delta *= count;
  double g0 = 0.0;
  double t0;
  if (double(n) == -1.0) t0 = 9.6669;
  else if (double(n) == 0.0) t0 = 17.8456;
  else if (double(n) == 1.0) t0 = 23.1703;
  else t0 = Gram(n, TWO_PI*n/log(double(n)));
  do {
    n += 1.0;
    double t1 = Gram(n, t0);
    for (double t2 = t0; t2 < t1; t2 += delta) {
      double z0,z1;
      zetaZeros2->evalZeta.evalDZ(t2, z0, z1);
      cout << g0+(t2-t0)/(t1-t0) << ' ' << (z0+z1)/2 << endl;
    }
    t0 = t1; g0 += 1.0;
  } while (--count > 0);
  delete zetaZeros2;
  char c[50],c2[20];
  itoa(n, c2, 10);
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.txt"));
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.log"));
}

void plot(double t1, double t2, double delta = 0.001)
{
  const startType n = getStartN(t2) + 1000;
  setCoutLog(false);
  cout.precision(25);
  ZetaZeros* zetaZeros2 = new ZetaZeros(n, 5, 0);

  double z0,z1;
  for (; t1 < t2; t1 += delta) {
    zetaZeros2->evalZeta.evalDZ(t1, z0, z1);
    cout << t1 << ' ' << (z0+z1)/2 << endl;
  }
  zetaZeros2->evalZeta.evalDZ(t2, z0, z1);
  cout << t2 << ' ' << (z0+z1)/2 << endl;
  delete zetaZeros2;
  char c[50],c2[20];
  itoa(n, c2, 10);
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.txt"));
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.log"));
}

void plot(doubledouble t1, const doubledouble& t2, double delta = 0.001)
{
  const startType n = getStartN(double(t2)) + 1000;
  setCoutLog(false);
  cout.precision(25);
  ZetaZeros* zetaZeros2 = new ZetaZeros(n, 5, 0);

  cout.precision(25);
  double z0,z1;
  for (; t1 < t2; t1 += delta) {
    zetaZeros2->evalZeta.evalDZ(t1, z0, z1);
    cout << t1 << ' ' << (z0+z1)/2 << endl;
  }
  zetaZeros2->evalZeta.evalDZ(t2, z0, z1);
  cout << t2 << ' ' << (z0+z1)/2 << endl;
  delete zetaZeros2;
  char c[50],c2[20];
  itoa(n, c2, 10);
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.txt"));
  remove(strcat(strcat(strcpy(c, "zeta_zeros_"), c2), "_5.log"));
}
