
//--------------------------------------------------------------------------------
// The getLatest.groovy file defines a funtion to return the short commit ref
// which coresponds to the latest commit
//--------------------------------------------------------------------------------
String call(String repo = System.env['GIT_URL']) {

    def cmd = repo?.trim() ? "git ls-remote ${repo} HEAD" : 'git rev-parse HEAD'
    def commitHash = execute(cmd)
    return commitHash?.take(8)
}
