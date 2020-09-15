ALTER TABLE applications
    ADD COLUMN time_to_complete INTEGER;

UPDATE applications
SET time_to_complete = subquery.time_to_complete
FROM (
         SELECT completed_at, time_to_complete
         FROM application_metrics
     ) AS subquery
WHERE applications.completed_at = subquery.completed_at;

ALTER TABLE applications ALTER COLUMN time_to_complete SET NOT NULL;
