# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

ARG FLINK_IMAGE
FROM $FLINK_IMAGE AS flink-distribution
FROM apache/zeppelin:0.11.1
ENV FLINK_HOME="/opt/flink"
COPY flink-interpreter-setting.json /opt/zeppelin/interpreter/flink/interpreter-setting.json
COPY interpreter.json /opt/zeppelin/conf/
COPY --from=flink-distribution ${FLINK_HOME} ${FLINK_HOME}
COPY notebook /opt/zeppelin/notebook
USER 0
RUN mv ${FLINK_HOME}/opt/flink-table-planner_2.12-1.16.3.jar ${FLINK_HOME}/lib/ &&\
    mv ${FLINK_HOME}/lib/flink-table-planner-loader-1.16.3.jar ${FLINK_HOME}/opt/ &&\
    mv ${FLINK_HOME}/opt/flink-sql-client-1.16.3.jar ${FLINK_HOME}/lib &&\
    wget -P ${FLINK_HOME}/lib/  https://repo1.maven.org/maven2/org/apache/flink/flink-table-api-scala-bridge_2.12/1.16.3/flink-table-api-scala-bridge_2.12-1.16.3.jar &&\
    wget -P ${FLINK_HOME}/lib/ https://repo1.maven.org/maven2/org/apache/flink/flink-table-api-scala_2.12/1.16.3/flink-table-api-scala_2.12-1.16.3.jar &&\
    find /usr/bin/find /opt -type f -name '._*' -delete
