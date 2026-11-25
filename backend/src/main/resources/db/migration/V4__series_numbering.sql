-- D-005: seriile au un tip de document; storno-ul nu are tip propriu (folosește seria facturii).
-- number_width: câte cifre are numărul, completat cu zerouri (Aa0060). După 9999: Q-008.
-- unique (id, doc_type): ținta FK-urilor compuse care cer o serie de un anumit tip.
create table series (
    id           bigint      generated always as identity primary key,
    name         varchar(16) not null check (name <> ''),
    doc_type     varchar(8)  not null check (doc_type in ('FACTURA', 'PROFORMA', 'CHITANTA')),
    number_width smallint    not null default 4 check (number_width between 1 and 10),
    unique (name, doc_type),
    unique (id, doc_type)
);

-- D-005: contorul e pe (serie, an); anul vine din data emiterii, deci numerele se resetează
-- la 1 în fiecare an.
create table series_counter (
    series_id   bigint   not null references series (id),
    year        smallint not null check (year between 2000 and 2999),
    last_number integer  not null check (last_number >= 1),
    primary key (series_id, year)
);

-- Alocă următorul număr. Se apelează în aceeași tranzacție cu insert-ul documentului:
-- - fără duplicate: upsert-ul ține lock pe rândul contorului până la commit, deci o emitere
--   simultană pe aceeași serie și același an așteaptă și vede valoarea mărită;
-- - fără goluri: dacă tranzacția face rollback, se anulează și mărirea contorului.
create function allocate_number(p_series_id bigint, p_year integer) returns integer
language sql as $$
    insert into series_counter (series_id, year, last_number)
    values (p_series_id, p_year, 1)
    on conflict (series_id, year) do update set last_number = series_counter.last_number + 1
    returning last_number;
$$;
