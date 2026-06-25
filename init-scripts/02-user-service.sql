CREATE TABLE IF NOT EXISTS user_service.users(
  id UUID PRIMARY KEY,
  email VARCHAR(100),
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255),
  CONSTRAINT check_username CHECK (username ~ '^[A-Za-z0-9._-]+$'),
  CONSTRAINT check_email CHECK (email ~ '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$')
);

CREATE OR REPLACE FUNCTION update_modified_column() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language plpgsql;

CREATE TRIGGER update_users_modtime
    BEFORE UPDATE ON user_service.users
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();
