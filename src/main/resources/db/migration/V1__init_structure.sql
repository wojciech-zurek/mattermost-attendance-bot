CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE mm_users
(
    mm_user_id              VARCHAR(26)  NOT NULL,
    public_id               UUID         NOT NULL,
    mm_user_name            VARCHAR(256) NOT NULL,
    mm_user_email           VARCHAR(128),
    mm_channel_id           VARCHAR(26)  NOT NULL,
    mm_channel_name         VARCHAR      NOT NULL,
    mm_channel_display_name VARCHAR      NOT NULL,
    mm_status               VARCHAR(16)  NOT NULL,
    create_date             TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (mm_user_id)
);

CREATE INDEX ON mm_users (public_id);
CREATE INDEX ON mm_users (mm_channel_id);

CREATE TABLE IF NOT EXISTS attendance
(
    id            BIGSERIAL   NOT NULL,
    public_id     UUID        NOT NULL,
    mm_user_id    VARCHAR(26) NOT NULL REFERENCES mm_users (mm_user_id),
    work_date     DATE        NOT NULL DEFAULT NOW(),
    sign_in_date  BIGINT      NOT NULL,
    sign_out_date BIGINT      NOT NULL,
    work_time     INT         NOT NULL DEFAULT 0,
    absence_time  INT         NOT NULL DEFAULT 0,
    status        VARCHAR(16) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX ON attendance (public_id);
CREATE INDEX ON attendance (mm_user_id);
CREATE INDEX ON attendance (work_date);
CREATE INDEX ON attendance (sign_in_date);
CREATE INDEX ON attendance (sign_out_date);

CREATE TABLE IF NOT EXISTS absences
(
    id          BIGSERIAL   NOT NULL,
    public_id   UUID        NOT NULL,
    mm_user_id  VARCHAR(26) NOT NULL REFERENCES mm_users (mm_user_id),
    away_time   BIGINT      NOT NULL,
    away_type   VARCHAR(16),
    online_time BIGINT,
    online_type VARCHAR(16),
    PRIMARY KEY (id)
);
CREATE INDEX ON absences (public_id);
CREATE INDEX ON absences (mm_user_id);


CREATE TABLE IF NOT EXISTS configs
(
    key   VARCHAR(32) NOT NULL,
    value VARCHAR(64) NOT NULL,
    PRIMARY KEY (key)
);