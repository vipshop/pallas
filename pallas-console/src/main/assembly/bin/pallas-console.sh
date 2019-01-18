#!/bin/bash
JAVA_HOME="/apps/svr/jdk"
export PATH=$PATH:/usr/sbin:$JAVA_HOME/bin

PRG=$(readlink -f $0)
PRGDIR=`dirname "$PRG"`
BASEDIR=`cd "$PRGDIR/.." >/dev/null; pwd`
PARENTDIR=`cd "$BASEDIR/.." >/dev/null; pwd`

LOGDIR=""
OUTFILE=""

LISTEN_PORT="8080"
JMX_PORT="8061"
JMX_IP="127.0.0.1"
START_TIME=60
HANDLE_TIME=120

STATUS_FILE=${PRGDIR}/status
PID_FILE=${PRGDIR}/PID

RUN_MODE="background"

## 20160307 check java Version
function funCHK_JAVA() {
	JAVA_EXEC="$JAVA_HOME/bin/java"
	JAVA_VERSION=$(java -version |grep 'java version "1.6'|wc -l)
	if [ $JAVA_VERSION == "1" ]; then
		echo `date` ERROR!!!!!!!!!!!!!!!!!   java must be 1.7 or above , executor exit|tee -a $OUTFILE
		exit
	fi
	if [ -x /apps/sh/web_env.sh ]; then
		echo -e "source /apps/sh/web_env.sh"
		source /apps/sh/web_env.sh
	fi
	if [ -x /apps/sh/app_env.sh ]; then
		echo -e "source /apps/sh/app_env.sh"
		source /apps/sh/app_env.sh
	fi
}

funCHK_JAVA

USAGE()
{
	echo "Usage: $0 start|stop|restart|status [-p|-i|-j|-env|-r|-c]"
	echo -e "\n      '-p|--port': optional, default value is ${LISTEN_PORT}."
	echo -e "\n      '-i|--jmx-ip': optional, default value is ${JMX_IP}."
	echo -e "\n      '-j|--jmx-port': optional, default value is ${JMX_PORT}."
	echo -e "\n      JVM args: optional."
}

if [ $# -lt 1 ]; then
	USAGE
	exit -1
fi

CMD="$1"
shift

while true; do
	case "$1" in
		-p| --port) LISTEN_PORT="$2"; shift 2;;
		-j|--jmx-port) JMX_PORT="$2" ; shift 2 ;;
		-i|--jmx-ip) JMX_IP="$2" ; shift 2 ;;
		*) break;;
	esac
done

ADDITIONAL_OPTS=$*;

MEM_OPTS="-Xms4g -Xmx4g"
PERM_SIZE="256m"
MAX_PERM_SIZE="512m"

ADDITIONAL_OPTS="$JAVA_OPTS $ADDITIONAL_OPTS"

LOGDIR=/apps/logs/log_receiver/${DEPLOY_DOMAIN_NAME:='pallas-console'}
OUTFILE=$LOGDIR/pallas-console-out.log

GC_LOG_PATH=${GCLOG_PATH:='/dev/shm'}
GC_LOG_FILE=${GC_LOG_PATH}/pallas-console-gc.log
BACKUP_GC_LOG()
{
 GCLOG_DIR=${LOGDIR}
 BACKUP_FILE="${GCLOG_DIR}/gc-${DEPLOY_DOMAIN_NAME:='pallas-console'}-${LISTEN_PORT}_$(date +'%Y%m%d_%H%M%S').log"
 if [ -f ${GC_LOG_FILE} ]; then
  echo "saving gc log ${GC_LOG_FILE} to ${BACKUP_FILE}"
  mv ${GC_LOG_FILE} ${BACKUP_FILE}
 fi
}

JAVA_OPTS=" -XX:+PrintCommandLineFlags -XX:-OmitStackTraceInFastThrow -XX:-UseBiasedLocking -XX:AutoBoxCacheMax=20000"
#JAVA_OPTS="$JAVA_OPTS -javaagent:${ASPECTJ_PATH} -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
MEM_OPTS="-server -XX:NewRatio=1 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch -XX:MaxTenuringThreshold=6 -XX:+ExplicitGCInvokesConcurrent -XX:ReservedCodeCacheSize=128M -XX:+AlwaysPreTouch -XX:+PerfDisableSharedMem"
GCLOG_OPTS="-Xloggc:${GC_LOG_FILE}  -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:ParallelGCThreads=4 -XX:ConcGCThreads=1"
CRASH_OPTS="-XX:ErrorFile=${LOGDIR}/hs_err_%p.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGDIR}/"
JMX_OPTS="-Dcom.sun.management.jmxremote.port=${JMX_PORT} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dsun.rmi.transport.tcp.threadKeepAliveTime=75000 -Djava.rmi.server.hostname=${JMX_IP}"
JVM_ADD="$JVM_ADD -Dserver.port=${LISTEN_PORT}"

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')

echo -e "The java version is $JAVA_VERSION"


if [[ "$JAVA_VERSION" < "1.8" ]]; then
  MEM_OPTS="$MEM_OPTS -XX:PermSize=${PERM_SIZE} -XX:MaxPermSize=${MAX_PERM_SIZE} -Djava.security.egd=file:/dev/./urandom"
else
  MEM_OPTS="$MEM_OPTS -XX:MetaspaceSize=${PERM_SIZE} -XX:MaxMetaspaceSize=${MAX_PERM_SIZE} "
fi

CHECK_JMX()
{
	if [ x"$JMX_PORT" == x ]; then
		JMX_OPTS=""
	else
		TMP=$(echo `lsof -P -i :${JMX_PORT} | grep LISTEN | awk '{print $2}'`)
		if [ x"$TMP" != x ]; then
			echo "The jmx port is used, please use other port."
			exit -1
		fi
		echo "The jmx port is ${JMX_PORT}."
	fi
}


CHECK_LISTEN_PORT()
{
    TMP=$(echo `lsof -P -i :${LISTEN_PORT} | grep LISTEN | awk '{print $2}'`)
    if [ x"$TMP" != x ]; then
        echo "The listen port is used, please use other port."
        exit -1
    fi
    echo "The listen port is ${LISTEN_PORT}."
}


CHECK_LISTEN_PORT_AFTER_START()
{
    TMP=$(echo `lsof -P -i :${LISTEN_PORT} | grep LISTEN | awk '{print $2}'`)
    if [ x"$TMP" != x ]; then
        echo "SUCCESS" > ${STATUS_FILE}
    fi
}

START()
{
	BACKUP_GC_LOG
	echo "Log redirects to ${LOGDIR}"
	CHECK_JMX
	CHECK_LISTEN_PORT

	if [ ! -d $LOGDIR ]; then
		echo -e "\nWarning, the log directory of $LOGDIR is not existed, try to create it."
		mkdir -p $LOGDIR
		if [ -d $LOGDIR ]; then
			echo -e "\nCreate log directory successfully."
		else
			echo -e "\nCreate log directory failed."
			exit -1
		fi
    fi

	if [ -f $PID_FILE ] ; then
		PID=`cat $PID_FILE`
	fi

	if [ "$PID" != "" ]; then
		if [ -d /proc/$PID ];then
		 echo "pallas console is running, please stop it first!!"
		 exit -1
		fi
	fi

	echo "" > ${STATUS_FILE}
	RUN_PARAMS="port ${LISTEN_PORT} jmxport ${JMX_PORT}"

    nohup java  $JAVA_OPTS $MEM_OPTS $JMX_OPTS $GCLOG_OPTS $CRASH_OPTS $SETTING_CONF $ADDITIONAL_OPTS ${JVM_ADD} -jar ${BASEDIR}/pallas-console.jar  >> $OUTFILE 2>&1 &
	PID=$!
	echo $PID > $PID_FILE

	starttime=0
	while  [ x"$CHECK_STATUS" == x ]; do
	if [[ "$starttime" -lt ${START_TIME} ]]; then
	  sleep 1
	  ((starttime++))
	  echo -e ".\c"
	  CHECK_STATUS=`CHECK_LISTEN_PORT_AFTER_START; cat ${STATUS_FILE}`
	else
	  echo -e "\npallas console start may fails, checking not finished until reach the starting timeout! See ${OUTFILE} for more information."
	  exit -1
	fi
	done

	if [ $CHECK_STATUS = "SUCCESS" ]; then
		echo -e "\npallas console start successfully, running as process:$PID."
		echo ${RUN_PARAMS} > ${STATUS_FILE}
	fi

	if [ $CHECK_STATUS = "ERROR" ]; then
		kill -9 $PID
		echo -e "\npallas console start failed ! See ${OUTFILE} for more information."
		exit -1
	fi

}

STOP()
{
	BACKUP_GC_LOG
	if [ -f $PID_FILE ] ; then
		PID=`cat $PID_FILE`
	fi

	stoptime=0
    if [ "$PID" != "" ]; then
        mypid=`ps -ef|grep -v grep|grep 'pallas'|grep 'console'|grep $PID|sed -n '1P'|awk '{print $2}'`
		if [ "$PID"x == "$mypid"x ];then
			RUN_PARAMS=`cat ${STATUS_FILE}`
			echo "pallas console is stopping,pid is ${PID}, params are : ${RUN_PARAMS}."
			while [ "$PID"x == "$mypid"x ]; do
			    mypid=`ps -ef|grep -v grep|grep 'pallas'|grep 'console'|grep $PID|sed -n '1P'|awk '{print $2}'`
				if	[[ "$stoptime" -lt 300 ]];	then
				    if [ "$PID"x == "$mypid"x ];then
					    kill $PID
					    sleep 1
					    ((stoptime++))
					    echo -e ".\c"
					fi
				else
					echo -e "\nstop failed after 300 seconds. now kill -9 ${PID}"
					kill -9 $PID
				fi
			done
			echo -e "\nKill the process successfully."
		else
			echo "pallas console is not running."
		fi
	else
		echo -e "\npallas console is not running."
	fi
}

STATUS()
{
  if [ -f $PID_FILE ] ; then
	PID=`cat $PID_FILE`
  fi
  if [ "$PID" != "" ]
	then
	if [ -d /proc/$PID ];then
	  RUN_PARAMS=`cat ${STATUS_FILE}`
	  echo "pallas console running ,params are : ${RUN_PARAMS}."
	  exit 0
	fi
  fi
  echo "pallas console is not running."
}
RESTART()
{
	STOP
	START
}


case "$CMD" in
  start) START;;
  stop) STOP;;
  restart) RESTART;;
  status) STATUS;;
  help) USAGE;;
  *) USAGE;;
esac
