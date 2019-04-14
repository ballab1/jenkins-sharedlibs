#!/usr/bin/env groovy

def call(uptime) {
  def reg = ~/^\s*(\S{8})\s+up\s+([^u]+),\s+(\d+){0,1}(?:\susers{0,1},\s+){0,1}load\s+average:\s(\S+),\s+(\S+),\s+(\S+)/
  def m = (uptime =~ reg)[0]
  def data = ['time': m[1],
              'uptime': m[2],
              'userCount': m[3],
              'load': ['01min average': m[4], '05min average': m[5], '45min average': m[6]]
              ]
  return data
}
