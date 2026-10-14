-- Demo users. There is no real authentication: the frontend lets you "log in as" any of them.
INSERT INTO users (id, display_name, email, roles) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Ana Gómez', 'ana@g7.dev', 'CREATOR,CONTRIBUTOR'),
    ('44444444-4444-4444-4444-444444444444', 'Diego Pérez', 'diego@g7.dev', 'CREATOR,CONTRIBUTOR'),
    ('22222222-2222-2222-2222-222222222222', 'Bruno Díaz', 'bruno@g7.dev', 'CONTRIBUTOR'),
    ('33333333-3333-3333-3333-333333333333', 'Carla Ruiz', 'carla@g7.dev', 'CONTRIBUTOR'),
    ('99999999-9999-9999-9999-999999999999', 'Admin G7', 'admin@g7.dev', 'ADMIN');
