###### Change the following lines as indicated ###########

#Point this to your JDK 1.3 root directory
JDK=D:\Compiler\j2sdk1.4.2_05

#The name of this executable being built
EXE=ZetaGrid.exe

###########################################################


#Resources
RES=messages.res

all: $(RES) $(EXE)

CFLAGS=-MT -I$(JDK)\include -I$(JDK)\include\win32

.c.obj:
    cl -c $(CFLAGS) $*.c


javaserver.obj: javaserver.c service.h

service.obj: service.c service.h messages.h

parseargs.obj: parseargs.c parseargs.h

registry.obj: registry.c registry.h

javaserv_objs=javaserver.obj service.obj parseargs.obj registry.obj

javaserv_libs=advapi32.lib shell32.lib $(JDK)\lib\jvm.lib

$(RES): messages.mc
    mc messages.mc
    rc messages.rc

$(EXE): $(javaserv_objs)
    link -OUT:$(EXE) $(javaserv_objs) $(javaserv_libs) $(RES)
    del *.obj
    del *.bin
    del *.rc
    del *.res
