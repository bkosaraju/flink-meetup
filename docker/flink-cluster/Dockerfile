FROM apache/flink:1.18.1-scala_2.12-java17
RUN cd /tmp &&\
    apt update &&\
    apt install -y xz-utils &&\
    curl -O https://nodejs.org/dist/v22.16.0/node-v22.16.0-linux-x64.tar.xz &&\
    tar -xf node-v22.16.0-linux-x64.tar.xz &&\
    mv node-v*-linux-x64 /usr/local/node &&\
    ln -sf /usr/local/node/bin/node /usr/local/bin/node &&\
    ln -sf /usr/local/node/bin/npm /usr/local/bin/npm &&\
    ln -sf /usr/local/node/bin/npx /usr/local/bin/npx


ENV FLINK_VERSION=2.0.0 \
    SCALA_VERSION=2.12 \
    JAVA_VERSION=17
COPY conf /opt/flink/conf
# Download connector libraries
#wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/${FLINK_VERSION}/flink-sql-connector-kafka-${FLINK_VERSION}.jar; \
RUN wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-jdbc/${FLINK_VERSION}/flink-connector-jdbc-${FLINK_VERSION}.jar; \
    wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-csv/${FLINK_VERSION}/flink-csv-${FLINK_VERSION}.jar; \
    wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-json/${FLINK_VERSION}/flink-json-${FLINK_VERSION}.jar; \
    wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar; \
    wget -P /opt/flink/lib/ https://repo1.maven.org/maven2/org/apache/flink/flink-connector-kafka/${FLINK_VERSION}/flink-connector-kafka-${FLINK_VERSION}.jar; \
    wget -P /opt/flink/lib/ https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/3.2.3/kafka-clients-3.2.3.jar;

RUN echo "execution.checkpointing.interval: 10s" >> /opt/flink/conf/flink-conf.yaml; \
    echo "pipeline.object-reuse: true" >> /opt/flink/conf/flink-conf.yaml; \
    echo "pipeline.time-characteristic: EventTime" >> /opt/flink/conf/flink-conf.yaml; \
    echo "taskmanager.memory.jvm-metaspace.size: 256m" >> /opt/flink/conf/flink-conf.yaml;
