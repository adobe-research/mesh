@echo off
@rem Need to delay the expansion of variables until runtime so
@rem so that we can build up the CP variable:
@rem http://ss64.com/nt/delayedexpansion.html
setlocal enabledelayedexpansion
set ROOTDIR=%~dp0..
set "PATHSEP=;"
set JAVA_PROPS=%JAVA_PROPS%


if not exist %ROOTDIR%\mesh.jar (
    echo Unable to find mesh.jar
    exit /b 1
)

set LIBJARS=guava-11.0.1,rats-runtime,javassist,jline,core
set "CP=-cp %ROOTDIR%\mesh.jar"

for %%i in (%LIBJARS%) do ( 
    set "CP=!CP!%PATHSEP%%ROOTDIR%\lib\%%i.jar"
)

set "JVM_ARGS=-Xmx1024m -ea -server %CP% %JAVA_PROPS%"

if "%MODULEPATH%" == "" (
    set "MODULEPATH=%ROOTDIR%\src\script\lib"
    set "MODULEPATH=!MODULEPATH!;%ROOTDIR%\src\script"
    set "MODULEPATH=!MODULEPATH!;."
)

@rem TODO bake lang + std access into compiler as default
java %JVM_ARGS% shell.Main -path %MODULEPATH% %*