#!/bin/bash

./ServerCMS_loop.sh &
MyPID=$!

echo $MyPID > PID