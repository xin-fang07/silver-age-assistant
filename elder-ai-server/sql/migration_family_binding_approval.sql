-- 家属绑定审批：0 已解除，1 已绑定，2 待老人确认，3 已拒绝。
ALTER TABLE family_binding
  MODIFY COLUMN status TINYINT NOT NULL DEFAULT 2
  COMMENT '状态：0-已解除,1-有效,2-待老人确认,3-已拒绝';

CREATE INDEX idx_elder_binding_status ON family_binding (elder_user_id, status);
