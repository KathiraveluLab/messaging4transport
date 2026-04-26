#!/bin/bash

# Messaging4Transport Setup Script
# Automates ActiveMQ setup and project build

echo "Checking for AMQP broker on localhost:5672..."

if (echo > /dev/tcp/localhost/5672) >/dev/null 2>&1; then
    echo "[OK] Broker detected on port 5672. Using existing broker."
else
    echo "[INFO] No broker detected on port 5672."
    if command -v docker >/dev/null 2>&1; then
        echo "Starting ActiveMQ Classic via Docker..."
        docker run -d --name activemq-m4t -p 5672:5672 -p 8161:8161 apache/activemq-classic:latest
        
        echo "Waiting for ActiveMQ to initialize (15s)..."
        sleep 15
        
        if (echo > /dev/tcp/localhost/5672) >/dev/null 2>&1; then
            echo "[OK] ActiveMQ started successfully."
        else
            echo "[WARNING] ActiveMQ might still be starting. Please check 'docker logs activemq-m4t'."
        fi
    else
        echo "[ERROR] Docker not found. Please start an AMQP broker manually on port 5672."
        exit 1
    fi
fi

echo "Checking Java version..."
JAVA_VER=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
if [[ "$JAVA_VER" != "1.8."* && "$JAVA_VER" != "8."* ]]; then
    echo "[INFO] Java 8 is required to build this project."
    if [ ! -d ".jdk/jdk8u412-b08" ]; then
        echo "[INFO] Downloading Temurin Java 8 to .jdk directory..."
        mkdir -p .jdk
        cd .jdk
        wget -q https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u412-b08/OpenJDK8U-jdk_x64_linux_hotspot_8u412b08.tar.gz
        tar -xzf OpenJDK8U-jdk_x64_linux_hotspot_8u412b08.tar.gz
        rm OpenJDK8U-jdk_x64_linux_hotspot_8u412b08.tar.gz
        cd ..
    fi
    export JAVA_HOME=$PWD/.jdk/jdk8u412-b08
    export PATH=$JAVA_HOME/bin:$PATH
    echo "[INFO] Using Java 8 from $JAVA_HOME"
fi

# Ensure dummy jsr173-ri is installed to avoid repository timeouts
mvn install:install-file -Dfile=/dev/null -DgroupId=com.bea.xml -DartifactId=jsr173-ri -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true >/dev/null 2>&1

echo "------------------------------------------"
echo "Building Messaging4Transport..."
echo "------------------------------------------"

mvn clean install -s settings.xml -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip

if [ $? -eq 0 ]; then
    echo "------------------------------------------"
    echo "[OK] Build Successful!"
    echo "To run the controller:"
    echo "  ./run.sh"
    echo ""
    echo "Inside the Karaf console, install the feature:"
    echo "  feature:install odl-messaging4transport"
    echo "------------------------------------------"
else
    echo "[ERROR] Build Failed. Please check the logs above."
    exit 1
fi
