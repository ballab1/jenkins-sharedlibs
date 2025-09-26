#!/usr/bin/env groovy


def call(Map params) {
  if (params.get('venv') == null) {
    error('uploadPythonPagackage: No virtual python environment "venv" specified, nothing uploaded')
  }
  else if (sh(script: "[ -f ${params.venv}/bin/activate ] || echo 'no'", returnStdout: true).trim() == 'no') {
    error('uploadPythonPagackage: No virtual environment exists, nothing uploaded')
  }

  if (params.get('credentialsid') == null) {
    error('uploadPythonPagackage: No credentials id "credentialsid" specified, nothing uploaded')
  }

  if (params.get('package_dir') == null) {
    error('uploadPythonPagackage: No package directory "package_dir" specified, nothing uploaded')
  }

  if (params.get('usedevpi') == null) {
    error('uploadPythonPagackage: No definition for type of upload "usedevpi" specified, nothing uploaded')
  }

  if (params.usedevpi) {
    if (params.get('devpi_server') == null) {
      error('uploadPythonPagackage: No devpi server "devpi_server" specified, nothing uploaded')
    }
    if (params.get('devpi_index') == null) {
      error('uploadPythonPagackage: No devpi index "devpi_index" specified, nothing uploaded')
    }
  }
  else {
    if (params.get('python') == null) {
      error('uploadPythonPagackage: No python executable "python" specified, nothing uploaded')
    }
    if (params.get('repository') == null) {
      error('uploadPythonPagackage: No repository "repository" specified, nothing uploaded')
    }
    if (params.get('pypirc_file') == null) {
      error('uploadPythonPagackage: No pypirc file "pypirc_file" specified, nothing uploaded')
    }
  }

  withCredentials([usernamePassword(credentialsId: params.credentialsid, usernameVariable: 'TWINE_USER', passwordVariable: 'TWINE_PASSWORD')]) {
    if (params.usedevpi) {
      sh """
        source ${params.venv}/bin/activate
        devpi use ${params.devpi_server}/${params.devpi_index}
        devpi login ${TWINE_USER} --password=${TWINE_PASSWORD}
        devpi upload --from-dir ${params.package_dir}
      """
    }
    else {
      sh """
        pwd
        hostname
        ls
        source ${params.venv}/bin/activate
        ${params.python} -m twine upload --verbose --disable-progress-bar --non-interactive --repository ${params.repository} ./${params.package_dir}/* --config-file ${params.pypirc_file}
      """
    }
  }
}
