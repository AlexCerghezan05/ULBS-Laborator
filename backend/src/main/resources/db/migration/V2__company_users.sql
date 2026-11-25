-- D-003: o singură firmă furnizor pe instalare; rândul are mereu id = 1.
-- county_code: județul în format ISO 3166-2:RO (ex. RO-SB), cerut de RO_CIUS (D-032).
-- rounding_rule (D-007): LINE = TVA pe linie, TOTAL = TVA pe total, GROSS = pornind de la
-- prețul cu TVA. Null până la răspunsul la Q-007.
create table company (
    id                smallint     primary key default 1 check (id = 1),
    name              varchar(200) not null,
    cif               varchar(12)  not null,
    reg_com           varchar(32)  not null,
    address           varchar(300) not null,
    city              varchar(100) not null,
    county_code       varchar(6)   not null check (county_code ~ '^RO-[A-Z]{1,2}$'),
    iban              varchar(34),
    bank              varchar(100),
    vat_payer         boolean      not null,
    vat_on_collection boolean      not null,
    rounding_rule     varchar(8)   check (rounding_rule in ('LINE', 'TOTAL', 'GROSS'))
);

-- D-012: trei roluri; un utilizator are un singur rol (D-029).
-- password_hash: BCrypt; null = contul nu se poate loga încă.
create table app_user (
    id            bigint       generated always as identity primary key,
    username      varchar(64)  not null unique,
    full_name     varchar(200) not null,
    password_hash varchar(100),
    role          varchar(8)   not null check (role in ('ADMIN', 'OPERATOR', 'CONTABIL')),
    active        boolean      not null default true
);
