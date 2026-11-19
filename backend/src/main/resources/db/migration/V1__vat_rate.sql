-- D-020: cotele de TVA stau doar aici. O cotă nouă = o migrare nouă, fără ecran.
-- category_code: codul de categorie TVA din XML-ul e-Factura (UNCL5305, RO_CIUS):
-- S = cota standard, E = scutit, AE = taxare inversă.
-- Motivul de scutire pentru E și AE nu e aici încă (Q-034).
create table vat_rate (
    code          varchar(8)   primary key,
    label         varchar(64)  not null,
    percent       numeric(5,2) not null,
    category_code varchar(2)   not null
);

insert into vat_rate (code, label, percent, category_code) values
    ('S21', '21%',            21.00, 'S'),
    ('S11', '11%',            11.00, 'S'),
    ('E',   'Scutit',          0.00, 'E'),
    ('AE',  'Taxare inversă',  0.00, 'AE');
