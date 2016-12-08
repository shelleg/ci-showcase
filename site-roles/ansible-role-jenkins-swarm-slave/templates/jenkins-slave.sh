#!/bin/bash

#
# Jenkins Swarm Client
#
# chkconfig: 2345 89 9
# description: jenkins-swarm-client

source /etc/rc.d/init.d/functions

[[ -e "/etc/jenkins/swarm-client" ]] && source "/etc/jenkins/swarm-client"

####
#### The following options can be overridden in /etc/jenkins/swarm-client
####

# The user to run swarm-client as
USER=${USER:="jenkins"}

# The location that the pid file should be written to
PIDFILE=${PIDFILE:="/var/run/jenkins/swarm-client.pid"}

# The location that the lock file should be written to
LOCKFILE=${LOCKFILE:="/var/lock/subsys/jenkins-swarm-client"}

# The location that the log file should be written to
LOGFILE=${LOGFILE:="/var/log/jenkins/swarm-client.log"}

# The location of the swarm-client jar file
JAR=${JAR:="/var/lib/jenkins/swarm-client.jar"}

# The arguments to pass to the JVM.  Most useful for specifying heap sizes.
JVM_ARGS=${JVM_ARGS:=""}

# The master Jenkins server to connect to
MASTER=${MASTER:="jenkins"}

# The port that the master is running Jenkins on
MASTER_PORT=${MASTER_PORT:="8080"}

# The username to use when connecting to the master
USERNAME=${USERNAME:=""}

# The password to use when connecting to the master
PASSWORD=${PASSWORD:=""}

# The name of this slave
NAME=${NAME:="$(hostname)"}

# The number of executors to run, by default one per core
NUM_EXECUTORS=${NUM_EXECUTORS:="$(/usr/bin/nproc)"}

# The labels to associate with each executor (space separated)
LABELS=${LABELS:=""}

# The return value from invoking the script
RETVAL=0

start() {
  echo -n $"Starting Jenkins Swarm Client... "

  # Must be in /var/lib/jenkins for the swarm client to run properly
  local cmd="cd /var/lib/jenkins;"

  cmd="${cmd} java ${JVM_ARGS} "
  cmd="${cmd} -jar '${JAR}'"
  cmd="${cmd} -master 'http://${MASTER}:${MASTER_PORT}'"

  if [[ -n "${USERNAME}" ]]; then
    cmd="${cmd} -username '${USERNAME}'"
  fi

  if [[ -n "${PASSWORD}" ]]; then
    cmd="${cmd} -password '${PASSWORD}'"
  fi

  cmd="${cmd} -name '${NAME}'"

  if [[ -n "${LABELS}" ]]; then
    cmd="${cmd} -labels '${LABELS}'"
  fi

  cmd="${cmd} -executors '${NUM_EXECUTORS}'"
  cmd="${cmd} >> '${LOGFILE}' 2>&1 </dev/null &"

  daemon --user "${USER}" --check "swarm-client" --pidfile "${PIDFILE}" "${cmd}"

  local pid=$(jps -l | grep "${JAR}" | awk '{print $1}')
  [ -n ${pid} ] && echo ${pid} > "${PIDFILE}"
  RETVAL=$?
  [ $RETVAL -eq 0 ] && touch $LOCKFILE

  echo
}

stop() {
  echo -n $"Stopping Jenkins Swarm Client... "
  killproc -p $PIDFILE "swarm-client"
  RETVAL=$?
  echo
  [ $RETVAL -eq 0 ] && rm -f $LOCKFILE
}

restart() {
  stop
  start
}

checkstatus() {
  status -p "$PIDFILE" "swarm-client"
  RETVAL=$?
}

condrestart() {
  [ -e "$LOCKFILE" ] && restart || :
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  status)
    checkstatus
    ;;
  restart)
    restart
    ;;
  condrestart)
    condrestart
    ;;
  *)
    echo $"Usage: $0 {start|stop|status|restart|condrestart}"
    exit 1
esac

exit $RETVAL