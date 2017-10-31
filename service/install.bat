SET START=%1
if not "%START%" == "" goto startDefined
SET START=c:\zeta\zeta.scr
:startDefined
%START% /C
install_service.bat
