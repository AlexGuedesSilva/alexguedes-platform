CREATE TABLE identity_users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(160) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_credentials (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES identity_users(id),
    password_hash VARCHAR(255) NOT NULL,
    password_updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_identity_users_username ON identity_users(username);
CREATE INDEX idx_identity_users_email ON identity_users(email);
