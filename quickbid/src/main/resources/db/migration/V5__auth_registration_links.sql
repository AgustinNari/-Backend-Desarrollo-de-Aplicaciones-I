ALTER TABLE app_solicitudes_registro ADD COLUMN persona_id integer REFERENCES personas(identificador);
ALTER TABLE app_solicitudes_registro ADD COLUMN cliente_id integer REFERENCES clientes(identificador);

CREATE UNIQUE INDEX uq_app_solicitudes_registro_persona
    ON app_solicitudes_registro(persona_id) WHERE persona_id IS NOT NULL;
CREATE UNIQUE INDEX uq_app_solicitudes_registro_cliente
    ON app_solicitudes_registro(cliente_id) WHERE cliente_id IS NOT NULL;
