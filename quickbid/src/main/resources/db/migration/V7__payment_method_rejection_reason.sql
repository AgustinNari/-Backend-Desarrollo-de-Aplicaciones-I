-- App-owned traceability for manual payment method rejection.

ALTER TABLE app_medios_pago
    ADD COLUMN motivo_rechazo varchar(500);
