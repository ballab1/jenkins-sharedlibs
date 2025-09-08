
//--------------------------------------------------------------------------------
def call(Map overrides = [:]) {
    def keys = [
        'KANIKO_IMAGE': "${env.DOCKER_REGISTRY}docker.io/bitnami/kaniko:1.25.0-debian-12-r4",
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

    // Use a map to initialize envVars
    def envVars = keys.collectEntries { k, v ->
        [(k): (v ?: getEnv(k, overrides))]
    }


    def buildArgs = envVars.collect { k, v -> "--build-arg '${k}=${v}'" }.join(' ')
    def buildParams = [
        '--cache=false',
        '--insecure',
        '--skip-tls-verify',
        '--verbosity info',
        "--context 'dir://${envVars.WORKSPACE}'",
        "--destination '${envVars.TARGET}'",
        '--dockerfile ci/Dockerfile',
        '--insecure',
        '--insecure-pull',
        "--label 'container.build.time=${envVars.CONTAINER_BUILD_TIME}'",
        "--label 'container.fingerprint=${envVars.CONTAINER_FINGERPRINT}'",
        "--label 'container.git.commit=${envVars.CONTAINER_GIT_COMMIT}'",
        "--label 'container.git.refs=${envVars.CONTAINER_GIT_REFS}'",
        "--label 'container.git.url=${envVars.CONTAINER_GIT_URL}'",
        "--label 'container.origin=${envVars.CONTAINER_ORIGIN}'",
        "--label 'container.original.name=${envVars.CONTAINER_NAME}'",
        "--label 'container.os=${envVars.CONTAINER_OS}'",
        "--label 'container.parent=${envVars.FROM_BASE}'",
        "--label 'container.build.host=${envVars.CONTAINER_BUILD_HOST}'",
    ].join(' ')

    def buildCmd = "/kaniko/executor ${buildArgs} ${buildParams}"

    return buildCmd
}

//--------------------------------------------------------------------------------
private String getDefault(String key){

    try {
        switch (key) {
            case 'CONTAINER_BUILD_HOST':
                return execute('hostname -f')
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
                return env['FROM_BASE']
    }
    catch(e) {
        println "Error getting default for '${key}'. Reason: ${e.getMessage()}"
    }
    return ''
}

//--------------------------------------------------------------------------------
private String getEnv(String key, Map overrides){
    if (overrides.containsKey(key))
        return overrides."${key}"
    if (env.containsKey(key))
        return env."${key}"
    return getDefault(key)
}

//--------------------------------------------------------------------------------
