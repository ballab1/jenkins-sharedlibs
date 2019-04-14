#!/usr/bin/env groovy

def call(def obj) {
   String json = ''
   if (obj instanceof Map) {
      json += '{'
      obj.each { k,v ->  json += String.format('"%s":%s,', k, toJSON(v)) }
      return json.substring(0,json.length()-1) + '}'
   }
   if (obj instanceof ArrayList) {
      json += '['
      obj.each { it -> json += toJSON(it)+ ',' }
      return json.substring(0,json.length()-1) + ']'
   }
   if (obj instanceof String) {
      return '"'+obj+'"'
   }
   return obj
}
