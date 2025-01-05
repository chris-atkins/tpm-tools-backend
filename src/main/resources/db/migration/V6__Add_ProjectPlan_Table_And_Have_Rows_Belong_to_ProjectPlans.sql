CREATE TABLE p0_project_plan (
    id SERIAL NOT NULL,
    title varchar(255) NULL,

    PRIMARY KEY (id)
);

INSERT INTO p0_project_plan (title)
    VALUES ('First Project Plan');

ALTER TABLE p1_row
    ADD COLUMN p0_project_plan_fk int NOT NULL DEFAULT 1;

ALTER TABLE p1_row
ADD FOREIGN KEY (p0_project_plan_fk) REFERENCES p0_project_plan(id);

ALTER TABLE p1_row
    ALTER COLUMN p0_project_plan_fk DROP DEFAULT;