#!/bin/bash
set -e

echo "Creating directories..."
mkdir -p ~/.jdk
mkdir -p ~/Android/Sdk/cmdline-tools

echo "Downloading OpenJDK 17..."
if [ ! -d "$HOME/.jdk/jdk-17.0.2" ]; then
DDDDDDDDDDDDDDDDDDD    tar -xzf /tmp/openjdk-17.tar.gz -C ~/.jdk/
    rm /tmp/openjdk-17.tar.gz
fi

echo "Setting up Java environment variables for current session..."
export JAVA_HOME="$HOME/.jdk/jdk-17.0.2"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Downloading Android Command-line Tools..."
if [ ! -d "$HOME/Android/Sdk/cmdline-tools/latest" ]; then
    wget -q -O /tmp/cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
    unzip -q /tmp/cmdline-tools.zip -d ~/Android/Sdk/cmdline-tools/
    mv ~/Android/Sdk/cmdline-tools/cmdline-tools ~/Android/Sdk/cmdline-tools/latest
    rm /tmp/cmdline-tools.zip
fi

echo "Setting up Android environment variables for current session..."
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"

echo "Accepting Android licenses..."
yes | sdkmanager --licenses > /dev/null

echo "Updating bashrc with environment variables..."
grep -q "JAVA_HOME" ~/.bashrc || echo 'export JAVA_HOME="$HOME/.jdk/jdk-17.0.2"' >> ~/.bashrc
grep -q "ANDROID_HOME" ~/.bashrc || echo 'export ANDROID_HOME="$HOME/Android/Sdk"' >> ~/.bashrc
grep -q "ANDROID_SDK_ROOT" ~/.bashrc || echo 'export ANDROID_SDK_ROOT="$HOME/Android/Sdk"' >> ~/.bashrc
grep -q "\$JAVA_HOME/bin" ~/.bashrc || echo 'export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"' >> ~/.bashrc

echo "Setup Complete!"
java -version
sdkmanager --version
