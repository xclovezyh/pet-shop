ALTER TABLE market_post
    ADD COLUMN author_user_id BIGINT NULL COMMENT '发布人用户ID，关联 app_user.id' AFTER author;

UPDATE market_post post
JOIN app_user user_account ON user_account.nickname = post.author
SET post.author_user_id = user_account.id
WHERE post.author_user_id IS NULL
  AND post.author IS NOT NULL
  AND post.author <> '';

