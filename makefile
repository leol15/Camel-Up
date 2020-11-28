

CP = -cp "./lib/*"
SP = -sourcepath .
RUNPATH = -cp "./lib/*:./lib/jetty/*:./server/"

default:
	echo build, run, both

build:
	javac $(CP) ./server/Server.java

run:
	java $(RUNPATH) Server

both:	build	run

clean:
	rm server/*.class

