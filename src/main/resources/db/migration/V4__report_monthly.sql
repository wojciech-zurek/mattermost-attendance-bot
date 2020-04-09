INSERT INTO configs (key, value, mm_user_name)
VALUES ('command.report.monthly.prefix', '!report monthly', 'default');
INSERT INTO configs (key, value, mm_user_name)
VALUES ('command.report.monthly.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name)
VALUES ('command.report.monthly.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name)
VALUES ('command.report.monthly.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name)
VALUES ('command.report.monthly.block.channel', '', 'default');

CREATE INDEX ON mm_users (mm_user_name);