CREATE TABLE IF NOT EXISTS app_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  avatar_url VARCHAR(255),
  bio VARCHAR(500),
  blacklisted BIT(1),
  blacklist_reason VARCHAR(255),
  city VARCHAR(255),
  created_at DATETIME(6),
  nickname VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pet_category (
  id BIGINT NOT NULL AUTO_INCREMENT,
  description VARCHAR(255),
  image_url VARCHAR(255),
  name VARCHAR(255),
  tags VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pet (
  id BIGINT NOT NULL AUTO_INCREMENT,
  age VARCHAR(255),
  age_range VARCHAR(255),
  breed VARCHAR(255),
  care_notes VARCHAR(600),
  category VARCHAR(255),
  city VARCHAR(255),
  dewormed BIT(1),
  gender VARCHAR(255),
  health_info VARCHAR(255),
  health_records VARCHAR(1000),
  image_url VARCHAR(255),
  image_urls VARCHAR(2000),
  name VARCHAR(255),
  neutered BIT(1),
  owner_name VARCHAR(255),
  personality VARCHAR(255),
  price DECIMAL(19,2),
  status VARCHAR(255),
  vaccinated BIT(1),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS market_post (
  id BIGINT NOT NULL AUTO_INCREMENT,
  audit_status VARCHAR(255),
  author VARCHAR(255),
  category VARCHAR(255),
  city VARCHAR(255),
  contact VARCHAR(255),
  created_at DATETIME(6),
  description VARCHAR(1000),
  image_url VARCHAR(255),
  image_urls VARCHAR(2000),
  price DECIMAL(19,2),
  status VARCHAR(255),
  title VARCHAR(255),
  type VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS moment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  audit_status VARCHAR(255),
  author VARCHAR(255),
  category VARCHAR(255),
  city VARCHAR(255),
  content VARCHAR(1000),
  created_at DATETIME(6),
  image_url VARCHAR(255),
  image_urls VARCHAR(2000),
  likes INT,
  pet_name VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS moment_comment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  author VARCHAR(255),
  content VARCHAR(500),
  created_at DATETIME(6),
  moment_id BIGINT,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS post_favorites (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6),
  post_id BIGINT NOT NULL,
  user_nickname VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_post_favorites_user_post (user_nickname, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS private_message_threads (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6),
  post_id BIGINT,
  post_title VARCHAR(255),
  recipient VARCHAR(255),
  starter VARCHAR(255),
  updated_at DATETIME(6),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS private_messages (
  id BIGINT NOT NULL AUTO_INCREMENT,
  content VARCHAR(1000),
  created_at DATETIME(6),
  read_by_recipient BIT(1),
  sender VARCHAR(255),
  thread_id BIGINT,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS trade_intents (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6),
  message VARCHAR(600),
  owner VARCHAR(255),
  post_id BIGINT,
  post_title VARCHAR(255),
  requester VARCHAR(255),
  status VARCHAR(255),
  updated_at DATETIME(6),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS content_reports (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME(6),
  handle_note VARCHAR(500),
  handled_at DATETIME(6),
  handled_by VARCHAR(255),
  reason VARCHAR(500),
  reporter VARCHAR(255),
  status VARCHAR(255),
  target_id BIGINT,
  target_type VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reference_option (
  id BIGINT NOT NULL AUTO_INCREMENT,
  label VARCHAR(255),
  option_type VARCHAR(255),
  sort_order INT,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS region_area (
  id BIGINT NOT NULL AUTO_INCREMENT,
  level VARCHAR(255),
  name VARCHAR(255),
  parent_id BIGINT,
  sort_order INT,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
