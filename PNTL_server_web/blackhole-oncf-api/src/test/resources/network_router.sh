#!/usr/bin/expect
set timeout 10
set arg0 [lindex $argv 0]
set arg1 [lindex $argv 1]
set arg2 [lindex $argv 2]
set arg3 [lindex $argv 3]
set arg4 [lindex $argv 4]
set arg5 [lindex $argv 5]
set arg6 [lindex $argv 6]
set arg7 [lindex $argv 7]
set arg8 [lindex $argv 8]
set arg9 [lindex $argv 9]
set arg10 [lindex $argv 10]
set arg11 [lindex $argv 11]
spawn su root
#spawn ssh -l root xxx.xxx.xxx.xxx
#expect "*connecting*"
#send "yes\r"
expect "*Password:*"
send "$arg11\r"
send "python /home/GalaX8800/network_router.py $arg0 $arg1 $arg2 $arg3 $arg4 $arg5 $arg6 $arg7 $arg8 $arg9 $arg10\r"
interact