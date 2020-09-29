@echo off
setlocal
set RAW_CMD=%~nx0 %*
set USAGE=Usage: %~nx0 [-h] -m MASTER -a IP
set MASTER=
set IP=

:loop
if not "%~1"=="" (
  if "%1"=="-m" (
    set MASTER=%~2
    shift
  )
  if "%1"=="-a" (
    set IP=%~2
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

if "%IP%" == "" (
  echo ERROR: IP not specified >&2
  echo %USAGE%
  exit /b 1
)

set STATUS=0


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

echo [%ts%] disabled DHCP wireless address '%IP%' of '%MASTER%'
exit /b 0
