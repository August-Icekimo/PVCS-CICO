final def workDir = new File('.').absoluteFile
final def out = System.out
final def env = System.getenv()

final def isWindows = (System.getProperty('os.name') =~ /(?i)windows/).find()
final def ahptool = isWindows ? 'ahptool.cmd' : 'ahptool'

//------------------------------------------------------------------------------
// GET ALL INPUT PARAMETERS 
//------------------------------------------------------------------------------
def props = new Properties();
def inputPropsFile = new File(args[0]);
def inputPropsStream = new FileInputStream(inputPropsFile);
try {
    props.load(inputPropsStream);
}
finally {
    inputPropsStream.close();
}

def pcliPath       = props['pcliPath'];
    pcliPath = '\"' + "$pcliPath" + '\"'  
def databasePath   = props['databasePath'];
def basePath       = props['basePath'];
// def projectPath    = props['projectPath'];
// def branch         = props['branch'];
def label          = props['label'];
// label = "-v\"$label\""
label = "-v\\\"$label\\\"" //escape if space inside
// def promotionGroup = props['promotionGroup'];
def user           = props['user'];
def password       = props['password'];
def changeDescription   = props['changeDescription'];
    // changeDescription = "-m\\\"$changeDescription\\\"" + "."
def unlockPath      = props['unlockPath'];
// def preCMD       = props['preCMD'];
// def postCMD       = props['postCMD'];

def id = null
    if (user != null && user.trim().length() > 0) {
        if (password != null && password.trim().length() > 0) {
            id = user.trim() + ":" + password.trim()
        }
        else {
            id = user.trim()
        }
    }
        
//------------------------------------------------------------------------------
def runCommand = {def message, def command ->
    def builder = new ProcessBuilder(command as String[]);
    println()
    if (message) {
        println(message)
    }
    println("command: ${builder.command().join(' ')}")
    def process = builder.start()
    process.consumeProcessOutput(out, out)
    process.getOutputStream().close()
    process.waitFor()
    if (process.exitValue()) {
    // express exit value in pcli references. 
        switch ( process.exitValue()) {
            case "0":
            println("No problem.")
            break
            case "-2":
            println("PCLI command not found.")
            break
            case "-3":
            println("A non-PCLI related error or a command-specific error.")
            break
            case "-6":
            println("An invalid argument was specified.")
            break
            case "-7":
            println("An argument for a flag that is not needed.")
            break
            case "-8":
            println("A missing argument for a flag.")
            break
            case "-9":
            println("Wrong type was specified for an option's argument.")
            break
            case "-10":
            println("The specified file name cannot be read.")
            break
            case "-11":
            println("A required argument is missing.")
            break
            case "-12":
            println("A security exception occurred.")
            break
            case "-13":
            println("An unknown problem.")
            break
        }
        throw new Exception("PUT Command failed with exit code: " + process.exitValue())
    }
}

//------------------------------------------------------------------------------
// PREPARE WORKING DIRECTORY
// The place to put $changeDescription
//------------------------------------------------------------------------------

println("Agent workDir: ${workDir}.absolutePath ")
workDir.mkdirs()

if (!workDir.isDirectory()) {
    throw new Exception("Could not create working directory ${workDir}")
}
def curTime = System.currentTimeMillis()
def description = new File("desc${curTime}")
description.deleteOnExit()
description.createNewFile()
description.write("")
description.write(changeDescription)

//------------------------------------------------------------------------------
// PREPARE COMMAND LINE
//------------------------------------------------------------------------------

def addCommand = [pcliPath]
addCommand <<  "addfiles"
addCommand <<  "-pr" + databasePath

//Args  -pp -ppproject_path
// Specifies the project or folder to which the files will be added. This option overrides the
// value of the PCLI_PP variable for a single command execution. If no project is specified,
// the PCLI_PP variable is used.
def addfilesPath = ( props['unlockPath'] =~ /^\/.*\// )
println(" -pp $addfilesPath[0]")
addCommand << "-pp" + addfilesPath[0]

if (id != null) {
    addCommand << "-id" + id
}

addCommand << "-t."

//Args "-v"
// Assigns a version label to the new revision. Note, you can use the -yv or -nv option to
// automatically answer Yes or No to prompts to reassign an existing label.
addCommand << label

//Args "-z" 
// Includes revisions in subprojects.
addCommand << "-z"
addCommand << basePath

def putCommand = [pcliPath]
//Funnel the command you are trying to execute through the run command,
//  and pass it either -y or -n. Since the run command strips quotes
//  by default it is wise to also pass it the -ns (no strip) option.
// putCommand << preCMD

putCommand << "Put"

putCommand << "-pr" + databasePath

if (id != null) {
    putCommand << "-id" + id
}

// if (label != null && label.trim().length() > 0) {
//     putCommand << "-r" + label.trim()
// }
// else if (branch != null && branch.trim().length() > 0) {
//     putCommand << "-r" + branch.trim()
// }
// else if (promotionGroup != null && promotionGroup.trim().length() > 0) {
//     putCommand << "-g" + promotionGroup.trim() 
// }
putCommand << label

//Args "-yv" 
// Reassigns the version label specified by the -v option to the new revision, if the version
// label exists.
putCommand << "-yv"

//Args "-m"
// Specifies the change description for the revision.
// -m@file obtains a description from a text file.
putCommand << "-m@$description.absolutePath"

//Args "-bp"
// Specifies the base project path to use in calculating workfile locations when -a or -o has
// been specified. For multiple-file operations, path must be the entity path to a common
// parent of each of the items being checked in.
// command << "-bp" + basePath

//Args "-ym" 
// Uses the change description specified by the -m option for all versioned items.
putCommand << "-ym"

//Args "-nf" 
// Aborts the Put operation if the workfile is unchanged or older.
putCommand << "-nf"

//Args "-z" 
// Includes revisions in subprojects.
putCommand << "-z" 
putCommand << unlockPath

// putCommand << postCMD

//------------------------------------------------------------------------------
// EXECUTE
//------------------------------------------------------------------------------

runCommand('PVCS Checkin and label :\n', putCommand)
runCommand('Add new files to the label :\n', addCommand)