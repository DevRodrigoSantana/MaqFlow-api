-- Apply once to the MySQL database before starting the application with ddl-auto=validate.
CREATE TABLE refresh_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    family_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    replaced_by_hash VARCHAR(64) NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_refresh_tokens_family (family_id)
) ENGINE=InnoDB;
