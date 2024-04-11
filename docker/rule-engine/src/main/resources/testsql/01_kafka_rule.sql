CREATE TABLE if not exists kafka_rule (
            rulename VARCHAR(20),
            rulekey VARCHAR(20),
            rulevalue VARCHAR(20),
            actionid int
    ) WITH (
          'connector' = 'kafka',
          'topic'     = 'rule',
          'properties.bootstrap.servers' = 'localhost:29092',
          'scan.startup.mode' = 'earliest-offset',
          'format'    = 'json'
          );