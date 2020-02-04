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
command << "Put"
//command << "-vTEST"
command << "-yv"
//command << "-m TEST"
//command << "-bp" + basePath
command << "-ym"
command << "-nf"
command << "-z"
// command << "lockPath"
if (id != null) {
    command << "-id" + id
}
command << "-pr" + databasePath
if (label != null && label.trim().length() > 0) {
    command << "-r" + label.trim()
}
else if (branch != null && branch.trim().length() > 0) {
    command << "-r" + branch.trim()
}
else if (promotionGroup != null && promotionGroup.trim().length() > 0) {
    command << "-g" + promotionGroup.trim() 
}
command << "-a" + workDir.absolutePath
command << projectPath

//------------------------------------------------------------------------------
// EXECUTE
//------------------------------------------------------------------------------

runCommand('PVCS Checkin', command)
