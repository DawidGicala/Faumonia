#!/bin/sh
cd -- "$(dirname -- "$0")" || exit 1
FAUMONIAONLINE_VERSION="8.0"
FAUMONIA_JAVA=${FAUMONIA_JAVA:-java}

for jar in "FaumoniaOnline-server-$FAUMONIAONLINE_VERSION.jar" \
    "FaumoniaOnline-maps-$FAUMONIAONLINE_VERSION.jar" \
    "FaumoniaOnline-xmlconf-$FAUMONIAONLINE_VERSION.jar" \
    marauroa.jar commons-lang.jar mysql-connector.jar h2.jar log4j.jar simple.jar groovy.jar
do
    if [ ! -f "$jar" ]; then
        echo "Missing file: $jar. Extract the complete server ZIP; do not unpack the JAR files." >&2
        exit 1
    fi
done

LOCALCLASSPATH="FaumoniaOnline-server-$FAUMONIAONLINE_VERSION.jar:FaumoniaOnline-maps-$FAUMONIAONLINE_VERSION.jar:FaumoniaOnline-xmlconf-$FAUMONIAONLINE_VERSION.jar:marauroa.jar:commons-lang.jar:mysql-connector.jar:h2.jar:log4j.jar:simple.jar:groovy.jar:data/script:data/conf:."
if [ "${1:-}" = "--check" ]; then
    exec "$FAUMONIA_JAVA" -cp "$LOCALCLASSPATH" games.stendhal.server.ServerPackageCheck
fi
exec "$FAUMONIA_JAVA" -Xmx400m -cp "$LOCALCLASSPATH" games.stendhal.server.StendhalServer -c server.ini -l "$@"

