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

-- Toate funcțiile au search_path fixat, cu pg_temp ultimul: altfel un tabel temporar cu
-- același nume (ex. „invoice”) ar fi citit în locul celui real și ar ocoli verificările.

-- Alocă următorul număr. O apelează doar trigger-ul de numerotare la INSERT-ul documentului
-- (assign_document_number), deci în aceeași tranzacție cu documentul:
-- - fără duplicate: upsert-ul ține lock pe rândul contorului până la commit, deci o emitere
--   simultană pe aceeași serie și același an așteaptă și vede valoarea mărită;
-- - fără goluri: dacă tranzacția face rollback, se anulează și mărirea contorului.
create function allocate_number(p_series_id bigint, p_year integer) returns integer
language sql
set search_path = pg_catalog, public, pg_temp
as $$
    insert into series_counter (series_id, year, last_number)
    values (p_series_id, p_year, 1)
    on conflict (series_id, year) do update set last_number = series_counter.last_number + 1
    returning last_number;
$$;

-- Numărul documentului (factură, storno, proformă, chitanță) îl pune baza de date la INSERT;
-- o valoare dată de apelant se ignoră. Trigger-ele se atașează în V5 și V6.
create function assign_document_number() returns trigger
language plpgsql
set search_path = pg_catalog, public, pg_temp
as $$
begin
    new.number := allocate_number(new.series_id, extract(year from new.issue_date)::integer);
    return new;
end;
$$;

-- Contorul se schimbă doar din trigger-ul de numerotare (pg_trigger_depth() >= 2: trigger-ul
-- contorului, pornit din cel al documentului). Un INSERT, UPDATE sau apel direct la
-- allocate_number ar lăsa goluri sau ar bloca seria; un DELETE ar refolosi numere.
create function forbid_direct_counter_change() returns trigger
language plpgsql
set search_path = pg_catalog, public, pg_temp
as $$
begin
    if tg_op = 'DELETE' or pg_trigger_depth() < 2 then
        raise exception 'Contorul seriei % se schimbă doar prin emiterea unui document.', coalesce(new.series_id, old.series_id)
            using errcode = 'restrict_violation';
    end if;
    return new;
end;
$$;

create trigger series_counter_guard before insert or update or delete on series_counter
    for each row execute function forbid_direct_counter_change();

-- O serie folosită (are contor, deci cel puțin un document numerotat) nu-și mai schimbă
-- numele, tipul sau lățimea numărului: ar schimba retroactiv numerele documentelor emise.
create function forbid_used_series_change() returns trigger
language plpgsql
set search_path = pg_catalog, public, pg_temp
as $$
begin
    if (new.name, new.doc_type, new.number_width) is distinct from (old.name, old.doc_type, old.number_width)
       and exists (select 1 from series_counter where series_id = old.id) then
        raise exception 'Seria % are documente emise: numele, tipul și formatul nu se mai schimbă.', old.id
            using errcode = 'restrict_violation';
    end if;
    return new;
end;
$$;

create trigger series_used_guard before update on series
    for each row execute function forbid_used_series_change();

-- TRUNCATE ocolește trigger-ele pe rânduri; tabelele cu documente emise și contorul îl refuză.
create function forbid_truncate() returns trigger
language plpgsql
set search_path = pg_catalog, public, pg_temp
as $$
begin
    raise exception 'TRUNCATE pe % nu e permis.', tg_table_name
        using errcode = 'restrict_violation';
end;
$$;

create trigger series_counter_no_truncate before truncate on series_counter
    for each statement execute function forbid_truncate();
