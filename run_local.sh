#!/bin/bash

echo "Building..."
mvn -q clean compile

mkdir -p results

echo "Running..."
java -cp target/classes com.concurrent.Main

echo "Done. See results/ folder."
