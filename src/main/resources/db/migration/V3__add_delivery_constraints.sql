ALTER TABLE deliveries ADD CONSTRAINT chk_deliveries_status CHECK ( status IN ('IN_PROGRESS', 'DELIVERED') );

ALTER TABLE deliveries ADD CONSTRAINT chk_deliveries_finished_state
    CHECK ( (status = 'DELIVERED' AND finished_at IS NOT NULL) OR (status = 'IN_PROGRESS' AND finished_at IS NULL) );


ALTER TABLE deliveries ADD CONSTRAINT chk_deliveries_finished_after_started
    CHECK ( finished_at IS NULL OR finished_at >= started_at )