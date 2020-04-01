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
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.absence', '!absence', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.attendance', '!attendance', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.away', '!away', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.config.get', '!config get', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.config.set', '!config set', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.help', '!help', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.last', '!last', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.members', '!members', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.now', '!now', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.online', '!online', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.rollback', '!rollback', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.start', '!start', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.status', '!status', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.stop', '!stop', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.whoami', '!whoami', 'default');
INSERT INTO configs (key, value, mm_user_name) VALUES ('command.prefix.who', '!who', 'default');