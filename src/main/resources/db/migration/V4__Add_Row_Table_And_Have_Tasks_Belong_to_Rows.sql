CREATE TABLE p1_row (
    id SERIAL NOT NULL,
    title varchar(255) NULL,

    PRIMARY KEY (id)
);

INSERT INTO p1_row (title)
    VALUES ('First row');

ALTER TABLE task
    ADD COLUMN p1_row_fk int NOT NULL DEFAULT 1;

ALTER TABLE task
ADD FOREIGN KEY (p1_row_fk) REFERENCES p1_row(id);

ALTER TABLE task
    ALTER COLUMN p1_row_fk DROP DEFAULT;