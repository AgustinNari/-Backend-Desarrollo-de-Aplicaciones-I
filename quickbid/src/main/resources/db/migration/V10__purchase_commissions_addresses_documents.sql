ALTER TABLE app_compras
    ADD COLUMN IF NOT EXISTS comision_comprador decimal(18, 2);

ALTER TABLE app_compras
    ADD COLUMN IF NOT EXISTS comision_vendedor decimal(18, 2);

UPDATE app_compras c
SET comision_comprador = CASE
        WHEN c.comprador_empresa THEN 0
        ELSE ROUND(c.monto_adjudicacion * i.comision / NULLIF(i."precioBase", 0), 2)
    END,
    comision_vendedor = ROUND(c.monto_adjudicacion * i.comision / NULLIF(i."precioBase", 0), 2)
FROM "itemsCatalogo" i
WHERE i.identificador = c.item_catalogo_id
  AND (c.comision_comprador IS NULL OR c.comision_vendedor IS NULL);

ALTER TABLE app_compras
    ALTER COLUMN comision_comprador SET NOT NULL;

ALTER TABLE app_compras
    ALTER COLUMN comision_vendedor SET NOT NULL;

ALTER TABLE app_entregas
    ADD COLUMN IF NOT EXISTS direccion_snapshot_json text;

ALTER TABLE app_entregas
    ADD COLUMN IF NOT EXISTS direccion_snapshot_at timestamp with time zone;
