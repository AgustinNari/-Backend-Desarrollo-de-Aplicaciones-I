ALTER TABLE app_subasta_estado_vivo
    ADD COLUMN IF NOT EXISTS retencion_hasta timestamp with time zone;

CREATE INDEX IF NOT EXISTS idx_app_subasta_estado_vivo_retencion
    ON app_subasta_estado_vivo (retencion_hasta)
    WHERE retencion_hasta IS NOT NULL;

ALTER TABLE app_consignacion_devoluciones
    ADD COLUMN IF NOT EXISTS direccion_envio_id bigint REFERENCES app_direcciones_envio (id);

CREATE INDEX IF NOT EXISTS idx_app_consignacion_devoluciones_direccion
    ON app_consignacion_devoluciones (direccion_envio_id);
