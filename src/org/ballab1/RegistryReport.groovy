package org.ballab1


import groovy.json.*
import java.awt.AttributeValue
import java.util.Map
import org.codehaus.groovy.tools.shell.util.Logger


class RepoEntry {
    String digest
    String createTime
    ArrayList<String> tags = []

    RepoEntry(Object data) {
        digest = data.digest
        createTime = data.createTime
        tags = data.tags
    }
    String toString() {
        String out = '       ' + digest + ', ' + createTime + '\n'
        tags.sort().each { k ->
            out += '          ' + k.toString() + '\n'
        }
        out
    }
}

class RepoContents {
    String name
    String id
    ArrayList<RepoEntry> digests = []

    RepoContents(Object data) {
        name = data.repository
        id = data.id
        data.digests.each { x ->
            digests += new RepoEntry(x)
        }
    }

    int size() {
        digests.size()
    }

    String toString() {
        String out = id + ', ' + name + ', Images: ' + digests.size() + '\n'
        digests.sort().each { k ->
            out += k.toString() + '\n'
        }
        out
    }
}

class RegistryData {
    String base
    def log
    ArrayList<RepoContents> repos = []

    RegistryData() {
        base = System.getenv('BASE') ?: '/home.config'
        log = Logger.create(getClass())
        Map json = readJson(System.getenv('JSON'))
        parser(json)
    }

    def readJson(String filename) {

        filename = filename ?: "${base}/registryReport.json"
        def jsonFileData = new File(filename)

        def slurper = new JsonSlurper()
        slurper.parseText('{"data":'+jsonFileData.text+'}')
    }

    def parser(Map json) {
        json.data.each { k ->
            repos += new RepoContents(k)
        }
    }

    String toString() {
        String out = ''
        repos.each { r ->
             out += r.toString()
        }
        out
    }

    def static main()
    {
        def processor = new RegistryData()
        def out = new File('c:/home.config/x.txt')
        if ( out.exists() ) {
            out.delete()
        }
        out << processor.toString()
        println 'done.'
        ''
    }
}
