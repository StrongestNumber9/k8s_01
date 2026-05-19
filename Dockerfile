FROM rockylinux/rockylinux:9-ubi AS builder
RUN --mount=type=bind,source=./artifacts/,target=/artifacts/,z dnf install -y /artifacts/*.rpm
RUN dnf install -y java-25-openjdk-devel java-25-openjdk-jmods binutils;
RUN /usr/lib/jvm/java-25-openjdk/bin/jdeps --multi-release=25 -quiet --print-module-deps --ignore-missing-deps --recursive /opt/teragrep/k8s_01/lib/k8s_01.jar > jre_modules.out;
RUN /usr/bin/xargs -a jre_modules.out /usr/lib/jvm/java-25-openjdk/bin/jlink --compress=zip-9 --no-header-files --no-man-pages --strip-debug --output=/opt/teragrep/k8s_01-jre/ --add-modules;

FROM rockylinux/rockylinux:9-ubi-micro
COPY --from=builder /opt/teragrep /opt/teragrep
VOLUME /opt/teragrep/k8s_01/var
VOLUME /opt/teragrep/k8s_01/etc
WORKDIR /opt/teragrep/k8s_01
ENTRYPOINT [ "/opt/teragrep/k8s_01-jre/bin/java", "-jar", "lib/k8s_01.jar" ]
