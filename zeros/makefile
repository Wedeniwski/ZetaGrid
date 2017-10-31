#
# Seperation of Zeros of the Riemann Zeta-Function
#
# Author: Sebastian Wedeniwski
# Date:   02/06/2001 (start)
#


# GNU C++ 3.3.1 (Windows, x86):
#
#CC           = g++ -c
#CFLAGS       = -O3 -Wno-deprecated -mcpu=pentium3 -malign-double -Dx86 -D_NOT_DEFINE_LLTOA_ -mno-cygwin -mthreads
#JAVA_INC     = -I"$(JAVA_HOME)\include" -I"$(JAVA_HOME)\include\win32" -D__int64="long long"
#DLLFLAGS     = -shared -Wl,--add-stdcall-alias -Wl,--enable-auto-import
#LNKFLAGS     = -mno-cygwin -lpthread
#OBJ          = o
#EXE          =
#DLL          = dll
#LIB_EXT      = lib
#LIB_PRE      = 
#LINK         = g++ -o $@

# Intel C++ 8.0 (Windows, x86):
#
CC            = icl /c
CFLAGS        = /nologo /G7 /QaxW /W3 /MT /GX /O3 /Oy- /Qip /Op /D_STATISTIC_FILE_ /Dx86
JAVA_INC      = -I"$(JAVA_HOME)\..\include" -I"$(JAVA_HOME)\..\include\win32"
DLLFLAGS      = /LD /MT
LNKFLAGS      = /link "/SECTION:Shared,RWS"
OBJ           = obj
EXE           = .exe
DLL           = dll
LIB_EXT       = lib
LIB_PRE       =
LINK          = icl /nologo

# Microsoft Visual C++ 6.0 (Windows, x86):
#
CC            = cl /c
CFLAGS        = /nologo /W3 /GX /O2 /Oi /Ot /Oy /Ob1 /Gs /Gf /Gy /MT /D_STATISTIC_FILE_ /Dx86
JAVA_INC      = -I"$(JAVA_HOME)\..\include" -I"$(JAVA_HOME)\..\include\win32"
DLLFLAGS      = /LD /MT
LNKFLAGS      = /link "/SECTION:Shared,RWS"
OBJ           = obj
EXE           = .exe
DLL           = dll
LIB_EXT       = lib
LIB_PRE       =
LINK          = cl /nologo

# GNU C++ 3.3 (Linux, x86):
#
#CC           = g++ -c
#CFLAGS       = -O3 -Wno-deprecated -mcpu=pentium3 -malign-double -Dx86 -D_DEFINE_SLEEP_
#JAVA_INC     = -I/usr/java/j2sdk1.4.1_05/include -I/usr/java/j2sdk1.4.1_05/include/linux
#DLLFLAGS     = -shared
#LNKFLAGS     = 
#OBJ          = o
#EXE          =
#DLL          = so
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = g++ -o $@

# GNU C++ 2.95.2 (Linux, x86):
#
#CC           = g++ -c
#CFLAGS       = -O3 -m486 -malign-double -Dx86 -D_DEFINE_SLEEP_
#JAVA_INC     = -I/usr/lib/jdk1.3/include
#DLLFLAGS     = -shared
#LNKFLAGS     =
#OBJ          = o
#EXE          =
#DLL          = so
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = g++ -o $@

# GNU C++ 3.2.1 (Linux, ppc):
#
#CC           = g++ -c
#CFLAGS       = -O3 -Wno-deprecated -fPIC -D_DEFINE_SLEEP_
#JAVA_INC     = -I/opt/blackdown-jdk-1.3.1/include -I/opt/blackdown-jdk-1.3.1/include/linux
#DLLFLAGS     = -shared
#LNKFLAGS     = -fPIC
#OBJ          = o
#EXE          =
#DLL          = so
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = g++ -o $@

# Intel C++ Compiler 7.0 (Linux, x86):
#
#CC           = /opt/intel/compiler70/ia32/bin/icc -c
#CFLAGS       = -D_DEFINE_ATOLL_ -O3 -tpp6 -rcd -D__i486__ -D__GNUC__ -D_DEFINE_SLEEP_
#JAVA_INC     = -I/usr/lib/jdk1.3/include
#DLLFLAGS     = -shared
#LNKFLAGS     =
#OBJ          = o
#EXE          =
#DLL          = so
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = /opt/intel/compiler70/ia32/bin/icc -o $@

# GNU C++ 2.95.2 (Solaris, sparc):
#
#CC           = g++ -c
#CFLAGS       = -O3 -D_REENTRANT -D_SOLTHREADS -fpic -mv8plus
#JAVA_INC     = -I/usr/java1.3/include
#DLLFLAGS     = -G
#LNKFLAGS     = 
#OBJ          = o
#EXE          =
#DLL          = so
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = $(CC) -o $@ -lthread

# GNU C++ 3.3.2 (Solaris, intel):
#
#CC           = g++ -c
#CFLAGS       = -O3 -D_REENTRANT -D_SOLTHREADS -D_DEFINE_SLEEP_ -Dx86 -fpic -Wno-deprecated
#JAVA_INC     = -I/usr/j2se/include
#DLLFLAGS     = -G
#LNKFLAGS     =  
#OBJ          = o
#EXE          =
#DLL          = so
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = g++ -o $@ -lthread

# IBM VisualAge C++ Professional for AIX, Version 5.0 (AIX, ppc):
#
#CC           = /usr/vacpp/bin/xlC_r -c
#CFLAGS       = -D_DEFINE_ATOLL_ -D_DEFINE_SLEEP_ -D_BIG_ENDIAN_ -qthreaded -O -qinline -qinlglue
#JAVA_INC     = -I/usr/java130/include
#DLLFLAGS     = -G
#LNKFLAGS     = 
#OBJ          = o
#EXE          =
#DLL          = so
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = /usr/vacpp/bin/xlC_r -o $@

# GNU C++ 2.9 (AIX, ppc):
#
#CC           = g++ -c
#CFLAGS       = -D_DEFINE_ATOLL_ -D_BIG_ENDIAN_ -fno-exceptions -D_THREAD_SAFE -mthreads -mmultiple -mstring -mhard-float -mpower
#JAVA_INC     = -I/usr/java130/include
#DLLFLAGS     = -shared
#LNKFLAGS     = 
#OBJ          = o
#EXE          =
#DLL          = so
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = g++ -lm_r -o $@

# GNU C++ 2.9 (Linux, s390):
#
#CC           = g++ -c
#CFLAGS       = -D_BIG_ENDIAN_ -O3 -fstrict-aliasing -ffast-math -fcaller-saves -fstrength-reduce -fno-inline -ffloat-store
#JAVA_INC     = -I/usr/java130/include
#DLLFLAGS     = -shared
#LNKFLAGS     =
#OBJ          = o
#EXE          =
#DLL          = so
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = g++ -o $@

# HP ANSI C++ B3910B A.03.37
#
#CC           = aCC -c
#CFLAGS       = -D_BIG_ENDIAN_ -D_DEFINE_SLEEP_ -D_DEFINE_ATOLL_ -mt +Z -ext -AA +O3 +Onolimit +Oprocelim
#JAVA_INC     = -I/opt/java1.3/include -I/opt/java1.3/include/hp-ux
#DLLFLAGS     = -b
#LNKFLAGS     = $(CFLAGS) -lstd_v2 -lCsup_v2
#OBJ          = o
#EXE          =
#DLL          = sl
#LIB_EXT      = a
#LIB_PRE      = lib
#LINK         = aCC -o $@


OBJECTS = zeta_zeros.$(OBJ) lowhigh.$(OBJ) gram.$(OBJ) theta.$(OBJ) statistic.$(OBJ) output.$(OBJ) eval_zeta.$(OBJ) check.$(OBJ) doubledouble.$(OBJ) math.$(OBJ)


all: zeta_zeros$(EXE) $(LIB_PRE)zeta_zeros.$(DLL) trydd$(EXE)
	trydd


zeta_zeros.$(OBJ): zeta_zeros.cpp lowhigh.h zeta_zeros.h eval_zeta.h gram.h theta.h statistic.h check.h output.h constants.h
	$(CC) $(CFLAGS) zeta_zeros.cpp

lowhigh.$(OBJ): lowhigh.cpp lowhigh.h
	$(CC) $(CFLAGS) lowhigh.cpp

gram.$(OBJ): gram.cpp gram.h constants.h
	$(CC) $(CFLAGS) gram.cpp

theta.$(OBJ): theta.cpp theta.h lowhigh.h constants.h
	$(CC) $(CFLAGS) theta.cpp

statistic.$(OBJ): statistic.cpp statistic.h eval_zeta.h zeta_zeros.h gram.h output.h constants.h
	$(CC) $(CFLAGS) statistic.cpp

output.$(OBJ): output.cpp output.h statistic.h eval_zeta.h zeta_zeros.h constants.h
	$(CC) $(CFLAGS) output.cpp

check.$(OBJ): check.cpp check.h theta.h eval_zeta.h
	$(CC) $(CFLAGS) check.cpp

jni.$(OBJ): jni.cpp zeta_zeros.h eval_zeta.h statistic.h check.h gram.h output.h constants.h
	$(CC) $(CFLAGS) $(JAVA_INC) jni.cpp

eval_zeta.$(OBJ): eval_zeta.cpp eval_zeta.h zeta_zeros.h output.h lowhigh.h constants.h
	$(CC) $(CFLAGS) eval_zeta.cpp

doubledouble.$(OBJ): doubledouble.cpp doubledouble.h
	$(CC) $(CFLAGS) doubledouble.cpp

math.$(OBJ): math.cpp doubledouble.h
	$(CC) $(CFLAGS) math.cpp

trydd$(EXE): trydd.cpp math.cpp doubledouble.cpp doubledouble.h doubledouble.$(OBJ) math.$(OBJ)
	$(LINK) $(CFLAGS) trydd.cpp doubledouble.$(OBJ) math.$(OBJ) $(LNKFLAGS)


$(LIB_PRE)zeta_zeros.$(DLL): $(OBJECTS) jni.$(OBJ)
	$(LINK) $(DLLFLAGS) $(OBJECTS) jni.$(OBJ) $(LNKFLAGS)

zeta_zeros$(EXE): $(OBJECTS)
	$(LINK) $(OBJECTS) $(LNKFLAGS)


clean:
	del *.$(OBJ) $(LIB_PRE)zeta_zeros.$(DLL)
