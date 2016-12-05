@echo off
setlocal
set RAW_CMD=%~nx0 %*
set USAGE=Usage: %~nx0 [-h] -a ADDRESS -s SUBJECT -m MESSAGE
set ADDRESS=
set SUBJECT=
set MESSAGE=

:loop
if not "%~1"=="" (
  if "%1"=="-a" (
    set ADDRESS=%~2
    shift
  )
  if "%1"=="-s" (
    set SUBJECT=%~2
    shift
  )
  if "%1"=="-m" (
    set MESSAGE=%~2
    shift
  )
  if "%1"=="-h" (
    echo %USAGE%
    exit /b 0
  )
  shift
  goto loop
)

if "%ADDRESS%" == "" (
  echo ERROR: address not specified >&2
  echo %USAGE%
  exit /b 1
)

if "%SUBJECT%" == "" (
  echo ERROR: subject not specified >&2
  echo %USAGE%
  exit /b 1
)

if "%MESSAGE%" == "" (
  echo ERROR: message not specified >&2
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

echo [%ts%] email sent to '%ADDRESS%' with subject '%SUBJECT%' and message '%MESSAGE%'
exit /b 0
