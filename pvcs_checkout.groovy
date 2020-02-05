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
def projectPath    = props['projectPath'];
def branch         = props['branch'];
def label          = props['label'];
def promotionGroup = props['promotionGroup'];
def cleanWorkspace = props['cleanWorkspace']?.toBoolean();
def user           = props['user'];
def password       = props['password'];

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
            case "-2":
            println("An unknown problem.")
            break
        }
        throw new Exception("Command failed with exit code: " + process.exitValue())
    }
}

//------------------------------------------------------------------------------
// PREPARE WORKING DIRECTORY
//------------------------------------------------------------------------------

if (cleanWorkspace && workDir.isDirectory()) {
    new AntBuilder().delete(includeemptydirs:'true') {
        fileset(dir: workDir.path, includes:'**/*', defaultexcludes:'false')
    }
}

workDir.mkdirs()

if (!workDir.isDirectory()) {
    throw new Exception("Could not create working directory ${workDir}")
}

//------------------------------------------------------------------------------
// PREPARE COMMAND LINE
//------------------------------------------------------------------------------
  
def command = [pcliPath]
command << "Get"
// DEPRECATED Arg "Quietly ignores nonexistent entities."", we need noise.
//command << "-qe"
command << "-y"
// Args "-o" 
// Overrides the workfile locations defined in the project and versioned files,
// and instead uses a hierarchy of directories that mirror the structure and names of the
// project and subprojects. Note if you do not use this option, any versioned file or
// project that has an absolute workfile location associated with it will be copied to
// that workfile location, even if you specify a workspace or use the -a option.
command << "-o"
// Args "-bp" 
// Specifies the base project path to use in calculating workfile locations when
// the -a option has been specified.
command << "-bp" + basePath
// Args "-l"
// Locks the revision of the file you are getting. Optionally, allows you to specify the revision
// to lock. By default, the default revision defined for the workspace is acted on. Note the revision
// was assign with the -r args below.
command << "-l"
if (id != null) {
    command << "-id" + id
}
// Args "-pr" 
// Sets the current project database for this command execution.
command << "-pr" + databasePath
// Args "-r"
// Specifies the revision, promotion group, or version to act upon.
if (label != null && label.trim().length() > 0) {
    command << "-r" + label.trim()
}
else if (branch != null && branch.trim().length() > 0) {
    command << "-r" + branch.trim()
}
else if (promotionGroup != null && promotionGroup.trim().length() > 0) {
    command << "-g" + promotionGroup.trim() 
}
//Args "-a" 
// Specifies an alternate location to place workfiles, rather than the location defined in the
// workspace. See also the -o and -bp options.
// For single file checkouts, the alternate location can be either a directory or an alternate
// name for the file itself. If the specified leafname of the destination path:
//  Does not exist and the -bp option was not used, the leafname is assumed to be an
//  alternate filename.
//  Already exists as a directory, the file will be placed into that directory using its 
//  original name.
//  If the -bp option is used, the leafname is always assumed to be a directory; additional
//  subdirectories may be created depending on how the -bp option was used.
command << "-a" + workDir.absolutePath
command << projectPath
//Args "-z" Includes versioned files in subprojects.
command << "-z" + "/"
//------------------------------------------------------------------------------
// EXECUTE
//------------------------------------------------------------------------------

runCommand('PVCS Checkout', command)
