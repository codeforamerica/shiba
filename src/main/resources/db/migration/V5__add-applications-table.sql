CREATE TABLE applications (
    id VARCHAR2(255) NOT NULL PRIMARY KEY,
    completed_at TIMESTAMP NOT NULL,
    data clob NOT NULL
)