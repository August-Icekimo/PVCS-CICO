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
// def promotionGroup = props['promotionGroup'];
def user           = props['user'];
def password       = props['password'];
def changeDescription   = props['changeDescription'];
    changeDescription = "-m$changeDescription" + "."
def unlockPath      = props['unlockPath'];
def preCMD       = props['preCMD'];
def postCMD       = props['postCMD'];

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
// MAY DEPRECATED in future
//------------------------------------------------------------------------------

workDir.mkdirs()

if (!workDir.isDirectory()) {
    throw new Exception("Could not create working directory ${workDir}")
}

//------------------------------------------------------------------------------
// PREPARE COMMAND LINE
//------------------------------------------------------------------------------

def addFilesCommand = [pcliPath]
addFilesCommand <<  "addfiles"
addFilesCommand <<  "-pr" + databasePath

if (id != null) {
    addFilesCommand << "-id" + id
}

addFilesCommand << "-t."

//Args "-v"
// Assigns a version label to the new revision. Note, you can use the -yv or -nv option to
// automatically answer Yes or No to prompts to reassign an existing label.
addFilesCommand << "-v" + label

//Args "-z" 
// Includes revisions in subprojects.
addFilesCommand << "-z"
addFilesCommand << basePath + "\\" + unlockPath

def putCommand = [pcliPath]

//Funnel the command you are trying to execute through the run command,
//  and pass it either -y or -n. Since the run command strips quotes
//  by default it is wise to also pass it the -ns (no strip) option.
putCommand << preCMD

putCommand << "Put"

putCommand << "-pr" + databasePath


if (id != null) {
    putCommand << "-id" + id
}

// if (label != null && label.trim().length() > 0) {
//     command << "-r" + label.trim()
// }
// else if (branch != null && branch.trim().length() > 0) {
//     command << "-r" + branch.trim()
// }
// else if (promotionGroup != null && promotionGroup.trim().length() > 0) {
//     command << "-g" + promotionGroup.trim() 
// }
putCommand << "-v" + label

//Args "-yv" 
// Reassigns the version label specified by the -v option to the new revision, if the version
// label exists.
putCommand << "-yv"

//Args "-m"
// Specifies the change description for the revision.
// -mdescription specifies a description at the command-line.
// To end the description, place a period (.) on a line by itself.
putCommand << changeDescription

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

//Args "-a" 
// Specifies an alternate location from which to check in workfiles, rather than the location to
// which they were checked out. The check out location is used unless you specify either the
// -a or -fw option
// command << "-a" + workDir.absolutePath
// command << projectPath

//Args "-z" 
// Includes revisions in subprojects.
putCommand << "-z" 
putCommand << "/" + unlockPath

putCommand << postCMD
//------------------------------------------------------------------------------
// EXECUTE
//------------------------------------------------------------------------------
runCommand('Add new files to the lable', addFilesCommand)

runCommand('PVCS Checkin and lable', putCommand)
