CREATE TABLE P1_ROW (
    ID int NOT NULL AUTO_INCREMENT,
    TITLE varchar(255) NULL,

    PRIMARY KEY (ID)
);

INSERT INTO P1_ROW (TITLE)
    VALUE ("First row");

ALTER TABLE TASK
    ADD COLUMN P1_ROW_FK int NOT NULL DEFAULT 1 AFTER ID;

ALTER TABLE TASK
ADD FOREIGN KEY (P1_ROW_FK) REFERENCES P1_ROW(ID);

ALTER TABLE TASK
    ALTER COLUMN P1_ROW_FK DROP DEFAULT;