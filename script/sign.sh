#!/bin/bash
CWD=$(dirname $(readlink -f $0))
KEYSTORE=${CWD}/../app/keystore/gift_cool.keystore
KEYPASS=ouwan_giftcool_key
STOREPASS=ouwan_giftcool_store
KEYSTORENAME=giftcool
SOURCEDIR=${CWD}/../apk/temp-unsigned
OUTPUTDIR=${CWD}/../apk/output
UNALIGNAPK=temp-unalign.apk

while getopts "keystore:keypass:storepass:keyname:srcdir:outputdir:" OPTION
do
    case ${OPTION} in
        keystore)
            KEYSTORE=${OPTARG}
            ;;
        keypass)
            KEYPASS=${OPTARG}
            ;;
        storepass)
            STOREPASS=${OPTARG}
            ;;
        keyname)
            KEYSTORENAME=${OPTARG}
            ;;
        srcdir)
            SOURCEDIR=${OPTARG}
            ;;
        outputdir)
            OUTPUTDIR=${OPTARG}
            ;;
        \?)
            echo "use variable of :keystore:keypass:storepass:keyname:srcdir:outputdir:"
            ;;
    esac
done


[ ! -d ${SOURCEDIR} ] && { echo "path argument is not a available directory!" ; exit 1; }
[ -z ${OUTPUTDIR} ] && OUTPUTDIR=${CWD}/../apk/output
[ ! -d ${OUTPUTDIR} ] && mkdir -p ${OUTPUTDIR}

for APKFILE in `ls ${SOURCEDIR} | grep '.*apk$'`
do
    echo "keystore = " + ${KEYSTORE}
    SRCAPK=${SOURCEDIR}/${APKFILE}
    UNALIGNAPK=${SOURCEDIR}/temp_${APKFILE}
    OUTPUTNAME=${OUTPUTDIR}/${APKFILE}
    jarsigner -verbose -sigalg md5withrsa -digestalg sha1 -keystore ${KEYSTORE} -keypass ${KEYPASS} -storepass ${STOREPASS} -signedjar ${UNALIGNAPK} ${SRCAPK} ${KEYSTORENAME}
    zipalign -v 4 ${UNALIGNAPK} ${OUTPUTNAME}
    rm -d ${UNALIGNAPK}
done
echo "done sign and zipalign"
