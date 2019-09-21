def call() {
  def nodes = []
  Jenkins.instance.nodes.each {
    if (it.toComputer().isOnline()) {
      nodes += it.name
    }
  }
  nodes
}