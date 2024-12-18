set FAUMONIAONLINE_VERSION=4.1
set LOCALCLASSPATH=.;data\script;data\conf;faumoniaonline-server-%FAUMONIAONLINE_VERSION%.jar;marauroa.jar;commons-lang.jar;mysql-connector.jar;h2.jar;log4j.jar;simple.jar;libtiled.jar;groovy.jar
java -Xmx400m -cp "%LOCALCLASSPATH%" games.stendhal.server.StendhalServer -c server.ini -l