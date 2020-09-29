@echo off
setlocal
set RAW_CMD=%~nx0 %*
set USAGE=Usage: %~nx0 [-h] -s SWITCH -p PORT -v VALUE
set SWITCH=
set PORT=
set VALUE=

:loop
if not "%~1"=="" (
  if "%1"=="-s" (
    set SWITCH=%~2
    shift
  )
  if "%1"=="-p" (
    set PORT=%~2
    shift
  )
  if "%1"=="-v" (
    set VALUE=%~2
    shift
  )
  if "%1"=="-h" (
    echo %USAGE%
    exit /b 0
  )
  shift
  goto loop
)

if "%SWITCH%" == "" (
  echo ERROR: switch not specified >&2
  echo %USAGE%
  exit /b 1
)

if "%PORT%" == "" (
  echo ERROR: port not specified >&2
  echo %USAGE%
  exit /b 1
)

if "%VALUE%" == "" (
  echo ERROR: value not specified >&2
  echo %USAGE%
  exit /b 1
)

set STATUS=0

REM RUN COMMAND HERE

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

echo [%ts%] configured '%SWITCH%' port '%PORT%' to '%VALUE%'
exit /b 0
