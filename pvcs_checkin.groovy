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
def databasePath   = props['databasePath'];
def basePath       = props['basePath'];
// def projectPath    = props['projectPath'];
// def branch         = props['branch'];
def label          = props['label'];
// def promotionGroup = props['promotionGroup'];
// def cleanWorkspace = props['cleanWorkspace']?.toBoolean();
def user           = props['user'];
def password       = props['password'];
def changeDescription   = props['changeDescription'];
def unlockPath           = props['unlockPath'];
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
    express exit value in pcli references. 
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
        throw new Exception("GET Command failed with exit code: " + process.exitValue())
    }
}

//------------------------------------------------------------------------------
// PREPARE WORKING DIRECTORY
// MAY DEPRECATED in future
//------------------------------------------------------------------------------

// if (cleanWorkspace && workDir.isDirectory()) {
//     new AntBuilder().delete(includeemptydirs:'true') {
//         fileset(dir: workDir.path, includes:'**/*', defaultexcludes:'false')
//     }
// }

workDir.mkdirs()

if (!workDir.isDirectory()) {
    throw new Exception("Could not create working directory ${workDir}")
}

//------------------------------------------------------------------------------
// PREPARE COMMAND LINE
//------------------------------------------------------------------------------
  
def command = [pcliPath]
command << "Put"

//Args "-v"
// Assigns a version label to the new revision. Note, you can use the -yv or -nv option to
// automatically answer Yes or No to prompts to reassign an existing label.
command << "-v" + label
//Args "-yv" 
// Reassigns the version label specified by the -v option to the new revision, if the version
// label exists.
command << "-yv"

//Args "-m"
// Specifies the change description for the revision.
// -mdescription specifies a description at the command-line.
// To end the description, place a period (.) on a line by itself.
changeDescription = "-m" + changeDescription + "."

//Args "-bp"
// Specifies the base project path to use in calculating workfile locations when -a or -o has
// been specified. For multiple-file operations, path must be the entity path to a common
// parent of each of the items being checked in.
command << "-bp" + basePath

//Args "-ym" 
// Uses the change description specified by the -m option for all versioned items.
command << "-ym"

//Args "-nf" 
// Aborts the Put operation if the workfile is unchanged or older.
command << "-nf"

if (id != null) {
    command << "-id" + id
}

command << "-pr" + databasePath
// if (label != null && label.trim().length() > 0) {
//     command << "-r" + label.trim()
// }
// else if (branch != null && branch.trim().length() > 0) {
//     command << "-r" + branch.trim()
// }
// else if (promotionGroup != null && promotionGroup.trim().length() > 0) {
//     command << "-g" + promotionGroup.trim() 
// }

//Args "-a" 
// Specifies an alternate location from which to check in workfiles, rather than the location to
// which they were checked out. The check out location is used unless you specify either the
// -a or -fw option
command << "-a" + workDir.absolutePath
command << projectPath


//Args "-z" 
// Includes revisions in subprojects.
command << "-z" 
command << "/" + unlockPath
//------------------------------------------------------------------------------
// EXECUTE
//------------------------------------------------------------------------------

runCommand('PVCS Checkin and lable', command)
