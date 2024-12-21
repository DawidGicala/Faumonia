#!/bin/sh
ant compile_polskaonlinetools server_build

LOCALCLASSPATH=build/build_polskaonlinetools:build/build_server:build/build_server_maps:libs/marauroa.jar:libs/log4j.jar:libs/commons-lang.jar:libs/groovy.jar:libs/mysql-connector.jar:libs/h2.jar:libs/swing-layout.jar:.
java -cp "${LOCALCLASSPATH}" games.stendhal.tools.npcparser.TestEnvDlg
