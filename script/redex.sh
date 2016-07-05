#!/bin/bash

## 此脚本用于执行完redex之后进行打包

## 根据本机项目位置设置
APP_PATH=/home/zsigui/AndroidStudioProjects/GiftCool
SCRIPT_PATH=${APP_PATH}"/script"
REDEX_PATH=/home/zsigui/software/android/redex
REDEX_CONFIG=${REDEX_PATH}"/config/gc.config"

INPUT_APK_PATH=
OUTPUT_APK_PATH=
# dDebug下不执行
PROGUARD_TXT=
MAPPING_TXT=
## mode 1 : dDebug 2 : rDebug 3 : rRelease
PACK_MODE=
## define whether execute redex compile first
## 1 : need 0 : not need
NEED_REDEX=

setDefaultVar()
{
    # default arguments for channel 0
    [ -z ${PACK_MODE} ] && PACK_MODE=1
    [ -z ${NEED_REDEX} ] && NEED_REDEX=0
    [ -z ${PROGUARD_TXT} ] && PROGUARD_TXT=$APP_PATH"/app/build/intermediates/proguard-rules/rRelease/release/aapt_rules.txt"
    [ -z ${MAPPING_TXT} ] && MAPPING_TXT=$APP_PATH"/app/build/outputs/mapping/rRelease/release/mapping.txt"

    if [ ${PACK_MODE} -eq 1 ]; then
        [ -z ${INPUT_APK_PATH} ] && INPUT_APK_PATH=$APP_PATH"/app/build/outputs/apk/app-dDebug-debug.apk"
        [ -z ${OUTPUT_APK_PATH} ] && OUTPUT_APK_PATH=$APP_PATH"/apk/src/app-dDebug.encrypt.apk"
    elif [ ${PACK_MODE} -eq 2 ]; then
        [ -z ${INPUT_APK_PATH} ] && INPUT_APK_PATH=$APP_PATH"/app/build/outputs/apk/app-rDebug-release.apk"
        [ -z ${OUTPUT_APK_PATH} ] && OUTPUT_APK_PATH=$APP_PATH"/apk/src/app-rDebug.encrypt.apk"
    elif [ ${PACK_MODE} -eq 3 ]; then
        [ -z ${INPUT_APK_PATH} ] && INPUT_APK_PATH=$APP_PATH"/app/build/outputs/apk/app-rRelease-release.apk"
        [ -z ${OUTPUT_APK_PATH} ] && OUTPUT_APK_PATH=$APP_PATH"/apk/src/app-rRelease.encrypt.apk"
    fi
}

while getopts "d:n:i:o:p:m" OPTION
do
    case ${OPTION} in
        d)
            PACK_MODE=$OPTARG
            ;;
        n)
            NEED_REDEX=$OPTARG
            ;;
        i)
            INPUT_APK_PATH=$OPTARG
            ;;
        o)
            OUTPUT_APK_PATH=$OPTARG
            ;;
        p)
            PROGUARD_TXT=$OPTARG
            ;;
        m)
            MAPPING_TXT=$OPTARG
            ;;
        \?)
            echo "-d (PACK_MODE: 1 dDebug 2 rDebug 3 rRelease ; DEFAULT is 1)"
            echo "-n (NEED_REDEX: 1 yes 0 no ; DEFAULT is 0)"
            echo "-i (INPUT_APK_PATH)"
            echo "-o (OUTPUT_APK_PATH)"
            echo "-p (PROGUARD_TXT: work when PACK_MODE not in mode 1)"
            echo "-m (MAPPING_TXT: work when PACK_MODE not in mode 1)"
            ;;
    esac
done

echo "start to work script!!!"

setDefaultVar

echo ${INPUT_APK_PATH},${OUTPUT_APK_PATH}

echo "now pack_mode is "${PACK_MODE}" and need_redex is "${NEED_REDEX}
if [ ${PACK_MODE} -eq 1 ]; then
	if [ ${NEED_REDEX} -eq 1 ]; then
		cd ${REDEX_PATH} && pwd
        ${REDEX_PATH}/redex -c ${REDEX_CONFIG}  ${INPUT_APK_PATH} -o ${OUTPUT_APK_PATH}
        echo "${REDEX_PATH}/redex -c ${REDEX_CONFIG}  ${INPUT_APK_PATH} -o ${OUTPUT_APK_PATH}"
	fi
	cd ${SCRIPT_PATH} && pwd
    ${SCRIPT_PATH}/work.sh -s ${OUTPUT_APK_PATH}
else
	if [ ${NEED_REDEX} -eq 1 ]; then
		cd ${REDEX_PATH} && pwd
        ${REDEX_PATH}/redex -c ${REDEX_CONFIG} ${INPUT_APK_PATH} -P ${PROGUARD_TXT} -m ${MAPPING_TXT} -o ${OUTPUT_APK_PATH}
        echo "${REDEX_PATH}/redex -c ${REDEX_CONFIG} ${INPUT_APK_PATH} -P ${PROGUARD_TXT} -m ${MAPPING_TXT} -o ${OUTPUT_APK_PATH}"
	fi
	cd ${SCRIPT_PATH} && pwd
    ${SCRIPT_PATH}/work.sh -s ${OUTPUT_APK_PATH}
fi
