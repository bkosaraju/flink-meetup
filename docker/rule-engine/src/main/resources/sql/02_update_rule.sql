insert into mysql.demo.rule(rulename, rulekey, rulevalue, actionid)
select rulename, rulekey, rulevalue, actionid
from kafka_rule;