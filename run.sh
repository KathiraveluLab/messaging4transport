#!/bin/bash

echo "Starting Messaging4Transport OpenDaylight Controller..."

# Set JAVA_HOME to the automatically downloaded Java 8 if possible
if [ -d ".jdk/jdk8u412-b08" ]; then
    export JAVA_HOME=$PWD/.jdk/jdk8u412-b08
    export PATH=$JAVA_HOME/bin:$PATH
    echo "[INFO] Using project-local Java 8 from $JAVA_HOME"
else
    # Check if system Java is Java 8
    JAVA_VER=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    if [[ "$JAVA_VER" != "1.8."* && "$JAVA_VER" != "8."* ]]; then
        echo "[ERROR] System Java is not version 8 ($JAVA_VER)."
        echo "[ERROR] Please run ./setup.sh first to download the local OpenJDK 8 distribution."
        exit 1
    fi
fi

# Patch Karaf Maven config to use HTTPS (avoids 501 Not Implemented from Maven Central)
MVN_CFG="karaf/target/assembly/etc/org.ops4j.pax.url.mvn.cfg"
if [ -f "$MVN_CFG" ]; then
    if grep -q "http://repo1.maven.org" "$MVN_CFG"; then
        echo "[INFO] Patching Karaf Maven config to use HTTPS..."
        sed -i 's|http://|https://|g' "$MVN_CFG"
        # Ensure ODL repos are present and properly formatted
        if ! grep -q "opendaylight.public" "$MVN_CFG"; then
            sed -i '$ s/$/, \\/' "$MVN_CFG"
            echo "    https://nexus.opendaylight.org/content/repositories/public/@id=opendaylight.public, \\" >> "$MVN_CFG"
            echo "    https://nexus.opendaylight.org/content/repositories/opendaylight.release/@id=opendaylight.release" >> "$MVN_CFG"
        fi
    fi
fi

KARAF_BIN="karaf/target/assembly/bin/karaf"

if [ -x "$KARAF_BIN" ]; then
    echo "Launching Karaf..."
    exec "$KARAF_BIN"
else
    echo "[ERROR] Karaf executable not found at $KARAF_BIN. Make sure the build succeeded using ./setup.sh."
    exit 1
fi
