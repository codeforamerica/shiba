--ALTER TABLE applications
--    ADD time_to_complete number(19,0) DEFAULT 0 ;

--UPDATE applications
--SET time_to_complete =(
         --SELECT  time_to_complete
         --FROM application_metrics
--WHERE  completed_at = applications.completed_at;
--     );

ALTER TABLE applications modify  time_to_complete NOT NULL;
