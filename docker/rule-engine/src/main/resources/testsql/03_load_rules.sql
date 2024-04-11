create  view IF NOT EXISTS all_rules as (
select rulename, rulekey, rulevalue, actionid from kafka_rule
union all
select rulename, rulekey, rulevalue, actionid from mysql.demo.rule)