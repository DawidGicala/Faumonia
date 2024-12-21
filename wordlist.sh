#!/bin/sh
ant compile_polskaonlinetools server_build

LOCALCLASSPATH=build/build_polskaonlinetools:build/build_server:libs/marauroa.jar:libs/log4j.jar:libs/mysql-connector.jar:libs/h2.jar
java -cp "${LOCALCLASSPATH}" games.stendhal.tools.npcparser.WordListUpdate
