CREATE SCHEMA IF NOT EXISTS user_service;
CREATE SCHEMA IF NOT EXISTS video_service;

GRANT ALL ON SCHEMA user_service TO admin;
GRANT ALL ON SCHEMA video_service TO admin;