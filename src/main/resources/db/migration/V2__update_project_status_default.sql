ALTER TABLE project ALTER COLUMN status SET DEFAULT 'PENDING';

UPDATE project
SET status = 'PENDING'
WHERE status IS NULL OR lower(status) = 'draft';

UPDATE project
SET status = 'COMPLETED'
WHERE lower(status) = 'complete';
