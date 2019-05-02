#!/usr/bin/env bash

kill $(ps ax |grep 'vault server' |awk '{print $1}') 2>/dev/null