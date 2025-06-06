CREATE TABLE if not exists kafka_rule (
            rulename VARCHAR(20),
            rulekey VARCHAR(20),
            rulevalue STRING,
            actionid int
    ) WITH (
          'connector' = 'kafka',
          'topic'     = 'rule',
          'properties.bootstrap.servers' = 'localhost:9092',
          'scan.startup.mode' = 'latest-offset',
          'format'    = 'json'
          );