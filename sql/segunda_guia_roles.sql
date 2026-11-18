-- SEGUNDA GUÍA. Ejecutar conectado a gestion_productos en pgAdmin.
-- No borra tablas, productos, rubros ni proveedores existentes.
-- Se puede volver a ejecutar: inserciones de ejemplo no duplican PIN.
-- Las actividades 4 y 5 amplían ck_roles_tipo para permitir invitado ('i').
BEGIN;

CREATE TABLE IF NOT EXISTS roles (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pin VARCHAR(4) NOT NULL UNIQUE,
    apellido VARCHAR(80) NOT NULL,
    nombre VARCHAR(80) NOT NULL,
    rol CHAR(1) NOT NULL,
    CONSTRAINT ck_roles_pin CHECK (pin ~ '^[0-9]{4}$'),
    CONSTRAINT ck_roles_tipo CHECK (rol IN ('e', 'a', 'i')),
    CONSTRAINT ck_roles_apellido CHECK (length(trim(apellido)) > 0),
    CONSTRAINT ck_roles_nombre CHECK (length(trim(nombre)) > 0)
);

-- Si alguien ejecutó antes el script básico del PDF (solo e/a),
-- ampliar la restricción de roles. Operación repetible.
ALTER TABLE roles DROP CONSTRAINT IF EXISTS ck_roles_tipo;
ALTER TABLE roles
    ADD CONSTRAINT ck_roles_tipo CHECK (rol IN ('e', 'a', 'i'));

-- Usuarios de DEMOSTRACIÓN dados por el PDF. No usar PIN reales aquí.
INSERT INTO roles (pin, apellido, nombre, rol) VALUES
    ('1234', 'Pérez', 'Ana', 'e'),
    ('5678', 'Gómez', 'Luis', 'a'),
    ('0042', 'Díaz', 'Carla', 'e')
ON CONFLICT (pin) DO NOTHING;

COMMIT;

-- El PIN NO figura en la consulta de presentación (evita divulgarlo).
SELECT id, apellido, nombre, rol FROM roles ORDER BY id;
