#!/bin/bash

## 此脚本用于执行完redex之后进行打包

## 根据本机项目位置设置
APP_PATH=/home/zsigui/AndroidStudioProjects/GiftCool
SCRIPT_PATH=$APP_PATH"/script"
TEST_INPUT_APK_PATH=$APP_PATH"/app/build/outputs/apk/app-dDebug-debug.apk" # this is a debug apk
INPUT_APK_PATH=$APP_PATH"/app/build/outputs/apk/app-rRelease-release.apk"
TEST_OUTPUT_APK_PATH=$APP_PATH"/apk/src/app-debug-redex.apk"
OUTPUT_APK_PATH=$APP_PATH"/apk/src/app-release-redex.apk"

PROGUARD_TXT=$APP_PATH"/app/build/intermediates/proguard-rules/rRelease/release/aapt_rules.txt"
MAPPING_TXT=$APP_PATH"/app/build/outputs/mapping/rRelease/release/mapping.txt"

TEST_MODE=1

REDEX_PATH=/home/zsigui/software/android/redex/

while getopts "d:" OPTION
do
    case ${OPTION} in
        d)
            TEST_MODE=$OPTARG
            ;;
        \?)
            echo "d: TEST_MODE"
            ;;
    esac
done

echo "start to work script!!!"
if [ $TEST_MODE -eq 1 ];
then
    echo "now is working in test mode"
    cd $REDEX_PATH
    $REDEX_PATH/redex -c $REDEX_PATH/config/gc.config $TEST_INPUT_APK_PATH -o $TEST_OUTPUT_APK_PATH
    cd $SCRIPT_PATH
    $SCRIPT_PATH/work.sh -s $TEST_OUTPUT_APK_PATH
else
    echo "now is working in release mode"
    cd $REDEX_PATH
    $REDEX_PATH/redex -c $REDEX_PATH/config/gc.config $INPUT_APK_PATH -P $PROGUARD_TXT -m $MAPPING_TXT -o $OUTPUT_APK_PATH
    cd $SCRIPT_PATH
    $SCRIPT_PATH/work.sh -s $OUTPUT_APK_PATH
fi
