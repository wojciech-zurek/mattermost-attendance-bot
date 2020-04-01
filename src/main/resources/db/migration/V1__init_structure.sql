CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE mm_users
(
    mm_user_id              VARCHAR(26)                            NOT NULL,
    public_id               UUID                                   NOT NULL,
    mm_user_name            VARCHAR(256)                           NOT NULL,
    mm_user_email           VARCHAR(128),
    mm_channel_id           VARCHAR(26)                            NOT NULL,
    mm_channel_name         VARCHAR                                NOT NULL,
    mm_channel_display_name VARCHAR                                NOT NULL,
    mm_status               VARCHAR(16)                            NOT NULL,
    work_status             VARCHAR(16)                            NOT NULL,
    work_status_update_date TIMESTAMP WITH TIME ZONE               NOT NULL,
    absence_reason          VARCHAR(128)                           NOT NULL,
    create_date             TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    update_date             TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    PRIMARY KEY (mm_user_id)
);

CREATE INDEX ON mm_users (public_id);
CREATE INDEX ON mm_users (mm_channel_id);

CREATE TABLE IF NOT EXISTS attendance
(
    id            BIGSERIAL                NOT NULL,
    public_id     UUID                     NOT NULL,
    mm_user_id    VARCHAR(26)              NOT NULL REFERENCES mm_users (mm_user_id),
    work_date     DATE                     NOT NULL DEFAULT NOW(),
    sign_in_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    sign_out_date TIMESTAMP WITH TIME ZONE,
    work_time     BIGINT                   NOT NULL DEFAULT 0,
    away_time     BIGINT                   NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE INDEX ON attendance (public_id);
CREATE UNIQUE INDEX ON attendance (mm_user_id, work_date);
CREATE INDEX ON attendance (sign_in_date);
CREATE INDEX ON attendance (sign_out_date);

CREATE TABLE IF NOT EXISTS absences
(
    id            BIGSERIAL                NOT NULL,
    public_id     UUID                     NOT NULL,
    attendance_id BIGINT                   NOT NULL REFERENCES attendance (id),
    mm_user_id    VARCHAR(26)              NOT NULL REFERENCES mm_users (mm_user_id),
    reason        VARCHAR(128)             NOT NULL,
    away_time     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    away_type     VARCHAR(16)              NOT NULL,
    online_time   TIMESTAMP WITH TIME ZONE,
    online_type   VARCHAR(16),
    absence_type  VARCHAR(16)              NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX ON absences (public_id);
CREATE INDEX ON absences (attendance_id);
CREATE INDEX ON absences (mm_user_id);


CREATE TABLE IF NOT EXISTS configs
(
    key          VARCHAR(64)              NOT NULL,
    value        TEXT                     NOT NULL,
    mm_user_name VARCHAR(256)             NOT NULL,
    update_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (key)
);

INSERT INTO configs (key, value, mm_user_name) VALUES ('work.time.in.sec', '28800', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.absence.prefix', '!absence', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.absence.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.absence.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.absence.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.absence.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.attendance.prefix', '!attendance', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.attendance.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.attendance.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.attendance.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.attendance.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.away.prefix', '!away', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.away.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.away.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.away.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.away.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.get.prefix', '!config get', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.get.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.get.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.get.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.get.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.set.prefix', '!config set', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.set.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.set.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.set.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.config.set.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.help.prefix', '!help', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.help.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.help.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.help.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.help.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.last.prefix', '!last', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.last.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.last.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.last.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.last.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.members.prefix', '!members', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.members.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.members.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.members.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.members.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.now.prefix', '!now', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.now.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.now.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.now.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.now.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.online.prefix', '!online', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.online.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.online.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.online.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.online.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.rollback.prefix', '!rollback', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.rollback.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.rollback.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.rollback.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.rollback.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.start.prefix', '!start', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.start.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.start.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.start.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.start.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.status.prefix', '!status', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.status.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.status.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.status.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.status.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.stop.prefix', '!stop', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.stop.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.stop.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.stop.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.stop.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.whoami.prefix', '!whoami', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.whoami.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.whoami.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.whoami.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.whoami.block.channel', '', 'default');

INSERT INTO configs (key, value, mm_user_name) VALUES ('command.who.prefix', '!who', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.who.allow.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.who.allow.channel', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.who.block.user', '', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.who.block.channel', '', 'default');