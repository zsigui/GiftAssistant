#!/bin/bash
rm -f giftcool-unalign.apk
rm -f giftcool-v1.0.2.apk
jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 keystore ../app/keystore/gift_cool.keystore -keypass ouwan_giftcool_key -storepass ouwan_giftcool_store -signedjar giftcool-unalign.apk $1 GiftCool
zipalign -v 4 giftcool-unalign.apk giftcool-v1.0.3.apk
