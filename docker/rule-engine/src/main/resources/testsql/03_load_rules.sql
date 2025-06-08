create  view IF NOT EXISTS all_rules as (
select rulename, rulekey, rulevalue, actionid, actionvalue from kafka_rule
--union all
--select rulename, rulekey, rulevalue, actionid from mysql.demo.rule
)