#!/bin/bash
JAVA_VERSION="${1}";
JAR_PATH="${2}";
COMPONENT_NAME="${3}";
echo "Optimizing ${COMPONENT_NAME}:${JAR_PATH} on java version ${JAVA_VERSION}";
dnf install -y "java-${JAVA_VERSION}-openjdk-devel" "java-${JAVA_VERSION}-openjdk-jmods" binutils;
"/usr/lib/jvm/java-${JAVA_VERSION}-openjdk/bin/jdeps" "--multi-release=${JAVA_VERSION}" -quiet --print-module-deps --ignore-missing-deps --recursive "${JAR_PATH}" > jre_modules.out;
/usr/bin/xargs -a jre_modules.out "/usr/lib/jvm/java-${JAVA_VERSION}-openjdk/bin/jlink" --compress=zip-9 --no-header-files --no-man-pages --strip-debug "--output=/tmp/${COMPONENT_NAME}/" --add-modules;
cp -arv "/tmp/${COMPONENT_NAME}/*" "/opt/teragrep/${COMPONENT_NAME}/";
