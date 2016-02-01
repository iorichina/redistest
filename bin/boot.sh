#!/bin/bash

BOOTPATH=/data/webapps/thrift_test/redistest/
BOOTPID=${BOOTPATH}logs/boot.pid
BOOTLOG=${BOOTPATH}logs/boot.log

export LANG=zh_CN.UTF-8
export TZ="Asia/Shanghai"

JAVAHOME=/data/services/jdk1.6.0_27/
JAVAOPT='-Xmx256m'
JAVACP="${BOOTPATH}target/classes:${BOOTPATH}target/libs/*"

MAINCLASS='com.iorichina.base.ServiceBooter'
MAINARGS=""
WORKHOME=${BOOTPATH}

startBoot()
{
    $JAVAHOME/bin/java $JAVAOPT -cp $JAVACP  -Duser.dir="$WORKHOME" $MAINCLASS $MAINARGS >> $BOOTLOG 2>&1 &

    pid="$!";
    echo "$pid" > $BOOTPID
    echo "start ... ">>${BOOTLOG}
    echo "child pid is $pid">>${BOOTLOG}
    echo "status is $?">>${BOOTLOG}
}

if [ -f $BOOTPID ] then
    pid="`cat $BOOTPID`"
    if test `ps -p $pid | wc -l` -gt 1 then
        echo "progress $pid exists!"
        exit       
    fi
fi

startBoot

while [ 1 ] do
    wait $pid
    exitstatus="$?"
    echo "**************************" >>${BOOTLOG}
    echo "child pid=$pid is gone, $exitstatus" >>${BOOTLOG}
    echo `date` >> ${BOOTLOG}
    echo "**************************" >>${BOOTLOG}

    sleep 5

    startBoot
done