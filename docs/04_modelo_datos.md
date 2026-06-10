# 04 — Modelo de datos

## Regla central: legacy intocable

La estructura SQL existente debe considerarse externa/legacy. No se puede:

- agregar columnas;
- modificar tipos;
- agregar valores a checks/enums;
- cambiar relaciones;
- cambiar nombres;
- alterar semántica.

Si la base está vacía, el backend debe poder crear esas tablas como migración inicial. Luego, toda extensión debe hacerse con tablas nuevas.

## Tablas legacy detectadas

- `paises`
- `personas`
- `empleados`
- `sectores`
- `seguros`
- `clientes`
- `duenios`
- `subastadores`
- `subastas`
- `productos`
- `fotos`
- `catalogos`
- `itemsCatalogo`
- `asistentes`
- `pujos`
- `registroDeSubasta`

Ver `source_material/consigna_y_sql_legacy_original.md` para SQL completo.

## Problemas/limitaciones legacy a resolver con tablas nuevas

- `personas` no tiene email, apellido separado, password, tokens ni sesión.
- `clientes` tiene categoría y admitido, pero no estado app, puntos, bloqueo, multas ni password.
- `duenios` requiere verificador NOT NULL, por lo que no puede crearse al inicio de una consignación.
- `subastas` no contempla claramente moneda, título comercial, segmento, estado vivo, lote activo, etc.
- `pujos` legacy puede no alcanzar para orden, idempotencia, concurrencia y eventos live.
- Archivos/documentos requieren almacenamiento y metadata separada.
- Comisiones, multas, pagos, compras, documentos, notificaciones y auditoría requieren estructuras nuevas.

## Tablas nuevas recomendadas

Usar prefijo `app_` o nombres claros para distinguir de legacy.

### Registro y cuentas

#### `app_solicitudes_registro`

Campos sugeridos:

- `id`
- `email` único parcial mientras pendiente
- `nombre`
- `apellido`
- `domicilio_legal`
- `id_pais_origen` FK `paises.numero`
- `foto_frente_dni_archivo_id` nullable
- `foto_dorso_dni_archivo_id` nullable
- `estado`
- `motivo_rechazo`
- `setup_token_hash`
- `setup_token_expires_at`
- `setup_token_used_at`
- `created_at`, `updated_at`, `last_activity_at`
- `aprobado_por_empleado_id` nullable FK `empleados.identificador`

#### `app_cuentas`

- `id`
- `persona_id` FK `personas.identificador`
- `cliente_id` FK `clientes.identificador`
- `email` único
- `password_hash`
- `estado`
- `puntos`
- `categoria_calculada`
- `refresh_token_hash` o tabla separada
- `ultimo_acceso_at`
- `intentos_login`
- `created_at`, `updated_at`

- Consideración importante:

La categoría operativa debe calcularse desde puntos/configuración en tablas app_, pero debe mantenerse sincronizada con clientes.categoria cuando cambie, porque el sistema legacy también usa esa categoría. No se modifica la estructura legacy, solo se actualiza el valor existente.

#### `app_refresh_tokens`

- `id`, `cuenta_id`, `token_hash`, `expires_at`, `revoked_at`, `created_at`, `user_agent`, `ip`.

#### `app_password_reset_tokens`

- `id`, `cuenta_id`, `token_hash`, `expires_at`, `used_at`, `created_at`.

### Categorías y puntos

#### `app_categoria_puntos`

- `categoria`, `puntos_minimos`, `orden`.

#### `app_movimientos_puntos`

- `id`, `cuenta_id`, `delta`, `motivo`, `referencia_tipo`, `referencia_id`, `created_at`.

### Archivos/documentos

#### `app_archivos`

- `id`
- `owner_cuenta_id` nullable
- `tipo_contexto` (`dni`, `cheque`, `consignacion`, `producto`, `documento_compra`, etc.)
- `filename_original`
- `content_type`
- `size_bytes`
- `storage_path`
- `checksum`
- `created_at`

#### `app_documentos`

- `id`
- `tipo`
- `referencia_tipo`
- `referencia_id`
- `archivo_id`
- `estado`
- `created_at`

### Medios de pago

#### `app_medios_pago`

- `id`
- `cuenta_id`
- `tipo`
- `moneda`
- `estado` (`pendiente_verificacion`, `verificado`, `rechazado`, `eliminado`)
- `principal`
- `nacional`
- `alias_visible` / `ultimos_4`
- `titular`
- `hash_identificador` para duplicados sin guardar PAN claro
- `limite_monto` nullable
- `saldo_garantia` nullable para cheques
- `verificado_por_empleado_id` nullable
- `created_at`, `updated_at`, `deleted_at`

#### Tablas detalle opcionales

- `app_tarjetas`
- `app_cuentas_bancarias`
- `app_cheques_certificados`

Separarlas ayuda a no llenar `app_medios_pago` de columnas nullable.

### Subastas extendidas

#### `app_subasta_ext`

- `subasta_id` FK `subastas.identificador`
- `titulo`
- `descripcion`
- `moneda`
- `segmento`
- `estado_operativo` (`programada`, `abierta`, `en_vivo`, `cerrada`, `finalizada`)
- `permite_inscripcion_online`
- `created_at`, `updated_at`

#### `app_subasta_estado_vivo`

- `subasta_id`
- `item_catalogo_activo_id` nullable
- `version`
- `usuarios_conectados`
- `updated_at`

#### `app_inscripciones_subasta`

- `id`
- `subasta_id`
- `cuenta_id`
- `medio_pago_id`
- `estado`
- `numero_postor` nullable, coordinado con `asistentes.numeroPostor` si aplica
- `created_at`

### Pujas

#### `app_pujas_live`

- `id`
- `subasta_id`
- `item_catalogo_id`
- `cuenta_id`
- `medio_pago_id`
- `monto`
- `moneda`
- `estado` (`pendiente`, `aceptada`, `rechazada`, `superada`, `ganadora`, `anulada_admin`)
- `secuencia`
- `version_estado`
- `motivo_rechazo`
- `created_at`, `confirmed_at`

Mantener sincronización o vínculo con `pujos` legacy si el sistema existente lo requiere.

### Compras, pagos y multas

#### `app_compras`

- `id`
- `subasta_id`
- `item_catalogo_id`
- `producto_id`
- `cuenta_comprador_id` nullable si compra empresa
- `comprador_empresa` boolean
- `puja_id` nullable
- `monto_adjudicacion`
- `moneda`
- `estado` (Posiblemente separarlo en más atributos de estado si se considera acorde)
- `medio_pago_id`
- `created_at`, `updated_at`

#### `app_pagos`

- `id`
- `compra_id` nullable
- `multa_id` nullable
- `medio_pago_id`
- `monto`
- `moneda`
- `estado`
- `referencia_externa`
- `error_codigo`, `error_detalle`
- `created_at`, `updated_at`

#### `app_multas`

- `id`
- `cuenta_id`
- `compra_id`
- `monto`
- `moneda`
- `porcentaje` default 10
- `estado` (`pendiente`, `pagada`, `vencida`, `derivada`)
- `vence_at`
- `created_at`, `paid_at`

#### `app_entregas`

- `id`
- `compra_id`
- `tipo` (`envio`, `retiro`)
- `direccion_envio_id` nullable
- `costo_envio`
- `estado`
- `perdio_cobertura_seguro` boolean
- `created_at`, `updated_at`

#### `app_direcciones_envio`

- `id`, `cuenta_id`, datos dirección, `principal`, `created_at`, `updated_at`.

### Consignación

#### `app_solicitudes_consignacion`

Tabla maestra del flujo completo, un registro por bien.

- `id`
- `cuenta_id` / `cliente_id`
- `producto_id` nullable FK `productos.identificador`
- `item_catalogo_id` nullable FK `itemsCatalogo`
- `subasta_id` nullable FK `subastas`
- `titulo`
- `descripcion`
- `categoria_sugerida`
- `historia`
- `artista_disenador`
- `fecha_objeto`
- `declaracion_propiedad` boolean
- `acepta_devolucion_con_cargo` boolean
- `estado` (Posiblemente separarlo en más atributos de estado si se considera acorde)
- `requiere_documentacion_origen` boolean
- `motivo_rechazo`
- `revisor_empleado_id` nullable
- `valor_base_propuesto`
- `moneda_propuesta`
- `comision_comprador_pct` / `comision_vendedor_pct` o tabla separada
- `acuerdo_texto`
- `acuerdo_enviado_at`, `acuerdo_aceptado_at`, `acuerdo_rechazado_at`
- `created_at`, `updated_at`

#### `app_consignacion_fotos`

- `id`, `solicitud_id`, `archivo_id`, `orden`, `created_at`.

#### `app_consignacion_documentos_origen`

- `id`, `solicitud_id`, `archivo_id`, `estado`, `created_at`.

#### `app_consignacion_devoluciones`

- `id`, `solicitud_id`, `motivo`, `costo`, `moneda`, `estado`, `pago_id`, `created_at`.

#### `app_liquidaciones_consignacion`

- `id`, `solicitud_id`, `compra_id`, `monto_bruto`, `comision`, `monto_neto`, `cuenta_destino`, `estado`, `created_at`, `paid_at`.

### Notificaciones y ayuda

#### `app_notificaciones`

- `id`, `cuenta_id`, `tipo`, `titulo`, `descripcion`, `referencia_tipo`, `referencia_id`, `leida`, `created_at`.

### Auditoría

#### `app_auditoria`

- `id`
- `actor_tipo` (`usuario`, `admin`, `sistema`)
- `actor_id` nullable
- `accion`
- `entidad_tipo`
- `entidad_id`
- `before_json` nullable
- `after_json` nullable
- `metadata_json`
- `created_at`

## Transacciones obligatorias

- Aprobar registro: solicitud -> `personas` -> `clientes` -> token/cuenta pendiente.
- Finalizar registro: validar token -> crear/activar `app_cuentas` -> invalidar token.
- Verificar medio de pago y sumar puntos.
- Inscripción a subasta.
- Pujar.
- Cerrar lote: marcar ganadora -> compra -> producto vendido -> notificación.
- Pago exitoso/fallido + multa/restricción.
- Pagar multa + obligación pendiente.
- Vencimiento de multa + bloqueo.
- Aceptar acuerdo de consignación -> asegurar `duenios` -> crear `productos` -> vincular solicitud.
- Publicar consignación en subasta/catálogo.
- Liquidar consignación.

---

## Addendum post revisión de modelo de datos

- `app_medios_pago` debe incluir `limite_aprobado` obligatorio al verificar/revalidar, `verificado_hasta`, estado `vencido`, baja lógica y estructura de consumo/reserva.
- No interpretar `limite_aprobado = null` como sin límite.
- Agregar `app_reservas_medio_pago` o equivalente para reservas temporales de puja ganadora: estados `activa`, `liberada`, `consumida`.
- `app_direcciones_envio` debe soportar hasta 5 activas por cuenta, una principal y baja lógica.
- Compras deben copiar snapshot de dirección al pagar extras.
- Separar `segmento` y `categoriaSubasta`: segmento es rubro/tema; categoría de subasta es común/especial/plata/oro/platino.
- `app_documentos`/`app_documentos_generados` debe cubrir factura de compra, recibo de multa, acuerdo de consignación, liquidación y comprobante de devolución.
- `app_inscripciones_subasta` debe permitir reintento si una inscripción fue rechazada, por ejemplo evitando unicidad rígida para estados rechazados.
