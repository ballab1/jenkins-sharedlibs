#!/usr/bin/env groovy

def call(Map args)
{
  if (args.file) {
      String content = readFile args.file
      Properties props = new Properties()
      props.load(new StringReader(content))
      Properties propvals = new Properties()
      props.each { k,v ->
          def val = v.toString()
          def chr = val.substring(0, 1)
          if (chr == "'" || chr == '"') {
            if (val.length() == 2) {
                val = ''
            }
            else {
               val = val.substring(1, val.length()-2)
            }
          }
          propvals[k] = val
      }
      return propvals
   }
   return null
}
