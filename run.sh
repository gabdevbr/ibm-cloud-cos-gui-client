#!/bin/bash

# IBM Cloud Object Storage Client
# Launch script for macOS/Linux

JAR_FILE="ibm-cos-client.jar"
JAVA_OPTS="-Xmx512m -Dapple.laf.useScreenMenuBar=true -Dapple.awt.application.name=IBM_COS_Client"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 8 or higher and try again"
    exit 1
fi

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: $JAR_FILE not found"
    echo "Please make sure the JAR file is in the same directory as this script"
    exit 1
fi

# Launch the application
echo "Starting IBM Cloud Object Storage Client..."
java $JAVA_OPTS -jar "$JAR_FILE"