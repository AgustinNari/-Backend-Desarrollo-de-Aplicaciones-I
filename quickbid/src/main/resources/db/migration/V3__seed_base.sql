-- Small deterministic baseline. Demo scenarios will be added separately.

INSERT INTO paises (numero, nombre, "nombreCorto", capital, nacionalidad, idiomas) VALUES
    (32, 'Argentina', 'AR', 'Buenos Aires', 'argentina', 'español'),
    (76, 'Brasil', 'BR', 'Brasilia', 'brasileña', 'portugués'),
    (152, 'Chile', 'CL', 'Santiago', 'chilena', 'español'),
    (858, 'Uruguay', 'UY', 'Montevideo', 'uruguaya', 'español'),
    (840, 'Estados Unidos', 'US', 'Washington D.C.', 'estadounidense', 'inglés');

INSERT INTO empleados (identificador, cargo, sector) VALUES
    (1001, 'Verificador de usuarios', NULL),
    (1002, 'Revisor de consignaciones', NULL),
    (1003, 'Operador de pagos', NULL),
    (1004, 'Operador de subastas', NULL);

INSERT INTO sectores ("nombreSector", "codigoSector", "responsableSector") VALUES
    ('Validación de usuarios', 'USR', 1001),
    ('Consignaciones', 'CON', 1002),
    ('Pagos', 'PAY', 1003),
    ('Subastas', 'AUC', 1004);

INSERT INTO personas (documento, nombre, direccion, estado)
VALUES ('EMPRESA-SUB-001', 'Martillero Demo', 'Casa central QuickBid', 'activo');

INSERT INTO subastadores (identificador, matricula, region)
SELECT identificador, 'MAT-DEMO-001', 'Buenos Aires'
FROM personas
WHERE documento = 'EMPRESA-SUB-001';

INSERT INTO app_categoria_puntos (categoria, puntos_minimos, orden) VALUES
    ('comun', 0, 1),
    ('especial', 250, 2),
    ('plata', 700, 3),
    ('oro', 1500, 4),
    ('platino', 3000, 5);
