-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;

-- Sample project with full document seeded by ProjectDocumentDevSeed (Project ID = 1)
INSERT INTO project (id, name, description, status, created_at, updated_at)
VALUES (1, 'Chapter Case Study Project', 'Complete STPA chapter case for application', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

ALTER SEQUENCE project_seq RESTART WITH 2;
