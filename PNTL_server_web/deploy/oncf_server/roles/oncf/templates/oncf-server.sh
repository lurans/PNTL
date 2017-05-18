
#!/bin/sh

source /etc/profile
source ~/.bashrc

TOMCAT_BIN=${TOMCAT_HOME}/bin/startup.sh
PIDFILE={{ install_path }}/{{ service_name }}/{{ tool_name }}.pid

PYTHON=${BASEDIR}/bin/python
. functions

SERVICE=oncf
PROCESS=oncf

RETVAL=0

start() {
    echo -n $"Starting oncf daemon: "
    if [[ ! -z "$(pidofproc -p ${PIDFILE} $PROCESS)" ]]; then
       RETVAL=$?
       echo -n "already running"
    else
       daemon --check $SERVICE $TOMCAT_BIN 
    fi       
    RETVAL=$?
    echo
    return $RETVAL
}

stop() {
    echo -n $"Stopping oncf daemon: "
    killproc -p ${PIDFILE} $PROCESS
    RETVAL=$?
    echo
}

restart() {
   stop
   start
}

# See how we were called.
case "$1" in
    start|stop|restart)
        $1
        ;;
    status)
        
        status -p ${PIDFILE} $PROCESS
        RETVAL=$?
       
        ;;
    condrestart)
        [ -f $LOCKFILE ] && restart || :
        ;;
    reload)
        echo "can't reload configuration, you have to restart it"
        RETVAL=$?
        ;;
    *)
        echo $"Usage: $0 {start|stop|status|restart|condrestart|reload}"
        exit 1
        ;;
esac
exit $RETVAL

