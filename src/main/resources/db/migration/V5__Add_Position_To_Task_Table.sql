ALTER TABLE TASK
    ADD COLUMN POSITION int NOT NULL DEFAULT 1 AFTER TITLE;

ALTER TABLE TASK
    ALTER COLUMN SIZE DROP DEFAULT;