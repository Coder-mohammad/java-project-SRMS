#!/bin/bash

# Navigate to the project root directory
# Compile the Java files with the MySQL connector in the classpath
echo "Compiling..."
javac -cp ".:lib/mysql-connector-j.jar" -d bin src/main/java/com/srms/*.java

# Run the application
echo "Running application..."
java -cp "bin:lib/mysql-connector-j.jar" com.srms.SRMS
