#!/bin/bash
for var in `ls .`
do
    if [ "$var" != "sign.sh" ] && [ "$var" != "app-release.encrypted.apk" ];
    then
        rm -rf $var
    fi
done
jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 keystore ../app/keystore/gift_cool.keystore -keypass ouwan_giftcool_key -storepass ouwan_giftcool_store -signedjar giftcool-unalign.apk app-release.encrypted.apk GiftCool
zipalign -v 4 giftcool-unalign.apk giftcool-v1.0.4.apk
