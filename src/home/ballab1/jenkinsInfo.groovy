import hudson.model.*
import hudson.FilePath
import jenkins.model.*
import hudson.util.*
import groovy.json.JsonBuilder


//-----------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------
public class JenkinsProperties {

    private def jenkins

    public JenkinsProperties() {
        jenkins = Jenkins.instance
    }

    //-----------------------------------------------------------------------------------------------
    public def getMasterName() {
        return ('uname -n'.execute().text.toLowerCase() - "\n")
    }

    //-----------------------------------------------------------------------------------------------
    public def getJobMap() {

        def jobMap = [:]
        jenkins.jobNames.sort().each { name ->
           println "querying job: ${name}"
           jobMap[name] = new JenkinsJob(name).properties
           sleep(100)
        }
        return jobMap
    }

    //-----------------------------------------------------------------------------------------------
    public def getLabelMap(def ref = null) {

        def labelMap = [:]
        def label = null
        if (ref) {
            label = jenkins.getLabel(ref)
        }
        jenkins.labels.sort().each { lbl ->
          if (!label || label.grep(~/${lbl}/)) {
              println "querying label: ${lbl.displayName}"
              labelMap[lbl.name] = getLabelProps lbl
           }
        }
        return labelMap
    }

    //-----------------------------------------------------------------------------------------------
    public def getSlaveMap(def ref = null) {

        def slaveMap = [:]
        def computers = jenkins.computerMap
        def label = null
        if (ref) {
            label = jenkins.getLabel(ref)
        }
        computers.each { node, computer ->
          if (!label || label.contains(node)) {
              println "querying node: ${node.displayName}"
              slaveMap[node.displayName?:'master'] = new JenkinsNode(node, computer).properties
              sleep(100)
          }
        }
        return slaveMap
    }

    //-----------------------------------------------------------------------------------------------
    public def getViewMap() {

        def viewMap = [:]
        jenkins.views.sort().each { view ->
           println "querying view: ${view.displayName}"
           viewMap[view.viewName] = new JenkinsView(view).properties
        }
        return viewMap
    }

    }

    //-----------------------------------------------------------------------------------------------
    public def getProperties() {

        return [lastUpdate: (new Date()).toString(),
                workspace:  build.workspace ?: '/home/jenkins',
                masterUrl:  jenkins.rootUrl,
                masterName: this.masterName,
                slaveMap:   this.slaveMap,
                jobMap:     this.jobMap,
                viewMap:    this.iewMap,
                labelList:  this.labelMap
               ]
    }
}

//-----------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------
public class JenkinsAgent {

    private def agent

    JenkinsAgent(def agent) {
        this.agent = agent
    }


    //-----------------------------------------------------------------------------------------------
    public def getType() {

        if (agent instanceof hudson.model.Hudson) {
            return 'master'
        }

        if (agent instanceof hudson.slaves.SlaveComputer) {
           if (agent.isUnix()) {
              return 'unix'
           }
           else {
              return 'windows'
           }
        }
        return ''
    }


    //-----------------------------------------------------------------------------------------------
    public def runGroovy(def cmd) {

        try {
            def multi = RemotingDiagnostics.executeGroovy(cmd, this.agent.channel)
            def lines = multi.readLines()
            lines[0] = lines[0].substring(8)   // chop of 'Results:' from start of first line
            return lines
        }
        catch (e) {
            println "${this.agent.displayName}: unable to determine 'cmd'.  Exception: ${e.message}"
        }
        return [ '', '' ]
    }

    //-----------------------------------------------------------------------------------------------
    public def runCmd(def cmd) {

        def gCmd = """
def proc = '${cmd}'.execute()
proc.waitFor()
proc.text
"""
        return this.runGroovy(gCmd)
    }

    //-----------------------------------------------------------------------------------------------
    public def runUnixCmd(def cmd) {

        def tmp = new FilePath(this.agent.channel, '/var/tmp')
        def xx = tmp.createTextTempFile('tmp','.sh', cmd)
        xx.chmod(0744)
        return this.runCmd(xx.remote)
    }

    //-----------------------------------------------------------------------------------------------
    public def getAgentProps() {

        def props = [:]

        props.url = this.agent.url
        props.hostName = this.agent.hostName
        props.hostCaption = this.agent.caption
        props.jobs = this.agent.tiedJobs.collect{ it.name }.findAll{ !it.contains('=') }.unique().sort()
        return props
    }
}

//-----------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------
public class JenkinsNode : JenkinsAgent {

    private def node

    JenkinsNode(def node, def agent) : JenkinsAgent(agent) {
        this.node = node
    }

    //-----------------------------------------------------------------------------------------------
    public def getNodePropsMaster() {

        def props = [:]

        props.nodeName = 'master'
        props.label = 'master'
        props.label = this.node.displayName
        props.uname = masterName
        props.displayName = this.node.displayName
        props.description = this.node.nodeDescription
        props.labelString = this.node.labelString
        props.labels = this.node.assignedLabels.collect { it.name }
        props.isUnix = false
        return props
    }

    //-----------------------------------------------------------------------------------------------
    public def getNodeRemotePropsUnixExtended() {

    def cmd = '''#!/bin/bash
echo '-- procinfo -------------------------------------------------------------------'
procinfo
echo '-- df -------------------------------------------------------------------------'
df
echo '-- ls node.root ---------------------('+node.rootPath.toString()+')-------------------------------'
ls "''' + this.node.rootPath.toString() + '''"
echo '-- ls node.root/workspace -----------------------------------------------------'
ls "''' + this.node.rootPath.toString() + '''/workspace"
echo '-- ls /buildenvs --------------------------------------------------------------'
ls -l /buildenvs
echo '-- git config --global -l -----------------------------------------------------'
git config --global -l
echo '-- ccache -s ------------------------------------------------------------------'
ccache -s
echo '-- cat /etc/c4buildversion ----------------------------------------------------'
cat /etc/c4buildversion
echo '-------------------------------------------------------------------------------'
'''
        return this.runUnixCmd(cmd)
    }

    //-----------------------------------------------------------------------------------------------
    public def getNodeRemotePropsUnix() {

    def cmd = '''#!/bin/bash
uname -n
declare -r ip="$(/sbin/ifconfig | /usr/bin/awk '/inet addr:10\\./ { print substr($2, 6) }')"
if [[ "$ip" ]]; then
  /bin/hostname -f
  echo "$ip"
else
  /usr/bin/nslookup "$(/bin/hostname -f)" | /usr/bin/awk '{ if (NR == 4 || NR == 5) {print $2}}'
fi
nproc --all
grep MemTotal /proc/meminfo | awk '{print $2}'
ccache -s | grep 'max cache size' | awk '{ if ($5=="Kbytes") {print $4} else if ($5=="Mbytes") {print (1024 * $4)} else if ($5=="Gbytes") {print (1024 * 1024 * $4)}  }'
cat /etc/c4buildversion
'''

        def data = this.runUnixCmd(cmd)

        def props = [ uname:  data[0],
                        fqn:  data[1],
                         ip:  data[2],
                       cpus:  data[3],
                        mem:  data[4],
                     ccache:  data[5],
                      c4dev:  data[6]
                    ]
        return props
    }

    //-----------------------------------------------------------------------------------------------
    public def getNodePropsUnix() {

    def cmd2 = '''#!/bin/bash
/usr/bin/nslookup "$(/bin/hostname -f)" | /usr/bin/awk '{ if (NR == 4 || NR == 5) {print $2}}'
'''

        def uname = this.runCmd('uname -n')
        def inet = this.runUnixCmd(cmd2)

        def props = [:]
        props.uname = uname[0]
        props.fqn = inet[0]
        props.ip = inet[1]
        props.isUnix = true

        return props
    }

    //-----------------------------------------------------------------------------------------------
    public def getNodePropsWindows() {

        def props = this.windowsIpconfig
        props += this.this.windowsNslookup
        props.isUnix = false
        return props
    }

    //-----------------------------------------------------------------------------------------------
    public def getWindowsIpconfig() {

        def pattern = ~/^.+:\s*(\w)/

        def props = [:]
        def inet = this.runCmd('ipconfig /all').find{ it =~ /Host Name/ }
        def matches = pattern.matcher(inet)
        props.uname = inet.substring(39)
        return props
    }

    //-----------------------------------------------------------------------------------------------
    public def getWindowsNslookup() {

        def props = [:]
        def inet = this.runCmd('nslookup ' + props.uname)
        props.fqdn = inet[3].substring(9)
        props.ip = inet[4].substring(10)
        return props
    }

    //-----------------------------------------------------------------------------------------------
    public String getTypeFactoryProps() {

        switch(this.type) {
            case 'master':
                return this.nodePropsMaster
            case 'unix':
                return this.nodePropsUnix
            case 'windows':
                return this.nodePropsWindows
        }
        return ''
    }

    //-----------------------------------------------------------------------------------------------
    public def getNodeProps() {

        def props = [:]
        props.assignedlabels = node.assignedLabels.collect{ it.name }.sort()
        props.descriptor = this.node.descriptor.displayName
        props.label = this.node.labelString
        props.mode = this.node.mode.toString()
        props.nodeDescription = this.node.nodeDescription
        props.nodeName = this.node.displayName
        props.rootpath = this.node.rootPath.toString()
        props.numExecutors = this.node.numExecutors
        props.remoteFS = (this.node instanceof hudson.model.Slave) ? this.node.remoteFS : null
        return props
    }

    //-----------------------------------------------------------------------------------------------
    public def getProperties() {

        def props = this.typeFactoryProps
        props += this.agentProps
        props += this.nodeProps

        return props
    }
}

//-----------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------
public class JenkinsLabel {

    private def label

    JenknsLabel(def label) {
        this.label = label
    }

    //-----------------------------------------------------------------------------------------------
    public def getProperties() {

        def props = [:]
        props.name = label.displayName
        props.description = label.description
        props.class = label.class.toString()
        props.jobs = label.tiedJobs.collect{ it.name?:'master' }.findAll{ !it.contains('=') }.unique().sort()
        props.nodes = label.nodes.collect{ it.nodeName?:'master' }.unique().sort()
        props.totalExecutors = label.totalExecutors
        props.totalConfiguredExecutors = label.totalConfiguredExecutors
        props.url = label.url
        return props
    }
}

//-----------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------
public class JenkinsJob {

    private def job

    public JenkinsJob(def job) {
        this.job = job
    }


    //-----------------------------------------------------------------------------------------------
    public def getProperties() {

        def props = [:]

        props.inQueue = job.inQueue
        props.firstBuild = job.firstBuild?.number
        props.lastBuild = job.lastBuild?.number
        props.building = job.lastBuild?.building

        props.lastStableBuild = job.lastStableBuild?.number
        props.lastStableBuild_duration = formatTime(job.lastStableBuild?.duration)
        props.lastSuccessfulBuild = job.lastSuccessfulBuild?.number
        props.lastSuccessfulBuild_duration = formatTime(job.lastSuccessfulBuild?.duration)
        props.lastUnstableBuild = job.lastUnstableBuild?.number
        props.lastUnstableBuild_duration = formatTime(job.lastUnstableBuild?.duration)
        props.lastUnsuccessfulBuild = job.lastUnsuccessfulBuild?.number
        props.lastUnsuccessfulBuild_duration = formatTime(job.lastUnsuccessfulBuild?.duration)
        props.class = job.class.toString()

        if (! job instanceof org.jenkinsci.plugins.workflow.job.WorkflowJob) {
            props.pollsSCM = this.pollsSCM
            props.scmProps = this.scmProperties
            props.label = job.assignedLabelString
            props.downstreamProjects = job.downstreamProjects?.collect{ it.name }.unique().sort()
            props.upstreamProjects = job.upstreamProjects?.collect{ it.name }.unique().sort()
            props.workspace = job.workspace.toString()
        }
        return props
    }

    //-----------------------------------------------------------------------------------------------
    public def getPollsSCM() {
        try {
            return (job.getTrigger(hudson.triggers.SCMTrigger.class) != null)
        }
        catch (e) {
            println "unable to determine 'SCMtrigger'.  Exception: ${e.message}"
            return ''
        }
    }

    //-----------------------------------------------------------------------------------------------
    public def getScmProperties() {

        props = [:]

        def usesGIT = (this.job.scm instanceof hudson.plugins.git.GitSCM)
        if (usesGIT) {
            return null
        }

        props.configVersion = scm.configVersion
        props.doGenerateSubmoduleConfigurations = scm.doGenerateSubmoduleConfigurations
        props.submoduleCfg = scm.submoduleCfg
        def remoteConfig = scm.userRemoteConfigs
        remoteConfig.'hudson.plugins.git.UserRemoteConfig'.each { it ->
            props.remoteConfig = it.url
        }
        def branches = scm.branches
        branches.'hudson.plugins.git.BranchSpec'.each { it ->
            props.name = it.name
        }
        props.usesGit = true
        return props
    }
}

//-----------------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------
public class JenkinsView

    private def view

    public JenkinsView(def view) {
        this.view = view
    }

    //-----------------------------------------------------------------------------------------------
    public def getViewProperties() {

        def props = [:]

        props.displayName = view.displayName
        props.url = view.url
        props.viewName = view.viewName
        props.items = view.items.collect{ it.name }.unique().sort()
        return props
    }
}
''
