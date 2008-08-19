#!/bin/sh
#
# This shell script will cat files named tf-[command].log to the stdout.
# 
#
if [ $# = 0 ]; then
	echo 1>&2 Usage: tf [command] [ignored arguments]
	exit 1
fi

commandfile=`dirname $0`/tf-$1.log

case $1 in
	workfold)
#		eval last_arg=\$$#
		mkdir $4
		;;
	setcs)
		exit 0
		;;
esac

if [ -r  $commandfile ] 
then
	cat $commandfile
	exit 0
else
	echo Unknown command $1, $commandfile
	exit 1
fi
