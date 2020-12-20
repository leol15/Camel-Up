
default:
	@echo "build run"

build:
	mvn clean package

run:
	java -jar target/camel-up-1.0-SNAPSHOT-jar-with-dependencies.jar .

both:	build	run

deploy-test:
	mvn clean install
	java -jar target/camel-up-1.0-SNAPSHOT-jar-with-dependencies.jar .
