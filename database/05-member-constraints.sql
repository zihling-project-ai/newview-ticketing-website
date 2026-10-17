-- Prevent concurrent registrations from reusing an email. Existing demo emails are unique.
ALTER TABLE user ADD CONSTRAINT uq_recovery_email UNIQUE(userEmail);
