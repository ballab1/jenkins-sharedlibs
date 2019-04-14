def call() {
  Jenkins.instance.nodes.collect{ it.name }
}