UPDATE app_compras c
SET comision_vendedor = ROUND(c.monto_adjudicacion * s.comision_vendedor_pct / 100, 2)
FROM app_solicitudes_consignacion s
WHERE s.item_catalogo_id = c.item_catalogo_id
  AND s.comision_vendedor_pct IS NOT NULL
  AND c.comision_vendedor IS DISTINCT FROM
      ROUND(c.monto_adjudicacion * s.comision_vendedor_pct / 100, 2);
