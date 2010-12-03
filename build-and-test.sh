#!/bin/bash

INPUT_COMMAND=$1
AGENT_JAR=perf-agent-shaded/target/perf-agent-shaded-1.0.0-SNAPSHOT-shaded.jar
PRG_JARS=perf-agent-tests/target/perf-agent-tests-1.0.0-SNAPSHOT-shaded.jar
PRG_CLASS="org.tarpoon.performance.agent.FakeService"

function echo_line_sep {
	echo "######################################"
}
function echo_message {
	echo "## $1"
}
function echo_head {
	echo_line_sep
	echo_message "$1"
	echo_line_sep
}

function build_jars {
	echo_head "Building project"
	mvn clean package
}

function test_program {
	echo_head "Launching tests"
	java -javaagent:${AGENT_JAR} -cp ${PRG_JARS} ${PRG_CLASS}
}

case "$INPUT_COMMAND" in 
	build)
		build_jars
		;;
	test) 
		test_program
		;;
	*)
		build_jars
		test_program
		;;
esac

exit

if [ "$INPUT_COMMAND" -eq "build" ]; then
	build()
elif [ "$INPUT_COMMAND" -eq "test" ]; then
	test()
else
	build()
	test()
fi
	


