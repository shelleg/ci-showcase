job('OSTKL-init') {
  customWorkspace('/opt/tikal');
  label('master')
  scm {
    git {
      remote {
        name('origin')
        url('git@github.com:tikalk/OSTK-playbook.git')
        credentials('ostkl-admin')
      }
      branch('jenkins-ci')
    }
  }
  steps {
    shell('cd /opt/tikal/deploy; ./deploy.sh init')
  }
  publishers {
    downstream('OSTKL-reqs','SUCCESS')
  }

}

job('OSTKL-reqs') {
  customWorkspace('/opt/tikal');
  wrappers {
    runOnSameNodeAs('OSTKL-init', true)
  }
  scm {
    git {
      remote {
        name('origin')
        url('git@github.com:tikalk/OSTK-playbook.git')
        credentials('ostkl-admin')
      }
      branch('jenkins-ci')
    }
  }
  steps {
    shell('cd /opt/tikal/deploy; ./deploy.sh reqs')
  }
  publishers {
    downstream('OSTKL-vagrant','SUCCESS')
  }

}

job('OSTKL-vagrant') {
  customWorkspace('/opt/tikal');
  wrappers {
    runOnSameNodeAs('OSTKL-init', true)
  }
  scm {
    git {
      remote {
        name('origin')
        url('git@github.com:tikalk/OSTK-playbook.git')
        credentials('ostkl-admin')
      }
      branch('jenkins-ci')
    }
  }
  steps {
    shell('cd /opt/tikal/deploy; ./deploy.sh vagrant')
  }
  publishers {
    downstream('OSTKL-server','SUCCESS')
  }

}

job('OSTKL-server') {
  customWorkspace('/opt/tikal');
  label('sanjer')
  scm {
    git {
      remote {
        name('origin')
        url('git@github.com:tikalk/OSTK-playbook.git')
        credentials('ostkl-admin')
      }
      branch('jenkins-ci')
    }
  }
  steps {
    shell('cd /opt/tikal/deploy; ./deploy.sh vagrant')
  }
  publishers {
    downstream('OSTKL-pubkey','SUCCESS')
  }

}



job('OSTKL-pubkey') {
  customWorkspace('/opt/tikal');
  wrappers {
    runOnSameNodeAs('OSTKL-server', true)
  }
  scm {
    git {
      remote {
        name('origin')
        url('git@github.com:tikalk/OSTK-playbook.git')
        credentials('ostkl-admin')
      }
      branch('jenkins-ci')
    }
  }
  steps {
    shell('cd /opt/tikal/deploy; ./deploy.sh pubkey')
  }
  publishers {
    downstream('OSTKL-net','SUCCESS')
  }
}

job('OSTKL-net') {
  customWorkspace('/opt/tikal');
  wrappers {
    runOnSameNodeAs('OSTKL-server', true)
  }
  scm {
    git {
      remote {
        name('origin')
        url('git@github.com:tikalk/OSTK-playbook.git')
        credentials('ostkl-admin')
      }
      branch('jenkins-ci')
    }
  }
  steps {
    shell('cd /opt/tikal/deploy; ./deploy.sh net')
  }
  publishers {
    downstream('OSTKL-base','SUCCESS')
  }

}

job('OSTKL-base') {
  customWorkspace('/opt/tikal');
  wrappers {
    runOnSameNodeAs('OSTKL-server', true)
  }
  scm {
    git {
      remote {
        name('origin')
        url('git@github.com:tikalk/OSTK-playbook.git')
        credentials('ostkl-admin')
      }
      branch('jenkins-ci')
    }
  }
  steps {
    shell('cd /opt/tikal/deploy; ./deploy.sh base')
  }

}


buildPipelineView('OSTKL-NEW') {
  filterBuildQueue()
  filterExecutors()
  title('Project A CI Pipeline')
  displayedBuilds(5)
  selectedJob('OSTKL-init')
  alwaysAllowManualTrigger()
  showPipelineParameters()
  refreshFrequency(60)
}