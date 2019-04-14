
def call(Closure body, String nodeName) {
  return {
    node(nodeName) {
      timestamps {
        ws {
          checkout scm
          body(nodeName)
        }
      }
    }
  }
}