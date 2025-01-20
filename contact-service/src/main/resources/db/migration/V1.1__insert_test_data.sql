INSERT INTO contacts (email, first_name, last_name, created_at, updated_at)
VALUES
    ('hamza@hamza.com', 'Hamza', 'Hamza', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bilal@bilal.com', 'Bilal', 'Bilal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('bob@wilson.com', 'Bob', 'Wilson', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO contact_tags (contact_id, tag)
VALUES
    (1, 'customer'),
    (1, 'vip'),
    (2, 'prospect'),
    (2, 'newsletter'),
    (3, 'customer'),
    (3, 'inactive');