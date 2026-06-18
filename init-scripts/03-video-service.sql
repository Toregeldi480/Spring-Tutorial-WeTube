CREATE TABLE IF NOT EXISTS video_service.videos(
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  title VARCHAR(100) NOT NULL,
  description VARCHAR(5000),
  original_filename VARCHAR(255) NOT NULL,
  duration BIGINT,
  file_size BIGINT,
  likes BIGINT NOT NULL DEFAULT 0,
  tsvector_column TSVECTOR,
  status VARCHAR(20) NOT NULL DEFAULT 'UPLOADING',
  visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
  CONSTRAINT chk_video_status CHECK (status IN ('UPLOADING', 'READY', 'FAILED')),
  CONSTRAINT chk_video_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE'))
);

CREATE OR REPLACE FUNCTION video_service.update_tsvector_column() RETURNS TRIGGER AS $$
BEGIN
  NEW.tsvector_column:= to_tsvector('english', NEW.title || ' ' || NEW.description);
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tsvector_update ON video_service.videos;
CREATE TRIGGER tsvector_update BEFORE INSERT OR UPDATE
  ON video_service.videos FOR EACH ROW EXECUTE FUNCTION video_service.update_tsvector_column();



CREATE TABLE video_service.video_likes (
    video_id UUID NOT NULL,
    user_id UUID NOT NULL,
    PRIMARY KEY (video_id, user_id),
    FOREIGN KEY (video_id) REFERENCES video_service.videos(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_service.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_video_likes_video_id ON video_service.video_likes(video_id);
CREATE INDEX idx_video_likes_user_id ON video_service.video_likes(user_id);
