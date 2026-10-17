-- Recovery additions, inferred from entities + partial historical SQL. Not original DDL.
ALTER TABLE post MODIFY postContent TEXT NOT NULL, MODIFY postDateTime DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), MODIFY lastEditedTime DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), MODIFY likeCount INT NOT NULL DEFAULT 0, MODIFY disLikeCount INT NOT NULL DEFAULT 0;
ALTER TABLE postmessage MODIFY mesContent TEXT NOT NULL, MODIFY messageDate DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE user ADD CONSTRAINT uq_recovery_account UNIQUE(userAccount);
ALTER TABLE likes ADD CONSTRAINT uq_recovery_reaction UNIQUE(postID,userID);
CREATE INDEX ix_recovery_feed ON post(postDateTime,postID);
CREATE INDEX ix_recovery_comments ON postmessage(postID,messageDate);
