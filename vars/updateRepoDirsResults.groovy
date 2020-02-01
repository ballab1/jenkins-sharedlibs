#!/usr/bin/env groovy

import java.text.SimpleDateFormat

def call(def names) {

      if (names.size() == 0) {
        manager.buildUnstable()
        return
      }

      String results = ''

      def sdf = new SimpleDateFormat('MM/dd/yyyy HH:mm:ss')
      results += sdf.format(new Date()) + '\n\n'

      def errors = []
      def shas = [:]

      for (host in names) {
        unstash host
        String inf = host + '.inf'
        if (! fileExists(inf)) {
          errors += host
        }
        else {

          String data = readFile(inf)
          def lines = data.split('\n')
//          echo '  found ' + lines.size() + ' lines in ' + inf

          lines.each{ line ->
            results += line + '\n'

            def has = (line =~ /^\s+([a-f0-9]+)\s+:\s+.*$/)
            if (has.matches()) {
              def id = has.group(1)
              shas[id] = '#'
            }
          }
          results += '\n'
        }
      }
      writeFile file: 'results.txt', text: results


      echo ''
      echo '**************************************************************'
      echo '*'
      if (errors.size() > 0) {
        echo '*  No information found for ' + errors.toString()
        manager.buildFailure()
      }
      else if (shas.size() != 1) {
        echo '*  inconsistent shas found: '
        shas.keySet().each { it ->
          echo '  ' + it
        }
        manager.buildUnstable()
      }
      else {
        echo '*  SUCCESS '
      }
      echo '*'
      echo '**************************************************************'

      archiveArtifacts allowEmptyArchive: true, artifacts: 'results.txt,*.inf'
}
