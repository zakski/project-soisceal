#!/bin/bash

#author: Alberto Sita

#simulator for iPhone 5/5C/4S
IPHONE_SIMUL_32=x86

#simulator for iPhone 5S/SE/6/6Plus/6S/6SPlus/7/7Plus
#IPHONE_SIMUL_64=x86_64

#deploy to iPhone 5/5C/4S
IPHONE_32=thumbv7

#deploy to iPhone 5S/SE/6/6Plus/6S/6SPlus/7/7Plus
IPHONE_64=arm64

./RoboVM_SDK/bin/robovm -config ./RoboVM_SDK/robovm-config/robovm.xml -arch $IPHONE_SIMUL_32 -cp ./RoboVM_SDK/lib/robovm-objc.jar:./RoboVM_SDK/lib/robovm-cocoatouch.jar:./bin/: -verbose -run
