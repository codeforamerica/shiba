ALTER TABLE research
RENAME COLUMN live_alone TO has_household;

UPDATE research SET has_household = NOT has_household;