# PVCS-CICO
This plugin is design for MicorFocus/ Serena Deploy Automation.
contorl pcli to check in and check out codes/ artifacts.

## How to use
1. clone this project to somewhere on your disk.
1. Run generateZip.cmd
1. In DA > Administration > Automation , load this projectname.zip to plugins
1. While design component, The appears in Repositories > Source
1. Do checkout and lock to artifacts(bin) before compile process.
1. Do checkin and unlock to artifacts(bin) after compile process.
1. You may design a unlock process if autocompile fails.

## TODO
The preCMD and postCMD is design to add pcli script, 
however, maybe commandhelper run by passing list , 
"RUN -y -ns" as preCMD to auto answer yes won't find rest command
It may be correct by trans preCMD with command to a signle string.
However, String sb = putCommand.join(" ") fails to pass pcli command entities.
The solutions maybe to use pcli script "live to Run -S$script"