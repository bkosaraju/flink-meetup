
ARG FLINK_IMAGE
FROM gradle:8.10.2-jdk17-alpine@sha256:bc4cfcd317082aa35b3d5d86bbaa90f958fc3a3088ac8077db9dc443c1a347e7 AS rebuilder
COPY ./ /opt/rule-engine
RUN cd /opt/rule-engine; mount --bind ~/.gradle/ ~/.gradle/ ; gradle clean shadowJar -x test --no-daemon

FROM ${FLINK_IMAGE}
COPY --from=rebuilder /opt/rule-engine/build/libs/rule-engine-all.jar /opt/flink/usrlib/rule-engine-all.jar