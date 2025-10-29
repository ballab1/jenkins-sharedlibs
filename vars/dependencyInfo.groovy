import java.nio.file.*
import static java.nio.file.StandardWatchEventKinds.*
import java.security.MessageDigest

//--------------------------------------------------------------------------------
// The dependencyInfo.groovy file generates a hash-based signature of a project environment,
// incorporating elements like project settings, Git state, Docker images, and build arguments.
//--------------------------------------------------------------------------------

def call(String settingsFile = 'project.settings') {
    def config = getProjectSettings(settingsFile)
    if (!config) {
        return ''
    }
    def dependencies = new StringBuilder()

    def dirty = getDirtyWorkspace(dependencies)
    def treeSha = getGitTreeHash(dirty)
    dependencies << treeSha

if (0) {
    def argsJson = getBuildArgs(config)
    dependencies << argsJson

    def baseImageId = getBaseImageDigest(config)
    dependencies << baseImageId

    def cbfInfo = getCBFVersionInfo(baseImageId)
    dependencies << cbfInfo
}
    return sha256GString(dependencies.toString())
}

//--------------------------------------------------------------------------------
private def generateSha256(gStr) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256")

    if (!gStr) {
        def len = gStr.length()
        if (len <= 8192) {
            byte[] bytes = gStr.toString().getBytes("UTF-8")
            digest.update(bytes, 0, len)
        }
        else {
            gStr.withInputStream { stream ->
                byte[] buffer = new byte[8192]
                int bytesRead
                while ((bytesRead = stream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
        }
    }
    return digest.digest().encodeHex().toString()
}

//--------------------------------------------------------------------------------
private def getBaseImageDigest(config) {
    def baseImage = getImageParent(config)
    def baseDigest = execute("docker inspect ${baseImage}")
    def baseImageJson = new groovy.json.JsonSlurper().parseText(baseDigest)[0]
    return baseImageJson?.Id
}

//--------------------------------------------------------------------------------
private def getBuildArgs(config) {
    def argsJson = config?.build?.args
    if (argsJson?.containsKey('FROM_BASE'))
        argsJson.removeKey('FROM_BASE')
    return argsJson
}

//--------------------------------------------------------------------------------
private def getCBFVersionInfo(baseId) {
    def buildDir = new File('build')
    if (buildDir.exists()) {
        def localDir = buildDir.listFiles()?.find { it.name.contains('container_build_framework') }?.path
        if (localDir) {
            def checksum = getFilesChecksum(localDir)
            def fingerprint = sha256GString(checksum)
            return "local-${fingerprint}"
        }
    }
    return getCBFVersionOrLabel(baseId)
}

//--------------------------------------------------------------------------------
private def getCBFVersionOrLabel(baseImageJson) {
    def version = baseImageJson?.Config?.Labels?.'version.cbf'
    if (version) {
        return version
    }
    if (new File('.').canonicalFile.name == 'base_container') {
        def cbfPath = new File('../container_build_framework')
        if (cbfPath.exists()) {
            def cbf_version = getVersionTag('../container_build_framework')
            if (!cbf_version) {
                throw new RuntimeException("No version specified for CBF")
            }
            return cbf_version
        }
    }
    return ''
}

//--------------------------------------------------------------------------------
private def getDirtyWorkspace(dependencies) {
    def dirty = ''
    execute('git status --porcelain').eachLine { line ->
        def parts = line.trim().split(/\s+/, 2)
        def status = parts[0]
        def file = parts[1]
        switch (status) {
            case ~'A|M|MM':
                dependencies << sha256File(file)
                dirty = '*'
                break
            case ~'R|D':
                dependencies << line
                dirty = '*'
                break
            default:
                dependencies << line
        }
    }
    return dirty
}

//--------------------------------------------------------------------------------
private def getFilesChecksum(String dir) {
    if (!dir) {
        println "Missing directory path"
        return ''
    }
    def result = new StringBuilder()

    // Traverse directory using Java NIO
    Files.walk(Paths.get(dir))
         .filter { Files.isRegularFile(it) }  // Only include regular files
         .forEach { file ->
             result << sha256File(file)
         }
    return result.toString()
}

//--------------------------------------------------------------------------------
private def getGitTreeHash(dirty) {
    def treeHash = execute('git ls-tree HEAD .')
    def treeSha = sha256GString(treeHash)
    return "${dirty}${treeSha} ${new File('.').canonicalFile.name}"
}

//--------------------------------------------------------------------------------
private String getImageParent(config){
    return config?.FROM_BASE
}

//--------------------------------------------------------------------------------
private def getVersionTag(String dir = '.') {

    // Navigate to the directory
    def currentDir = new File(dir)
    if (!currentDir.exists() || !currentDir.isDirectory()) {
        throw new IllegalArgumentException("The specified path is not a valid directory: $dir")
    }

    // Execute git describe to get the version
    def version = execute("git -C ${dir} describe --tags --always --dirty")

    // Count untracked files
    def untracked = execute("git -C ${dir} ls-files --others --exclude-standard")
    def untrackedCount = untracked.split('\n').length

    // Check for untracked files and modify version accordingly
    if (untrackedCount > 0) {
        if (version.endsWith('-dirty')) {
            version += '+'
        } else {
            version = "${version}-dirty-"
        }
    }

    return version
}

//--------------------------------------------------------------------------------
private def sha256GString(String str) {
    return generateSha256(str)
}

//--------------------------------------------------------------------------------
private def sha256File(Path filePath) {
    return generateSha256(filePath.toFile())
}

//--------------------------------------------------------------------------------
private def sha256File(String filePath) {
    def file = new File(filePath)
    return generateSha256(file)
}
