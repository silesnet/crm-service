@echo off
setlocal
set RAW_CMD=%~nx0 %*
set USAGE=Usage: %~nx0 [-h] -m MASTER -a MAC
set MASTER=
set MAC=

:loop
if not "%~1"=="" (
  if "%1"=="-m" (
    set MASTER=%~2
    shift
  )
  if "%1"=="-a" (
    set MAC=%~2
    shift
  )
  if "%1"=="-h" (
    echo %USAGE%
    exit /b 0
  )
  shift
  goto loop
)

if "%MASTER%" == "" (
  echo ERROR: master not specified >&2
  echo %USAGE%
  exit /b 1
)

if "%MAC%" == "" (
  echo ERROR: MAC not specified >&2
  echo %USAGE%
  exit /b 1
)

set STATUS=0

echo
echo              address: lanfiber
echo          mac-address: 00:15:6D:4A:29:DA
echo            client-id: 1:0:15:6d:4a:29:da
echo        address-lists:
echo               server: lanfiber
echo          dhcp-option:
echo               status: bound
echo        expires-after: 2d7h8m33s
echo            last-seen: 16h51m27s
echo       active-address: 10.110.111.249
echo   active-mac-address: 00:15:6D:4A:29:DA
echo     active-client-id: 1:0:15:6d:4a:29:da
echo        active-server: lanfiber
echo            host-name: 10306001
echo     agent-circuit-id: 0:4:0:1:0:6
echo      agent-remote-id: 1:c:6b:61:72:70:65:6e:74:6e:61:2d:62:72
echo

if errorlevel 1 (
  set STATUS=1
)

set ts=%date:/=-% %time::=-%
set ts=%ts:-=:%
set ts=%ts:.=-%
set ts=%ts:,=.%
set ts=%ts:~6,4%-%ts:~3,2%-%ts:~0,2% %ts:~11,11%

if "%status%" == "1" (
  echo [%ts%] ERROR: failed execution of '%RAW_CMD%' >&2
  exit /b 1
)

echo [%ts%] fetched DHCP wireless connection status for '%MASTER%', '%MAC%'
exit /b 0
