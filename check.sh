#!/bin/sh

ps -ef | egrep -e '(db_net|drep)' | grep -v "grep"

