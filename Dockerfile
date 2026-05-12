FROM rockylinux/rockylinux:9
RUN --mount=type=bind,source=./rpm/,target=/rpm/ dnf -y install /rpm/*.rpm && dnf clean all
VOLUME /opt/teragrep/k8s_01/var
VOLUME /opt/teragrep/k8s_01/etc
WORKDIR /opt/teragrep/k8s_01
ENTRYPOINT [ "/usr/bin/java", "--add-opens", "java.base/java.nio=ALL-UNNAMED", "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "-jar", "lib/k8s_01.jar" ]
