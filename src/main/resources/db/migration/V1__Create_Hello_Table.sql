
CREATE TABLE hello (
    id SERIAL primary key ,
    message varchar(255)
);

INSERT INTO hello (message)
VALUES ('Oh hello from the db!'),
        ('DB says hi :) '),
        ('DB is angry :(');