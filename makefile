
default:
	@echo "build run"

build:
	mvn compile
# 	mvn clean package

run:
	mvn -q exec:java -Dexec.args="."
# 	java -jar target/camel-up-1.0-SNAPSHOT-jar-with-dependencies.jar .

both:	build	run

deploy-test:
	mvn clean install
	java -jar target/camel-up-1.0-SNAPSHOT-jar-with-dependencies.jar .
