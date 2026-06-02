-- App-owned additions required by the complete consignment workflow.
-- Legacy tables remain unchanged.

ALTER TABLE app_solicitudes_consignacion
    DROP CONSTRAINT app_solicitudes_consignacion_estado_check;

ALTER TABLE app_solicitudes_consignacion
    ADD CONSTRAINT chk_app_consignacion_estado CHECK (estado IN (
        'pendiente_revision', 'rechazo_inicial', 'documentacion_adicional', 'documentacion_recibida',
        'recepcion_pendiente', 'revision_fisica', 'revision_fisica_aprobada', 'rechazo_revision_fisica',
        'acuerdo_pendiente', 'acuerdo_aceptado', 'acuerdo_rechazado', 'devolucion_pendiente',
        'publicada', 'en_subasta', 'vendida', 'comprada_por_empresa', 'liquidada',
        'devolucion_incompleta'
    ));

ALTER TABLE app_solicitudes_consignacion
    ADD COLUMN ubicacion_fisica varchar(350);

ALTER TABLE app_consignacion_devoluciones
    DROP CONSTRAINT app_consignacion_devoluciones_estado_check;

ALTER TABLE app_consignacion_devoluciones
    ADD COLUMN modalidad varchar(10) CHECK (modalidad IN ('envio', 'retiro')),
    ADD COLUMN direccion varchar(250),
    ADD COLUMN piso varchar(30),
    ADD COLUMN codigo_postal varchar(20),
    ADD COLUMN localidad varchar(120),
    ADD COLUMN provincia varchar(120),
    ADD COLUMN telefono_contacto varchar(50),
    ADD CONSTRAINT chk_app_consignacion_devolucion_estado CHECK (estado IN (
        'pendiente_decision', 'pendiente_retiro', 'pendiente_pago', 'pendiente_entrega',
        'completada', 'pagada', 'enviada', 'retirada', 'incumplida'
    ));

ALTER TABLE app_pagos
    DROP CONSTRAINT chk_app_pagos_referencia;

ALTER TABLE app_pagos
    ADD COLUMN consignacion_devolucion_id bigint REFERENCES app_consignacion_devoluciones (id),
    ADD CONSTRAINT chk_app_pagos_referencia CHECK (
        compra_id IS NOT NULL OR multa_id IS NOT NULL OR consignacion_devolucion_id IS NOT NULL
    );
