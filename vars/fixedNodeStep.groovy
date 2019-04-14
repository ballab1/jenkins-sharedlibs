
def call(Closure body, String item_name) {
  return {
    stage (item_name) {
      timestamps {
        ws {
          checkout scm
          script {
              body(item_name)
          }
        }
      }
    }
  }
}