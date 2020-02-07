# PVCS-CICO
This plugin is design for MicorFocus/ Serena Deploy Automation.
contorl pcli to check in and check out codes/ artifacts.

## How to use
-1. In DA > Administration > Automation , load this project.zip to plugins
-2. While design component, The appears in Repositories > Source
-3. Do checkout and lock to artifacts(bin) before compile process.
-4. Do checkin and unlock to artifacts(bin) after compile process.
-5. You may design a unlock process if autocompile fails.

## TODO
The preCMD and postCMD is design to add pcli script, 
however, maybe commandhelper run by passing list , 
"RUN -y -ns" as preCMD to auto answer yes won't find rest command
It may be correct by trans preCMD with command to a signle string.
