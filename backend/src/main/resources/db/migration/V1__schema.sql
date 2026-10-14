CREATE TABLE users (
    id           UUID PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL,
    email        VARCHAR(160) NOT NULL,
    roles        VARCHAR(100) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE campaigns (
    id                      UUID PRIMARY KEY,
    creator_id              UUID           NOT NULL,
    title                   VARCHAR(150)   NOT NULL,
    description             VARCHAR(4000)  NOT NULL,
    category                VARCHAR(30)    NOT NULL,
    goal_amount             NUMERIC(19, 2) NOT NULL,
    currency                VARCHAR(3)     NOT NULL,
    deadline                TIMESTAMP WITH TIME ZONE NOT NULL,
    status                  VARCHAR(30)    NOT NULL,
    raised_amount           NUMERIC(19, 2) NOT NULL DEFAULT 0,
    contributions_count     INTEGER        NOT NULL DEFAULT 0,
    contributors_count      INTEGER        NOT NULL DEFAULT 0,
    contributors_visibility VARCHAR(10)    NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at            TIMESTAMP WITH TIME ZONE,
    closed_at               TIMESTAMP WITH TIME ZONE,
    goal_reached_at         TIMESTAMP WITH TIME ZONE,
    closing_soon_notified   BOOLEAN        NOT NULL DEFAULT FALSE,
    version                 BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_campaigns_creator FOREIGN KEY (creator_id) REFERENCES users (id),
    CONSTRAINT ck_campaigns_goal_positive CHECK (goal_amount > 0),
    CONSTRAINT ck_campaigns_raised_non_negative CHECK (raised_amount >= 0)
);
CREATE INDEX ix_campaigns_status_deadline ON campaigns (status, deadline);
CREATE INDEX ix_campaigns_creator ON campaigns (creator_id);

CREATE TABLE campaign_milestones (
    id          UUID PRIMARY KEY,
    campaign_id UUID        NOT NULL,
    type        VARCHAR(30) NOT NULL,
    reached_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    description VARCHAR(255),
    CONSTRAINT fk_milestones_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id),
    -- every milestone type is recorded at most once per campaign
    CONSTRAINT uk_milestones_campaign_type UNIQUE (campaign_id, type)
);

CREATE TABLE contributions (
    id              UUID PRIMARY KEY,
    campaign_id     UUID           NOT NULL,
    contributor_id  UUID           NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL,
    anonymous       BOOLEAN        NOT NULL DEFAULT FALSE,
    message         VARCHAR(500),
    idempotency_key UUID           NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_contributions_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id),
    CONSTRAINT fk_contributions_contributor FOREIGN KEY (contributor_id) REFERENCES users (id),
    CONSTRAINT uk_contributions_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT ck_contributions_amount_positive CHECK (amount > 0)
);
CREATE INDEX ix_contributions_campaign ON contributions (campaign_id, created_at);
CREATE INDEX ix_contributions_contributor ON contributions (contributor_id, created_at);

CREATE TABLE notifications (
    id              UUID PRIMARY KEY,
    recipient_id    UUID          NOT NULL,
    type            VARCHAR(30)   NOT NULL,
    campaign_id     UUID,
    title           VARCHAR(160)  NOT NULL,
    message         VARCHAR(1000) NOT NULL,
    source_event_id UUID,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    is_read         BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id)
);
CREATE INDEX ix_notifications_recipient ON notifications (recipient_id, created_at);

CREATE TABLE outbox_events (
    id              UUID PRIMARY KEY,
    event_type      VARCHAR(100)  NOT NULL,
    aggregate_id    UUID          NOT NULL,
    payload         VARCHAR(8000) NOT NULL,
    occurred_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    attempts        INTEGER       NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_error      VARCHAR(1000),
    processed_at    TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_outbox_status_next_attempt ON outbox_events (status, next_attempt_at);
