#!/bin/bash

printUsage()
{
    echo "usage: ./work.sh [-c][-v][-s]"
    echo "c: channel names, use as ',' to split, such '10000, 10004'"
    echo "v: the version name of apk"
    echo "s: the path of source apk"
}

# get current shell absolute dir
CWD=$(dirname $(readlink -f $0))

# 打包配置参数
NAME=giftcool
VERSION=1.2.1
# 150000 is for weixin hongbao
CHNNAMES=0,1972000
TEMPOUTPUT=${CWD}/../apk/temp-unsigned
SOURCEAPK=${CWD}/../apk/src/app-release.encrypted.apk
OUTPUT=${CWD}/../apk/output
EXTENSION=.giftcool

# 签名参数
KEYSTORE=${CWD}/../app/keystore/gift_cool.keystore
KEYPASS=ouwan_giftcool_key
STOREPASS=ouwan_giftcool_store
KEYSTORENAME=giftcool


while getopts "c:v:s:" OPTION
do
    case ${OPTION} in
        c)
            CHNNAMES=$OPTARG
            ;;
        v)
            VERSION=$OPTARG
            ;;
        s)
            SOURCEAPK=$OPTARG
            ;;
        \?)
            printUsage
            ;;
    esac
done

TIME=$(($(date +%s%N)/1000000))
echo $(pwd)
[ -d ${TEMPOUTPUT} ] && rm -r ${TEMPOUTPUT}
#${CWD}/../gradlew clean assembleRelease -x lint --parallel
#ant -f ${CWD}/../build.xml clean && ant -f ${CWD}/../build.xml
${CWD}/packchn.sh -s ${SOURCEAPK} -o ${TEMPOUTPUT} -v ${VERSION} -c ${CHNNAMES} -n ${NAME} -e ${EXTENSION}
${CWD}/sign.sh -i ${TEMPOUTPUT} -o ${OUTPUT} -f ${KEYSTORE} -k ${KEYPASS} -s ${STOREPASS} -n ${KEYSTORENAME} -v ${VERSION}

[ -d ${TEMPOUTPUT} ] && rm -r ${TEMPOUTPUT}

TIME=$(($(date +%s%N)/1000000 - TIME))
echo "done all, elapsed: ${TIME} ms"
