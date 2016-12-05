@echo off
setlocal
set RAW_CMD=%~nx0 %*
set USAGE=Usage: %~nx0 [-h] -u USER -d DEVICE
set USER=
set DEVICE=

:loop
if not "%~1"=="" (
  if "%1"=="-u" (
    set USER=%~2
    shift
  )
  if "%1"=="-d" (
    set DEVICE=%~2
    shift
  )
  if "%1"=="-h" (
    echo %USAGE%
    exit /b 0
  )
  shift
  goto loop
)

if "%USER%" == "" (
  echo ERROR: user not specified >&2
  echo %USAGE%
  exit /b 1
)

if "%DEVICE%" == "" (
  echo ERROR: device not specified >&2
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

echo [%ts%] kicked PPPoE user '%USER%' from '%DEVICE%'
exit /b 0
