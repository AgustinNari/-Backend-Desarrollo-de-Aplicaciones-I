-- Final consignment contract additions. Legacy tables remain unchanged.

ALTER TABLE app_solicitudes_consignacion
    ADD COLUMN IF NOT EXISTS segmento varchar(80);

UPDATE app_solicitudes_consignacion
SET segmento = COALESCE(segmento, categoria_sugerida, 'general')
WHERE segmento IS NULL;

ALTER TABLE app_solicitudes_consignacion
    ALTER COLUMN segmento SET NOT NULL;
