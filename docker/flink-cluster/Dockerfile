FROM apache/flink:1.16.3-scala_2.12-java11

# Download connector libraries
#wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/1.16.3/flink-sql-connector-kafka-1.16.3.jar; \
RUN wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-jdbc/1.16.3/flink-connector-jdbc-1.16.3.jar; \
    wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-csv/1.16.3/flink-csv-1.16.3.jar; \
    wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-json/1.16.3/flink-json-1.16.3.jar; \
    wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar; \
    wget -P /opt/flink/lib/ https://repo1.maven.org/maven2/org/apache/flink/flink-connector-kafka/1.16.3/flink-connector-kafka-1.16.3.jar; \
    wget -P /opt/flink/lib/ https://repo1.maven.org/maven2/org/apache/kafka/kafka-clients/3.2.3/kafka-clients-3.2.3.jar;

RUN echo "execution.checkpointing.interval: 10s" >> /opt/flink/conf/flink-conf.yaml; \
    echo "pipeline.object-reuse: true" >> /opt/flink/conf/flink-conf.yaml; \
    echo "pipeline.time-characteristic: EventTime" >> /opt/flink/conf/flink-conf.yaml; \
    echo "taskmanager.memory.jvm-metaspace.size: 256m" >> /opt/flink/conf/flink-conf.yaml;
