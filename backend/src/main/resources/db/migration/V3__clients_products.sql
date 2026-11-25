-- D-010: clienți doar din România (fără coloană de țară, mereu RO).
-- PJ = persoană juridică, PFA, PF = persoană fizică. PJ și PFA au CIF; PF are CNP opțional
-- (D-019; fără CNP, XML-ul primește 13 zerouri). Email opțional (D-022).
-- county_code: ISO 3166-2:RO, obligatoriu (D-032).
create table client (
    id          bigint       generated always as identity primary key,
    client_type varchar(3)   not null check (client_type in ('PJ', 'PFA', 'PF')),
    name        varchar(200) not null,
    cif         varchar(12)  check (cif ~ '^(RO)?[0-9]{2,10}$'),
    reg_com     varchar(32),
    cnp         varchar(13)  check (cnp ~ '^[0-9]{13}$'),
    address     varchar(300) not null,
    city        varchar(100) not null,
    county_code varchar(6)   not null check (county_code ~ '^RO-[A-Z]{1,2}$'),
    email       varchar(254) check (email ~ '^[^@[:space:]]+@[^@[:space:]]+$'),
    active      boolean      not null default true,
    constraint client_cif_by_type check ((client_type in ('PJ', 'PFA')) = (cif is not null)),
    constraint client_cnp_only_pf check (cnp is null or client_type = 'PF')
);

-- D-021: catalogul de produse/servicii; linia de factură îl poate folosi sau nu.
-- unit_price: fără TVA (D-013), cel mult 4 zecimale, fără rotunjire tăcută (D-031).
create table product (
    id            bigint       generated always as identity primary key,
    name          varchar(300) not null,
    unit          varchar(16)  not null,
    unit_price    numeric      not null check (unit_price >= 0 and scale(unit_price) <= 4),
    vat_rate_code varchar(8)   not null references vat_rate (code),
    active        boolean      not null default true
);
