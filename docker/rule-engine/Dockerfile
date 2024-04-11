
ARG FLINK_IMAGE
FROM gradle:7.5.1-jdk11-alpine@sha256:0c20c5771cbd452394e2d532592ff274daaaf026c4251a4b6988d31d82970e90 AS rebuilder
COPY ./ /opt/rule-engine
RUN cd /opt/rule-engine; mount --bind ~/.gradle/ ~/.gradle/ ; gradle clean shadowJar -x test

FROM ${FLINK_IMAGE}
COPY --from=rebuilder /opt/rule-engine/build/libs/rule-engine-all.jar /opt/flink/usrlib/rule-engine-all.jar