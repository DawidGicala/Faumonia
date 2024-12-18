#!/bin/sh
POLSKAONLINE_VERSION="0.45"

export
LOCALCLASSPATH=.:data/script/:data/conf/:polskaonline-server-$POLSKAONLINE_VERSION.jar:libs/marauroa.jar:libs/mysql-connector.jar:libs/log4j.jar:libs/commons-lang.jar:libs/simple.jar:libs/tiled.jar:libs/groovy.jar

java -Xmx400m -cp "${LOCALCLASSPATH}" games.stendhal.server.StendhalServer -c server.ini -l

