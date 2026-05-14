ALTER TABLE moment
    ADD COLUMN author_user_id BIGINT NULL COMMENT '发布人用户ID，关联 app_user.id' AFTER author;

UPDATE moment moment_row
JOIN app_user user_account ON user_account.nickname = moment_row.author
SET moment_row.author_user_id = user_account.id
WHERE moment_row.author_user_id IS NULL
  AND moment_row.author IS NOT NULL
  AND moment_row.author <> '';

ALTER TABLE moment_comment
    ADD COLUMN author_user_id BIGINT NULL COMMENT '评论人用户ID，关联 app_user.id' AFTER author;

UPDATE moment_comment comment_row
JOIN app_user user_account ON user_account.nickname = comment_row.author
SET comment_row.author_user_id = user_account.id
WHERE comment_row.author_user_id IS NULL
  AND comment_row.author IS NOT NULL
  AND comment_row.author <> '';

ALTER TABLE private_message_threads
    ADD COLUMN starter_user_id BIGINT NULL COMMENT '发起人用户ID，关联 app_user.id' AFTER starter,
    ADD COLUMN recipient_user_id BIGINT NULL COMMENT '接收人用户ID，关联 app_user.id' AFTER recipient;

UPDATE private_message_threads thread_row
JOIN app_user starter_account ON starter_account.nickname = thread_row.starter
LEFT JOIN app_user recipient_account ON recipient_account.nickname = thread_row.recipient
SET thread_row.starter_user_id = starter_account.id,
    thread_row.recipient_user_id = recipient_account.id
WHERE (thread_row.starter_user_id IS NULL OR thread_row.recipient_user_id IS NULL);

ALTER TABLE private_messages
    ADD COLUMN sender_user_id BIGINT NULL COMMENT '发送人用户ID，关联 app_user.id' AFTER sender;

UPDATE private_messages message_row
JOIN app_user user_account ON user_account.nickname = message_row.sender
SET message_row.sender_user_id = user_account.id
WHERE message_row.sender_user_id IS NULL
  AND message_row.sender IS NOT NULL
  AND message_row.sender <> '';

ALTER TABLE trade_intents
    ADD COLUMN requester_user_id BIGINT NULL COMMENT '意向提交人用户ID，关联 app_user.id' AFTER requester,
    ADD COLUMN owner_user_id BIGINT NULL COMMENT '帖子发布人用户ID，关联 app_user.id' AFTER owner;

UPDATE trade_intents intent_row
JOIN app_user requester_account ON requester_account.nickname = intent_row.requester
LEFT JOIN app_user owner_account ON owner_account.nickname = intent_row.owner
SET intent_row.requester_user_id = requester_account.id,
    intent_row.owner_user_id = owner_account.id
WHERE intent_row.requester_user_id IS NULL
   OR intent_row.owner_user_id IS NULL;

ALTER TABLE content_reports
    ADD COLUMN reporter_user_id BIGINT NULL COMMENT '举报人用户ID，关联 app_user.id' AFTER reporter;

UPDATE content_reports report_row
JOIN app_user user_account ON user_account.nickname = report_row.reporter
SET report_row.reporter_user_id = user_account.id
WHERE report_row.reporter_user_id IS NULL
  AND report_row.reporter IS NOT NULL
  AND report_row.reporter <> '';

