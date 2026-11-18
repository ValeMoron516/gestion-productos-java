-- Para una base existente de tu proyecto original (ya tiene rubros y productos
-- con rubro_id). Hacer COPIA DE SEGURIDAD antes de ejecutar este archivo.
-- Ejecutar conectado a la base gestion_productos, UNA SOLA VEZ.
BEGIN;

CREATE TABLE IF NOT EXISTS proveedores (
    id_prov INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    domicilio VARCHAR(200),
    telefono VARCHAR(50)
);

ALTER TABLE productos ADD COLUMN IF NOT EXISTS proveedor_id INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_productos_proveedor'
          AND conrelid = 'productos'::regclass
    ) THEN
        ALTER TABLE productos
        ADD CONSTRAINT fk_productos_proveedor
        FOREIGN KEY (proveedor_id) REFERENCES proveedores(id_prov)
        ON DELETE RESTRICT;
    END IF;
END $$;

COMMIT;
-- Los productos viejos quedan con proveedor_id = NULL, sin perderse.
-- Se verán como "Sin asignar". Para modificarlos, elegí un proveedor.
-- Nuevos productos requerirán proveedor existente desde la aplicación.
