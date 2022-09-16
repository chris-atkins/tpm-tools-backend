
CREATE TABLE HELLO (
    ID int NOT NULL AUTO_INCREMENT,
    MESSAGE varchar(255),

    PRIMARY KEY (ID)
);

INSERT INTO HELLO (MESSAGE)
VALUES
        ("Oh hello from the db!"),
        ("DB says hi :) "),
        ("DB is angry :(");