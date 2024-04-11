CREATE TABLE if not exists event_data (
        id Int,
        attr1 VARCHAR(20),
        attr2 VARCHAR(20),
        attr4 VARCHAR(20),
        attr6 VARCHAR(20)
    ) WITH (
          'connector' = 'kafka',
          'topic'     = 'event',
          'properties.bootstrap.servers' = 'localhost:29092',
          'scan.startup.mode' = 'latest-offset',
          'format'    = 'json',
          'json.fail-on-missing-field' = 'false',
          'json.ignore-parse-errors' = 'true'
          );