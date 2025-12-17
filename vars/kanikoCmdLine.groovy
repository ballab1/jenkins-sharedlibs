
//--------------------------------------------------------------------------------
// The kanikoCmdLine.groovy file provides a kaniko command line to build an image
//--------------------------------------------------------------------------------

def call(Map overrides = [:], String settingsFile = 'project.settings') {
    def keys = [
        'KANIKO_IMAGE',
        'WORKSPACE',
        'TARGET',
        'CONTAINER_BUILD_TIME',
        'CONTAINER_FINGERPRINT',
        'CONTAINER_GIT_COMMIT',
        'CONTAINER_GIT_REFS',
        'CONTAINER_GIT_URL',
        'CONTAINER_ORIGIN',
        'CONTAINER_NAME',
        'CONTAINER_OS',
        'CONTAINER_BUILD_HOST',
        'FROM_BASE'
    ]

    overrides.each { k,v ->
        if (! keys.contains(k)) {
            keys += k
        }
    }

    def project = getProjectSettings(settingsFile)
    if (! project || project?.containsKey('env'))
        return ''
    if (project.containsKey('env')){
        project.env.each { k, v ->
            if (! keys.contains(k)) {
                keys += k
            }
        }
    }

    // Use our map to initialize envVars
    def envVars = keys.collectEntries { k ->
        [(k): getEnv(k, overrides, project) ]
    }

    def dockerfile = 'Dockerfile'
    if (! fileExists(dockerfile) && fileExists('ci/Dockerfile')) {
        dockerfile = 'ci/Dockerfile'
    }
    def buildParams = [
        '--cache=false',
        '--skip-tls-verify',
        '--verbosity info',
        '--insecure',
        '--insecure-pull',
        "--context 'dir://${envVars.WORKSPACE}'",
        "--destination '${envVars.TARGET}'",
        "--dockerfile '${dockerfile}'",
        "--label 'container.build.time=${envVars.CONTAINER_BUILD_TIME}'",
        "--label 'container.fingerprint=${envVars.CONTAINER_FINGERPRINT}'",
        "--label 'container.git.commit=${envVars.CONTAINER_GIT_COMMIT}'",
        "--label 'container.git.refs=${envVars.CONTAINER_GIT_REFS}'",
        "--label 'container.git.url=${envVars.CONTAINER_GIT_URL}'",
        "--label 'container.origin=${envVars.CONTAINER_ORIGIN}'",
        "--label 'container.original.name=${envVars.CONTAINER_NAME}'",
        "--label 'container.os=${envVars.CONTAINER_OS}'",
        "--label 'container.parent=${envVars.FROM_BASE}'",
        "--label 'container.build.host=${envVars.CONTAINER_BUILD_HOST}'"
    ]

    def buildArgs = envVars.collect { k, v -> "--build-arg '${k}=${v}'" }

    def buildCmd = "/kaniko/executor \\\n  ${buildArgs.join(' \\\n  ')} \\\n  ${buildParams.join(' \\\n  ')}"

    return buildCmd
}

//--------------------------------------------------------------------------------
private String getDefault(String key){

    try {
        switch (key) {
            case 'KANIKO_IMAGE':
                return "${System.env['DOCKER_REGISTRY']}gcr.io/kaniko-project/executor:v1.24.0.debug"
            case 'CONTAINER_BUILD_HOST':
                return execute('hostname')
            case 'CONTAINER_BUILD_TIME':
                return new Date().format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"))
            case 'CONTAINER_FINGERPRINT':
                return dependencyInfo()
            case 'CONTAINER_GIT_COMMIT':
                return execute('git rev-parse HEAD')
            case 'CONTAINER_GIT_REFS':
                def refs = execute('git log -n 1 --pretty=%d HEAD')
                return refs?.replaceAll(/[^\(]*\(([^\)]*)\).*/, '$1')
            case 'CONTAINER_GIT_URL':
                return execute('git config --get remote.origin.url')
            case 'CONTAINER_ORIGIN':
                return execute('git describe --tags --always --dirty')
            case 'CONTAINER_TAG':
                return containerTag
            case 'CONTAINER_PARENT':
                return System.env['FROM_BASE']
        }
    }
    catch(e) {
        println "Error getting default for '${key}'. Reason: ${e.getMessage()}"
    }
    return ''
}

//--------------------------------------------------------------------------------
/**
 * Resolve a string containing simple shell-style expansions:
 *   ${VAR}       -> value or empty string if unset
 *   ${VAR:-def}  -> value if set and non-empty, otherwise def
 *   ${VAR:?err}  -> value if set and non-empty, otherwise throw IllegalStateException(err)
 *
 * Does not support other bash parameter expansions or command substitution.
 */
private String resolveShellStyle(String input, Map<String,String> env = System.getenv()) {
  if (input == null) return null

  // regex captures: full, name, optional operator+tail, operator (:- or :?), tail text
  def pattern = ~/\$\{([A-Za-z_][A-Za-z0-9_]*)(?:(:-|:\?)(.*?))?\}/

  boolean changed = true
  String result = input
  while (changed) {
    changed = false
    result = result.replaceAll(pattern) { full, name, op, tail ->
      changed = true
      String val = env.containsKey(name) ? env.get(name) : null
      boolean setAndNonEmpty = (val != null && val != '')

      if (!op) {
        return (val != null) ? val : ''
      }

      if (op == ':-') {
        return setAndNonEmpty ? val : (tail ?: '')
      }

      if (op == ':?') {
        if (setAndNonEmpty) return val
        String msg = (tail && tail.trim() != '') ? tail : "required variable ${name} missing"
        throw new IllegalStateException(msg)
      }

      return full
    }
  }
  return result
}

//--------------------------------------------------------------------------------
private String getEnv(String key, Map overrides, def project){
    if (overrides.containsKey(key)) {
       return resolveShellStyle(overrides."${key}", overrides)
    }
    if (System.env.containsKey(key)) {
       return resolveShellStyle(System.env["${key}"])
    }
    if (project.env.containsKey(key)) {
        return resolveShellStyle(project.env[key], project.env)
    }
    return getDefault(key)
}
