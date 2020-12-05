# CP = -cp "./lib/*:../jetty-9.4.35/lib/websocket/*"


CP = -cp "./lib/*"
SP = -sourcepath ".:./game:./SocketServer"
RUNPATH = -cp "./lib/*:./lib/jetty/*:./server:./game:./SocketServer"

default:
	echo build, run, both

build:
	javac -Xlint:unchecked $(CP) $(SP) ./server/Server.java

run:
	java $(RUNPATH) Server ./

both:	build	run

clean:
	rm server/*.class game/*.class

ss:
	javac $(CP) $(SP) ./SocketServer/*.java
	java $(RUNPATH) SocketServer
