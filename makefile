

CP = -cp "./lib/*"
SP = -sourcepath ".:./game"
RUNPATH = -cp "./lib/*:./lib/jetty/*:./server/:./game"

default:
	echo build, run, both

build:
	javac $(CP) $(SP) ./server/Server.java

run:
	java $(RUNPATH) Server ./

both:	build	run

clean:
	rm server/*.class

