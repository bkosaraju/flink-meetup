CREATE TABLE if not exists kafka_rule (
            rulename STRING,
            rulekey STRING,
            rulevalue STRING,
            actionid STRING,
            actionvalue STRING
    ) WITH (
          'connector' = 'kafka',
          'topic'     = 'rule',
          'properties.bootstrap.servers' = 'kafka:9094',
          'scan.startup.mode' = 'latest-offset',
          'format'    = 'json'
          );