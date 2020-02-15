@ECHO OFF
REM 這是一個編譯用指令的批次檔範例，使用方式 make.bat [CLEAN/CONFIG/BUILD]
REM 清空編譯目標值
SET CLEANED=
SET CONFIG=
SET BUILT=

REM 設定MSBUILD執行檔路徑
SET MSBUILD=C:\msbuild.exe

REM 設定要產出的目錄，用相對路徑方式
SET ARTIFACTS="WebApps\bin"


IF "%~1"=="" (
    SET CLEANSTEP=YES
    SET CONFIGSTEP=YES
    SET BUILDSTEP=YES
    GOTO :DEFAULTSTEP
) ELSE (
    :PARSER
    IF /I "%1"=="CLEAN" (
        SET CLEANSTEP=YES
    )
    IF /I "%1"=="CONFIG" (
        SET CONFIGSTEP=YES
    )
    IF /I "%1"=="BUILD" (
        SET BUILDSTEP=YES
    )
    SHIFT
    IF "%1"=="" GOTO :DEFAULTSTEP
    GOTO :PARSER
)


:DEFAULTSTEP
IF "%CLEANSTEP%"=="YES" GOTO :CLEAN
IF "%CONFIGSTEP%"=="YES" GOTO :CONFIG
IF "%BUILDSTEP%"=="YES" GOTO :BUILD
GOTO :EOF

:CLEAN
ECHO ==========CLEAN UP %ARTIFACTS%==========
REM 進入編譯目標區後，下面範例示範先刪除舊檔案，再建立空目錄的DOS指令
REM 你也可以使用別的指令或寫在MSBUILD -target:Clean清除檔案
IF "%CLEANSTEP%"=="YES" (
    REM 清除資料指令
    ECHO CD %ARTIFACTS%
    ECHO RMDIR /S /Q *
    ECHO MKDIR test/bin

    SET CLEANED=DONE
    
)
SET CLEANSTEP=DONE
@ECHO:
GOTO :DEFAULTSTEP


:CONFIG
IF "%CONFIGSTEP%"=="YES" (
    ECHO ==========CONFIG Project==========
    REM 執行你需要設定環境參數等的批次檔，例如CALL enviroment.bat
    REM 或是將取得的專案原始碼進行搬移或組合

    REM 用SET返回目前設定變數作為檢查
    SET

    SET CONFIG=DONE
)
SET CONFIGSTEP=DONE
@ECHO:
GOTO :DEFAULTSTEP

:BUILD
IF "%BUILDSTEP%"=="YES" (
    ECHO ==========BUILD==========
    ECHO RUN: %MSBUILD% test.csproj
    SET BUILT=DONE
    
)
SET BUILDSTEP=DONE
@ECHO:
