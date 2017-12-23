MAIN=Ada40

build:
	- rm *.class
	javac -classpath '.;../common' *.java

run: build
	java -classpath '.;../common' ${MAIN}

	