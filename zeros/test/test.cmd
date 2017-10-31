@echo off
if "%1" == "" goto noArg
set value=%1
goto Cont
:noArg
set value=1000000000000
:Cont
set range=1000
call zeta_test.cmd %value% %range% 0
call zeta_test.cmd %value% %range% 1
call zeta_test.cmd %value% %range% 2
call zeta_test.cmd %value% %range% 3
rem call zeta_test.cmd %value% %range% 4
call zeta_test.cmd %value% %range% 5
rem call zeta_test.cmd %value% %range% 6
call zeta_test.cmd %value% %range% 7
call zeta_test.cmd %value% %range% 8
call zeta_test.cmd %value% %range% 9
call zeta_test.cmd %value% %range% 10
rem call zeta_test.cmd %value% %range% 11
call zeta_test.cmd %value% %range% 12
rem call zeta_test.cmd %value% %range% 13
call zeta_test.cmd %value% %range% 14
call zeta_test.cmd %value% %range% 15
call zeta_test.cmd %value% %range% 16
call zeta_test.cmd %value% %range% 17
rem call zeta_test.cmd %value% %range% 18
call zeta_test.cmd %value% %range% 19
rem call zeta_test.cmd %value% %range% 20
call zeta_test.cmd %value% %range% 21
call zeta_test.cmd %value% %range% 22
call zeta_test.cmd %value% %range% 23
call zeta_test.cmd %value% %range% 24
call zeta_test.cmd %value% %range% 25
call zeta_test.cmd %value% %range% 26
