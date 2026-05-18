FROM rockylinux/rockylinux:9-ubi AS source
RUN --mount=type=bind,source=./artifacts/,target=/artifacts/,z dnf install -y /artifacts/*.rpm && dnf clean all

FROM localhost/microjre-builder:latest as optimize
COPY --from=source /opt/teragrep /opt/teragrep
RUN /optimize.sh 25 /opt/teragrep/k8s_01/lib/k8s_01.jar /opt/teragrep/k8s_01;

FROM scratch
COPY --from=optimize /opt/teragrep /opt/teragrep
VOLUME /opt/teragrep/k8s_01/var
VOLUME /opt/teragrep/k8s_01/etc
WORKDIR /opt/teragrep/k8s_01
ENTRYPOINT [ "/opt/teragrep/k8s_01/bin/java", "-jar", "lib/k8s_01.jar" ]
