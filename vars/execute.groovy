
//--------------------------------------------------------------------------------
// execute.groovy file defines a simple function that executes a given command string
// and returns its output.
//--------------------------------------------------------------------------------

def call(String cmd){
    final int MAX_LEN = 256
    if (!cmd) {
        println 'unexpected NULL passed'
        return ''
    }
    if (cmd.length() > MAX_LEN) {
        println 'command line too long'
        return ''
    }
    try {
       cmd.execute().text.trim()
    }
    catch (IOException e) {
        println "IO Exception while executing: ${cmd}. Error: ${e.message}"
    }
    catch (SecurityException e) {
        println "Security Exception while executing: ${cmd}. Error: ${e.message}"
    }
    catch (Exception e) {
        println "Unexpected error while executing: ${cmd}. Error: ${e.message}"
    }
}
