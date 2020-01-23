#!/bin/sh

JAVA_HOME=/opt/jdk/jre
JAVA=$JAVA_HOME/bin/java
JAR_HOME=lib
CLASSPATH=.
MAIN_CLASS=com.dma.nicomains.Main_CA_Parser_Encodage

for jar in $(ls $JAR_HOME/*.jar); do
  CLASSPATH="$CLASSPATH:$jar"
done

$JAVA -cp "$CLASSPATH" $MAIN_CLASS "$@"

