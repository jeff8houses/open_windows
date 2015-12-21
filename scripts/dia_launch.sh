#!/bin/sh

DISPLAY=:0
export DISPLAY

fcserverCount=$( pgrep -c fcserver )
javaCount=$( pgrep -c java )
now=$(date +"%H")

echo $now
echo $fcserverCount "fcserver"
echo $javaCount "java"


if [ "$now" -ge "7" -a "$now" -le "22" ] && [ "$fcserverCount" -ne "2" -o "$javaCount" -ne "2" ]
	then
	echo "rebooted"
	killall fcserver
	killall java
	sleep 1
	#launch fcsever
	/home/jeff/Desktop/fcserver/fcserver /home/jeff/Desktop/fcserver/A.json &
	sleep 1
	/home/jeff/Desktop/fcserver/fcserver /home/jeff/Desktop/fcserver/B.json &
	sleep 1
	#launch sketches
	/home/jeff/Desktop/dia/masterA/application.linux64/masterA &
	sleep 1
	/home/jeff/Desktop/dia/masterB/application.linux64/masterB
elif [ "$now" = "23" ]
	then
	#echo "phase 2!"
	killall fcserver
	killall java
	#launch fcsever
	/home/jeff/Desktop/fcserver/fcserver /home/jeff/Desktop/fcserver/A.json &
	/home/jeff/Desktop/fcserver/fcserver /home/jeff/Desktop/fcserver/B.json &
	#launch blackout sketches or clear fadecandy
	/home/jeff/Desktop/dia/blackoutA/application.linux64/blackoutA &
	/home/jeff/Desktop/dia/blackoutB/application.linux64/blackoutB &
	#kill everthing
	killall fcserver
	killall java
	echo "all dark!"
elif  [ "$now" -lt 7 ]
	then
	killall fcserver
	killall java
	echo "all is quiet"
else
	echo "all good!"
fi
