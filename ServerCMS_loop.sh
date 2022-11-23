#!/bin/bash

ulimit -n 65535

while :;
do
	java8 -jar cms-0.0.2.jar -Dfile.encoding=UTF-8 -Xmx1G > log/stdout.log 2>&1

	[ $? -ne 2 ] && break
	sleep 30;
done

