ALTER TABLE reminder
    ADD COLUMN escalation_threshold INT NOT NULL DEFAULT 2
    COMMENT '连续未确认达到该次数时通知家属' AFTER consecutive_missed_count;

ALTER TABLE family_binding
    ADD COLUMN application_note VARCHAR(300) NULL
    COMMENT '家属提交的绑定申请说明' AFTER relation;
