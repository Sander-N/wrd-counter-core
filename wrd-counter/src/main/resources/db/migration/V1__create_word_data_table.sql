CREATE TABLE word_data (
    id varchar(36) NOT NULL,
    words JSON NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;