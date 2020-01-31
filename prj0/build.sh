#!/bin/sh

JAR_NAME=projtec-1.0.1.jar
JAVA_HOME=/opt/JDK
JAR_HOME=lib
CLASSPATH=.
SOURCES=src
CLASSES=classes

rm -f $JAR_HOME/$JAR_NAME

for jar in $(ls -A $JAR_HOME/*.jar); do
	CLASSPATH="$CLASSPATH:$jar"
done

echo $CLASSPATH

[ ! -d $CLASSES ] && mkdir $CLASSES 

$JAVA_HOME/bin/javac -cp "$CLASSPATH" -d $CLASSES/ $SOURCES/*.java

$JAVA_HOME/bin/jar cvfM  $JAR_HOME/$JAR_NAME -C $CLASSES/ .

[ -d $CLASSES ] && rm -rf $CLASSES

exit 0
