-- Usar EN UNA BASE NUEVA llamada gestion_productos; crearla antes desde pgAdmin.
-- Las tablas se relacionan mediante claves foráneas y conservan productos dados de baja.
CREATE TABLE rubros (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE proveedores (
    id_prov INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    razon_social VARCHAR(150) NOT NULL,
    domicilio VARCHAR(200),
    telefono VARCHAR(50)
);

CREATE TABLE productos (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(250),
    precio NUMERIC(12,2) NOT NULL CHECK (precio > 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta DATE NOT NULL DEFAULT CURRENT_DATE,
    rubro_id INTEGER NOT NULL REFERENCES rubros(id) ON DELETE RESTRICT,
    -- Nullable para facilitar incorporación gradual de registros anteriores.
    -- Desde el programa los productos nuevos requieren un proveedor válido.
    proveedor_id INTEGER REFERENCES proveedores(id_prov) ON DELETE RESTRICT
);

INSERT INTO rubros (nombre) VALUES ('Informática'), ('Librería'), ('Audio');
-- Registrar los proveedores desde el menú 3 antes de cargar productos.


-- Continuación: para poder iniciar sesión, ejecutar también
-- sql/segunda_guia_roles.sql en esta misma base de datos.
