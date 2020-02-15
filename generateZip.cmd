@ECHO ON

for /f "tokens=2,3,4 delims=/- " %%x in ("%date%") do set d=%%y%%x%%z
SET data=%d%
for /f "tokens=1,2,3 delims=:. " %%x in ("%time%") do set t=%%x%%y%%z
SET time=%t%

ECHO zipping...

"C:\Program Files\7-Zip\7z.exe" a -tzip "%CD%\PVCS-CICO-%d%-%t%.zip" -xr!.git -x!*.md -x!*.cmd -x!*.ps1 -x!*.zip -x!*.bat

ECHO Done!