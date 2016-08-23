@ECHO OFF

IF "%1" == "" GOTO HELP

REM
REM Determine what command to fake
set command-output="%~dp0tf-%1.log"
set command=%1

REM
REM Find the last argument
SETLOCAL
SHIFT
:LOOP
IF {%1}=={} GOTO FOUND
SET LAST=%1
SHIFT
GOTO :LOOP
:FOUND
ENDLOCAL&SET LAST=%LAST%

IF "%command%" == "mkview" GOTO MKVIEW
IF "%command%" == "setcs" GOTO END
GOTO PRINT-OUTPUT

:MKVIEW
mkdir %LAST%


:PRINT-OUTPUT
IF NOT EXIST %command-output% goto UNKNOWN-COMMAND
type %command-output%
set errorlevel = 0
GOTO END


:UNKNOWN-COMMAND
ECHO Unknown command %1, %command-output%
set errorlevel = 2
GOTO END


:HELP
echo Usage: tf [command] [ignored arguments]
set errorlevel = 1
GOTO END


:END
exit /b %errorlevel%