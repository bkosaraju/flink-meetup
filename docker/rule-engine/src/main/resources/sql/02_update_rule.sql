insert into mysql.demo.rule(rulename, rulekey, rulevalue, actionid, actionvalue)
select rulename, rulekey, rulevalue, actionid, actionvalue
from kafka_rule;