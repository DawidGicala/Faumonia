@echo off
call ant compile_polskaonlinetools server_build

set LOCALCLASSPATH=build\build_polskaonlinetools;build\build_server;libs\marauroa.jar;libs\log4j.jar;libs\mysql-connector.jar;libs\h2.jar
javaw -cp "%LOCALCLASSPATH%" games.stendhal.tools.npcparser.WordListUpdate
