CREATE UNIQUE INDEX uk_app_user_nickname ON app_user (nickname);

ALTER TABLE post_favorites
    ADD COLUMN user_id BIGINT NULL COMMENT '收藏用户ID，关联 app_user.id' AFTER id;

ALTER TABLE post_favorites
    ADD COLUMN user_nickname_snapshot VARCHAR(255) NULL COMMENT '收藏时的用户昵称快照，仅用于展示' AFTER user_id;

UPDATE post_favorites favorite
JOIN app_user user_account ON user_account.nickname = favorite.user_nickname
SET favorite.user_id = user_account.id,
    favorite.user_nickname_snapshot = favorite.user_nickname
WHERE favorite.user_id IS NULL;

DELETE FROM post_favorites
WHERE user_id IS NULL;

ALTER TABLE post_favorites
    MODIFY user_id BIGINT NOT NULL COMMENT '收藏用户ID，关联 app_user.id';

ALTER TABLE post_favorites
    DROP INDEX uk_post_favorites_user_post;

ALTER TABLE post_favorites
    ADD CONSTRAINT uk_post_favorites_user_post UNIQUE (user_id, post_id);

ALTER TABLE post_favorites
    MODIFY user_nickname_snapshot VARCHAR(255) COMMENT '收藏时的用户昵称快照，仅用于展示';

ALTER TABLE post_favorites
    DROP COLUMN user_nickname;
