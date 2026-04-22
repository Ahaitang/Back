-- Drop specialized dictionary tables after migration to dict_common
-- These tables are no longer needed after V20240423_3 migration

DROP TABLE IF EXISTS frequency_dict;
DROP TABLE IF EXISTS route_dict;
DROP TABLE IF EXISTS medication_dict;
DROP TABLE IF EXISTS disease_dict;