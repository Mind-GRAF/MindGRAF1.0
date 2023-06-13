#!/bin/bash

# Check if argument is provided
if [[ $# -eq 0 ]] ; then
    echo 'Usage: run.sh package_name.MainClass'
    exit 1
fi

# Get the path of the bin directory
BIN_DIR="bin"

# Get the class name and package name
PACKAGE_NAME=$(echo $1 | cut -d '.' -f 1)
CLASS_NAME=$(echo $1 | cut -d '.' -f 2)

# Create the class path
CLASS_PATH="$BIN_DIR:$BIN_DIR/$PACKAGE_NAME"

# Compile the Java file if it doesn't exist in the bin directory
if [[ ! -f "$CLASS_PATH/$CLASS_NAME.class" ]] ; then
    javac -d $BIN_DIR $(find src -name *.java)
fi

# Run the Java file
java -cp $CLASS_PATH $1
