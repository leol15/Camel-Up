default:
	echo build, run, both

build:
	javac -cp "./lib/*" -sourcepath . ./server/Server.java

run:
	java -cp "./lib/*:./lib/jetty/*:./server/" Server

both:	build	run
