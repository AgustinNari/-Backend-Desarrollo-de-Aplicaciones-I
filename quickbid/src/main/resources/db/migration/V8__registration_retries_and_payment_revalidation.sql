ALTER TABLE app_inscripciones_subasta
    DROP CONSTRAINT IF EXISTS uq_app_inscripciones_subasta;

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_inscripciones_subasta_activa
    ON app_inscripciones_subasta (subasta_id, cuenta_id)
    WHERE estado IN ('pendiente_validacion', 'aprobada');
