#!/bin/bash
JAVA_VERSION="${1}";
JAR_PATH="${2}";
COMPONENT_PATH="${3}";
echo "Optimizing ${JAR_PATH} on java version ${JAVA_VERSION} to ${COMPONENT_PATH}";
dnf install -y "java-${JAVA_VERSION}-openjdk-devel" "java-${JAVA_VERSION}-openjdk-jmods" binutils;
"/usr/lib/jvm/java-${JAVA_VERSION}-openjdk/bin/jdeps" "--multi-release=${JAVA_VERSION}" -quiet --print-module-deps --ignore-missing-deps --recursive "${JAR_PATH}" > jre_modules.out;
/usr/bin/xargs -a jre_modules.out "/usr/lib/jvm/java-${JAVA_VERSION}-openjdk/bin/jlink" --compress=zip-9 --no-header-files --no-man-pages --strip-debug "--output=/tmp/microjre/" --add-modules;
cp -arv /tmp/microjre/* "${COMPONENT_PATH}/";
