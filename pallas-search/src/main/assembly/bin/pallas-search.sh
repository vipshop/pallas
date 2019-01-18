#!/bin/bash

ulimit -s 20280
ulimit -c unlimited
ulimit -n 20480

export PATH=$PATH:/usr/sbin

#PRG="$0"i
PRG=$(readlink -f $0)
PRGDIR=`dirname "$PRG"`
BASEDIR=`cd "$PRGDIR/.." >/dev/null; pwd`
PARENTDIR=`cd "$BASEDIR/.." >/dev/null; pwd`

LOGDIR=""
OUTFILE=""
UPSTREAM="127.0.0.1:9200"
LISTEN_PORT="9201"
JMX_PORT="8060"
JMX_IP="127.0.0.1"
START_TIME=60
HANDLE_TIME=120
#get pallas-search from /apps/dat/web/working/pallas-search.api.vip.com
CLUSTER=${BASEDIR##*/}

STATUS_FILE=${PRGDIR}/status
PID_FILE=${PRGDIR}/PID
HANDLE_FILE=${PRGDIR}/handle

RUN_MODE="background"

## 20160307 check java Version (by yejun)
function funCHK_JAVA() {
	JAVA_EXEC="/apps/svr/jdk/bin/java"
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
	echo -e "\n      '-env|--environment': optional."
	echo -e "\n      '-r|--runmode': optional, default value is $RUN_MODE, you can set it foreground"	
	echo -e "\n      '-c|--cluster': optional, default value is $CLUSTER"	
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
		-r| --runmode) RUN_MODE="$2"; shift 2;;
		-j|--jmx-port) JMX_PORT="$2" ; shift 2 ;;
		-i|--jmx-ip) JMX_IP="$2" ; shift 2 ;;
		-c|--cluster) CLUSTER="$2" ; shift 2 ;;
		-env|--environment) RUN_ENVIRONMENT="$2" ; shift 2 ;;
		*) break;;
	esac
done

ADDITIONAL_OPTS=$*;

PERM_SIZE="256m"
MAX_PERM_SIZE="512m"

if [[ "$RUN_ENVIRONMENT" = "dev" ]]; then
  ENVIRONMENT_MEM="-Xms512m -Xmx512m -Xss256K"
  PERM_SIZE="128m"
  MAX_PERM_SIZE="256m"
elif [[ "$RUN_ENVIRONMENT" = "docker" ]]; then
  ENVIRONMENT_MEM="-Xms512m -Xmx512m -Xss256K"
  PERM_SIZE="128m"
  MAX_PERM_SIZE="256m"
else
  ENVIRONMENT_MEM="-Xms4096m -Xmx4096m"
fi

ADDITIONAL_OPTS="$JAVA_OPTS $ADDITIONAL_OPTS"
	
LOGDIR=/apps/logs/pallas/search
OUTFILE=$LOGDIR/pallas-search-output.log
GC_LOG_FILE=/dev/shm/gc-pallas-search.log

JAVA_OPTS=" -Dpallas.log.folder=${LOGDIR} -XX:+PrintCommandLineFlags -XX:-OmitStackTraceInFastThrow -XX:-UseBiasedLocking -XX:AutoBoxCacheMax=20000 -Dio.netty.recycler.maxCapacity.default=0 -Dio.netty.leakDetectionLevel=disabled -XX:AutoBoxCacheMax=20000"
MEM_OPTS="-server ${ENVIRONMENT_MEM} -XX:NewRatio=1 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch -XX:MaxTenuringThreshold=6 -XX:+ExplicitGCInvokesConcurrent -XX:ReservedCodeCacheSize=128M -XX:+AlwaysPreTouch -XX:+PerfDisableSharedMem" 
GCLOG_OPTS="-Xloggc:${GC_LOG_FILE}  -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCDateStamps -XX:+PrintGCDetails"
CRASH_OPTS="-XX:ErrorFile=${LOGDIR}/hs_err_%p.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGDIR}/"
JMX_OPTS="-Dcom.sun.management.jmxremote.port=${JMX_PORT} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dsun.rmi.transport.tcp.threadKeepAliveTime=75000 -Djava.rmi.server.hostname=${JMX_IP}"
SETTING_CONF="-Dstart.check.outfile=${STATUS_FILE} -Dpallas.search.port=${LISTEN_PORT} -Dpallas.search.upstream=${UPSTREAM} -Dpallas.search.cluster=${CLUSTER} "

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
BACKUP_GC_LOG()
{
 GCLOG_DIR=${LOGDIR}
 BACKUP_FILE="${GCLOG_DIR}/gc-pallas-search_$(date +'%Y%m%d_%H%M%S').log"
 
 if [ -f ${GC_LOG_FILE} ]; then
  echo "saving gc log ${GC_LOG_FILE} to ${BACKUP_FILE}"
  mv ${GC_LOG_FILE} ${BACKUP_FILE}
  
  if [[ $? != 0 ]]; then
    echo -e "\033[31mmove gc log ${GC_LOG_FILE} to ${BACKUP_FILE} failed! Exit.\033[0m"
    exit -1
  fi
 fi
}

GET_PID()
{
  echo `lsof -n -P -i :${LISTEN_PORT},${JMX_PORT} | grep LISTEN | awk '{print $2}' | head -n 1`
}

START()
{	
	echo "Log redirects to ${LOGDIR}"
	CHECK_JMX

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
		 echo "Pallas search is running, please stop it first!!"
		 exit -1
		fi
	fi
	
	echo "" > ${STATUS_FILE}
	RUN_PARAMS="port ${LISTEN_PORT} jmxport ${JMX_PORT}"
    
    nohup java  $JAVA_OPTS $MEM_OPTS $JMX_OPTS $GCLOG_OPTS $CRASH_OPTS $SETTING_CONF $ADDITIONAL_OPTS -jar ${BASEDIR}/pallas-search.jar  >> $OUTFILE 2>&1 &
	PID=$!
	echo $PID > $PID_FILE
  
	sleep 3

	CHECK_STATUS=`cat ${STATUS_FILE}`
	starttime=0
	while  [ x"$CHECK_STATUS" == x ]; do
	if [[ "$starttime" -lt ${START_TIME} ]]; then
	  sleep 1
	  ((starttime++))
	  echo -e ".\c"
	  CHECK_STATUS=`cat ${STATUS_FILE}`
	  echo "check: ${CHECK_STATUS}"
	else
	  echo -e "\nPallas search start may fails, checking not finished until reach the starting timeout! See ${OUTFILE} for more information."
	  exit -1
	fi
	done

	if [ $CHECK_STATUS = "SUCCESS" ]; then
		echo -e "\nPallas search start successfully, running as process:$PID."
		echo ${RUN_PARAMS} > ${STATUS_FILE}
	fi

	if [ $CHECK_STATUS = "ERROR" ]; then
		kill -9 $PID
		echo -e "\nPallas search start failed ! See ${OUTFILE} for more information."
		exit -1
	fi
	
	if [[ "$RUN_MODE" = "foreground" ]]; then
		trap STOP SIGTERM
		wait $PID
	fi
}

STOP()
{	
	if [ -f $PID_FILE ] ; then
		PID=`cat $PID_FILE`
	else
		PID=$(GET_PID)
	fi

	stoptime=0
    if [ "$PID" != "" ]; then
		if [ -d /proc/$PID ];then
			RUN_PARAMS=`cat ${STATUS_FILE}`
			echo "Pallas search is stopping,pid is ${PID}, params are : ${RUN_PARAMS}."	
			while [ -d /proc/$PID ]; do
				if	[[ "$stoptime" -lt 300 ]];	then
					kill $PID
					sleep 1
					((stoptime++))
					echo -e ".\c"
				else
					echo -e "\nstop failed after 300 seconds. now kill -9 ${PID}"
					kill -9 $PID
				fi
			done
			echo -e "\nKill the process successfully."
			BACKUP_GC_LOG
		else
			echo "Pallas search is not running."
		fi
	else
		echo -e "\nPallas search is not running."
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
	  echo "Pallas search running ,params are : ${RUN_PARAMS}."
	  exit 0
	fi
  fi
  echo "Pallas search is not running."
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
