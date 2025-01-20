CREATE TABLE contacts
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name  VARCHAR(100),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version    BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE contact_tags
(
    contact_id BIGINT      NOT NULL,
    tag        VARCHAR(50) NOT NULL,
    CONSTRAINT pk_contact_tags PRIMARY KEY (contact_id, tag),
    CONSTRAINT fk_contact_tags_contact FOREIGN KEY (contact_id)
        REFERENCES contacts (id) ON DELETE CASCADE
);

CREATE INDEX idx_contacts_email ON contacts (email);
CREATE INDEX idx_contact_tags_tag ON contact_tags (tag);

COMMENT
ON TABLE contacts IS 'Stores contact information for email marketing';
COMMENT
ON COLUMN contacts.email IS 'Unique email address of the contact';
COMMENT
ON TABLE contact_tags IS 'Tags associated with contacts for segmentation';