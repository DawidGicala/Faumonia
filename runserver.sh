#!/bin/sh
FAUMONIAONLINE_VERSION="4.0"

export
LOCALCLASSPATH=.:data/script/:data/conf/:faumoniaonline-server-$FAUMONIAONLINE_VERSION.jar:marauroa.jar:mysql-connector.jar:log4j.jar:commons-lang.jar:h2.jar:simple.jar:tiled.jar:groovy.jar

java -Xmx400m -cp "${LOCALCLASSPATH}" games.stendhal.server.StendhalServer -c server.ini -l

