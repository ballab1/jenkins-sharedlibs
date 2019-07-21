def call(String name, String targetDir, Closure action) {
    stage (name) {
      agent { label name }
      steps {
        dir(targetDir, action)
      }
    }
}