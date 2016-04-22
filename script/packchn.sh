#!/bin/bash

SOURCE=
OUTPUTDIR=
CHNNAMES=
NAME=
VERSION=
EXTENSION=
METADIR=

printUsage()
{
    echo "USE DEFAULT VARIABLE"
#    exit 1
}

setDefaultVar()
{
    # default arguments for channel 0
    [ -z ${VERSION} ] && VERSION=1.1.5
    [ -z ${NAME} ] && NAME=giftcool
    [ -z ${CHNNAMES} ] && CHNNAMES=10000
    # get current shell absolute dir
    CWD=$(dirname $(readlink -f $0))
    [ -z ${SOURCE} ] && SOURCE=${CWD}/../apk/src/app-release.apk
    [ -z ${OUTPUTDIR} ] && OUTPUTDIR=${CWD}/../apk/temp-unsigned
    [ -z ${EXTENSION} ] && EXTENSION=.${NAME}
    [ -z ${METADIR} ] && METADIR=META-INF
}

while getopts "s:o:c:n:v:e:" OPTION
do
    case ${OPTION} in
            s)
            SOURCE=$OPTARG
            ;;
            o)
            OUTPUTDIR=$OPTARG
            ;;
            c)
            CHNNAMES=$OPTARG
            ;;
            n)
            NAME=$OPTARG
            ;;
            v)
            VERSION=$OPTARG
            ;;
            e)
            EXTENSION=$OPTARG
            ;;
            \?)
            printUsage
            ;;
    esac
done

setDefaultVar

#${CWD}/../gradlew clean assembleRelease -x lint --parallel

mkdir -p ${METADIR}
mkdir -p ${OUTPUTDIR}

# spilt $CHNNAMES to array
IFS=',' read -ra CHNARRAY <<< ${CHNNAMES}
DATE=`date +%Y%m%d%H%m`
for i in ${CHNARRAY[@]}
do
    DESTAPK=${OUTPUTDIR}/${NAME}_v${VERSION}_${i}_${DATE}.apk
    cp ${SOURCE} ${DESTAPK}
    if [ "${i}" == "0" ]
    then
        # 0 渠道包表示默认渠道号,直接复制即可
        echo "pack 0 : do nothing"
    else
        CHNFILE=${METADIR}/$(echo ${i} | base64)${EXTENSION}
        touch ${CHNFILE}
        echo Compressed ${CHNFILE} to ${DESTAPK}
        zip -q -m ${DESTAPK} ${CHNFILE}
    fi
done
rm -r ${METADIR}

echo "done"
