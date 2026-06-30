CREATE TABLE IF NOT EXISTS user_service.users(
  id UUID PRIMARY KEY,
  email VARCHAR(100),
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255),
  subscribers BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT check_username CHECK (username ~ '^[A-Za-z0-9._-]+$'),
  CONSTRAINT check_email CHECK (email ~ '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$')
);



CREATE TABLE IF NOT EXISTS user_service.user_subscriptions(
  channel_id UUID NOT NULL,
  subscriber_id UUID NOT NULL,
  PRIMARY KEY (channel_id, subscriber_id),
  FOREIGN KEY (channel_id) REFERENCES user_service.users(id) ON DELETE CASCADE,
  FOREIGN KEY (subscriber_id) REFERENCES user_service.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_subscriptions_channel_id ON user_service.user_subscriptions(channel_id);
CREATE INDEX idx_user_subscriptions_subscriber_id ON user_service.user_subscriptions(subscriber_id);
