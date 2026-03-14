CREATE TABLE IF NOT EXISTS project (
    id              BIGSERIAL PRIMARY KEY,
    project_code    VARCHAR(64) NOT NULL UNIQUE,
    project_name    VARCHAR(200) NOT NULL,
    status          VARCHAR(32) NOT NULL,
    description     TEXT,
    workspace_path  VARCHAR(500),
    memory_path     VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE project ADD COLUMN IF NOT EXISTS workspace_path VARCHAR(500);
ALTER TABLE project ADD COLUMN IF NOT EXISTS memory_path VARCHAR(500);

CREATE TABLE IF NOT EXISTS task (
    id                  BIGSERIAL PRIMARY KEY,
    task_code           VARCHAR(64) NOT NULL UNIQUE,
    project_id          BIGINT NOT NULL REFERENCES project(id),
    parent_task_id      BIGINT REFERENCES task(id),
    title               VARCHAR(200) NOT NULL,
    task_type           VARCHAR(64) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    priority            VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    detail              TEXT,
    initiator           VARCHAR(100),
    owner_name          VARCHAR(100),
    blocker_contact     VARCHAR(200),
    block_reason        VARCHAR(500),
    planned_finish_at   TIMESTAMP,
    actual_start_at     TIMESTAMP,
    actual_finish_at    TIMESTAMP,
    input_json          JSONB,
    output_json         JSONB,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_task_project_id ON task(project_id);
CREATE INDEX IF NOT EXISTS idx_task_parent_task_id ON task(parent_task_id);
CREATE INDEX IF NOT EXISTS idx_task_status ON task(status);
CREATE INDEX IF NOT EXISTS idx_task_task_type ON task(task_type);
CREATE INDEX IF NOT EXISTS idx_task_priority ON task(priority);

CREATE TABLE IF NOT EXISTS task_log (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    log_type        VARCHAR(32) NOT NULL,
    message         TEXT NOT NULL,
    payload_json    JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_task_log_task_id ON task_log(task_id);

CREATE TABLE IF NOT EXISTS task_event (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
    event_type      VARCHAR(32) NOT NULL,
    from_status     VARCHAR(32),
    to_status       VARCHAR(32),
    operator_name   VARCHAR(100),
    note            VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_task_event_task_id ON task_event(task_id);

CREATE TABLE IF NOT EXISTS project_meeting (
    id                      BIGSERIAL PRIMARY KEY,
    meeting_code            VARCHAR(64) NOT NULL UNIQUE,
    project_id              BIGINT NOT NULL REFERENCES project(id),
    related_task_id         BIGINT REFERENCES task(id),
    topic                   VARCHAR(200) NOT NULL,
    problem_statement       TEXT,
    organizer_name          VARCHAR(100) NOT NULL,
    status                  VARCHAR(32) NOT NULL DEFAULT 'VOTING',
    scheduled_at            TIMESTAMP,
    decision_option         VARCHAR(200),
    decision_summary        TEXT,
    decision_options_json   JSONB,
    minutes_json            JSONB,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_project_meeting_project_id ON project_meeting(project_id);
CREATE INDEX IF NOT EXISTS idx_project_meeting_status ON project_meeting(status);

CREATE TABLE IF NOT EXISTS meeting_participant (
    id                  BIGSERIAL PRIMARY KEY,
    meeting_id          BIGINT NOT NULL REFERENCES project_meeting(id) ON DELETE CASCADE,
    member_name         VARCHAR(100) NOT NULL,
    member_role         VARCHAR(64),
    member_mention      VARCHAR(64),
    responsibility      VARCHAR(200),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_meeting_participant_meeting_id ON meeting_participant(meeting_id);

CREATE TABLE IF NOT EXISTS meeting_vote (
    id                  BIGSERIAL PRIMARY KEY,
    meeting_id          BIGINT NOT NULL REFERENCES project_meeting(id) ON DELETE CASCADE,
    voter_name          VARCHAR(100) NOT NULL,
    voter_role          VARCHAR(64),
    voter_mention       VARCHAR(64),
    option_key          VARCHAR(200) NOT NULL,
    reason              VARCHAR(500),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_meeting_vote UNIQUE (meeting_id, voter_name)
);

CREATE INDEX IF NOT EXISTS idx_meeting_vote_meeting_id ON meeting_vote(meeting_id);
