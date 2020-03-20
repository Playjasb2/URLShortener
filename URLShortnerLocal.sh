#!/bin/bash
javac URLShortner.java
java URLShortner $1 &
echo "Server$1:$!" >> out/pids.pid;
