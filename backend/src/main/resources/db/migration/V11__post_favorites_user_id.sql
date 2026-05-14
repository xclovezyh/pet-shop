ALTER TABLE post_favorites
    ADD COLUMN user_id BIGINT NULL COMMENT '收藏用户ID，关联 app_user.id' AFTER id;

ALTER TABLE post_favorites
    ADD COLUMN user_nickname_snapshot VARCHAR(255) NULL COMMENT '收藏时的用户昵称快照，仅用于展示' AFTER user_id;

CREATE TABLE IF NOT EXISTS post_favorites_migration_issues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    favorite_id BIGINT NOT NULL COMMENT '原收藏记录ID',
    user_nickname VARCHAR(255) NOT NULL COMMENT '原收藏记录中的昵称',
    issue_type VARCHAR(64) NOT NULL COMMENT '迁移问题类型：UNMATCHED_NICKNAME / DUPLICATE_NICKNAME',
    issue_detail VARCHAR(255) NOT NULL COMMENT '迁移问题说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间'
) COMMENT='post_favorites 迁移问题清单';

INSERT INTO post_favorites_migration_issues (favorite_id, user_nickname, issue_type, issue_detail)
SELECT favorite.id,
       favorite.user_nickname,
       CASE
           WHEN user_match.match_count IS NULL THEN 'UNMATCHED_NICKNAME'
           ELSE 'DUPLICATE_NICKNAME'
       END,
       CASE
           WHEN user_match.match_count IS NULL THEN '收藏昵称在 app_user 中找不到唯一匹配，已保留到问题清单'
           ELSE '收藏昵称在 app_user 中存在多个匹配，无法安全回填 user_id'
       END
FROM post_favorites favorite
LEFT JOIN (
    SELECT nickname, COUNT(*) AS match_count
    FROM app_user
    GROUP BY nickname
) user_match ON user_match.nickname = favorite.user_nickname
WHERE user_match.nickname IS NULL OR user_match.match_count <> 1;

UPDATE post_favorites favorite
JOIN (
    SELECT nickname, MAX(id) AS user_id
    FROM app_user
    GROUP BY nickname
    HAVING COUNT(*) = 1
) user_match ON user_match.nickname = favorite.user_nickname
SET favorite.user_id = user_match.user_id,
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
