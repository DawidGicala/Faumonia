@echo off
call ant compile_polskaonlinetools server_build

set LOCALCLASSPATH=build\build_polskaonlinetools;build\build_server;build\build_server_maps;libs\marauroa.jar;libs\log4j.jar;libs\commons-lang.jar;libs\groovy.jar;libs\mysql-connector.jar;libs\h2.jar;libs\swing-layout.jar;.
javaw -cp "%LOCALCLASSPATH%" games.stendhal.tools.npcparser.TestEnvDlg
