def call (ArrayList<String> nodes, targetDir, Closure action) {
  nodes.each{ name ->
    namedStage(name, targetDir, action)
  }
}