FROM rockylinux/rockylinux:9-ubi AS assembly_container
RUN --mount=type=bind,source=./artifacts/,target=/artifacts/,z dnf install --releasever 9 --setopt install_weak_deps=false --nodocs --installroot /sysroot -y /artifacts/*.rpm && dnf clean all --installroot /sysroot
RUN dnf install -y java-25-openjdk-devel java-25-openjdk-jmods binutils;
RUN /usr/lib/jvm/java-25-openjdk/bin/jdeps --multi-release=25 -quiet --print-module-deps --ignore-missing-deps --recursive /sysroot/opt/teragrep/k8s_01/lib/k8s_01.jar > jre_modules.out
RUN /usr/bin/xargs -a jre_modules.out /usr/lib/jvm/java-25-openjdk/bin/jlink --compress=zip-9 --no-header-files --no-man-pages --strip-debug --output=/sysroot/opt/teragrep/java --add-modules

FROM rockylinux/rockylinux:9-ubi-micro
COPY --from=assembly_container /sysroot/opt/teragrep /opt/teragrep
VOLUME /opt/teragrep/k8s_01/var
VOLUME /opt/teragrep/k8s_01/etc
WORKDIR /opt/teragrep/k8s_01
ENTRYPOINT [ "/opt/teragrep/java/bin/java", "-jar", "lib/k8s_01.jar" ]
