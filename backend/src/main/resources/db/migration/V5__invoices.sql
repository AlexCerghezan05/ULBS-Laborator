-- Facturi, storno și proforme într-un singur tabel. Fără ciorne (D-018): o FACTURA sau un
-- STORNO din tabel e emis, deci nemodificabil (trigger-ele de mai jos).
--
-- Numerotarea (D-005):
-- - number vine din allocate_number(series_id, fiscal_year), în tranzacția insert-ului;
-- - unique (series_id, fiscal_year, number) e plasa de siguranță contra duplicatelor;
-- - series_type + FK compus: FACTURA și STORNO stau pe o serie FACTURA, PROFORMA pe o serie
--   PROFORMA, deci proforma nu consumă număr din seria de facturi;
-- - storno-ul referă o FACTURA din aceeași serie (FK compus pe id, doc_type, series_id).
--
-- Datele clientului și ale furnizorului se copiază la emitere (D-027); issued_by (D-030).
-- Sumele au cel mult 2 zecimale, cursul cel mult 4, fără rotunjire tăcută (D-031).
-- Moneda și cursul (D-008): la EUR/USD, cursul e din ziua bancară precedentă sau mai vechi,
-- niciodată din ziua emiterii; fallback_rate = „curs de rezervă”.
create table invoice (
    id                   bigint       generated always as identity primary key,
    doc_type             varchar(8)   not null check (doc_type in ('FACTURA', 'STORNO', 'PROFORMA')),
    series_id            bigint       not null,
    series_type          varchar(8)   not null,
    issue_date           date         not null,
    fiscal_year          smallint     not null generated always as (extract(year from issue_date)::smallint) stored,
    number               integer      not null check (number >= 1),
    due_date             date         not null,
    original_invoice_id  bigint,
    original_doc_type    varchar(8)   check (original_doc_type = 'FACTURA'),

    client_id            bigint       not null references client (id),
    client_type          varchar(3)   not null check (client_type in ('PJ', 'PFA', 'PF')),
    client_name          varchar(200) not null,
    client_cif           varchar(12),
    client_reg_com       varchar(32),
    client_cnp           varchar(13),
    client_address       varchar(300) not null,
    client_city          varchar(100) not null,
    client_county_code   varchar(6)   not null,

    supplier_name        varchar(200) not null,
    supplier_cif         varchar(12)  not null,
    supplier_reg_com     varchar(32)  not null,
    supplier_address     varchar(300) not null,
    supplier_city        varchar(100) not null,
    supplier_county_code varchar(6)   not null,
    supplier_iban        varchar(34),
    supplier_bank        varchar(100),

    currency             char(3)      not null check (currency in ('RON', 'EUR', 'USD')),
    exchange_rate        numeric      check (exchange_rate > 0 and scale(exchange_rate) <= 4),
    exchange_rate_date   date,
    fallback_rate        boolean      not null default false,

    net_total            numeric      not null check (scale(net_total) <= 2),
    vat_total            numeric      not null check (scale(vat_total) <= 2),
    total                numeric      not null check (scale(total) <= 2),
    net_total_ron        numeric      not null check (scale(net_total_ron) <= 2),
    vat_total_ron        numeric      not null check (scale(vat_total_ron) <= 2),
    total_ron            numeric      not null check (scale(total_ron) <= 2),

    issued_by            bigint       not null references app_user (id),
    created_at           timestamptz  not null default now(),

    constraint invoice_series_type check (
        (doc_type in ('FACTURA', 'STORNO') and series_type = 'FACTURA')
        or (doc_type = 'PROFORMA' and series_type = 'PROFORMA')),
    constraint invoice_series_fk foreign key (series_id, series_type) references series (id, doc_type),
    constraint invoice_number_unique unique (series_id, fiscal_year, number),
    constraint invoice_id_type_unique unique (id, doc_type),
    constraint invoice_id_type_series_unique unique (id, doc_type, series_id),
    constraint invoice_storno_has_original check (
        (doc_type = 'STORNO') = (original_invoice_id is not null)
        and (original_invoice_id is null) = (original_doc_type is null)),
    constraint invoice_storno_same_series foreign key (original_invoice_id, original_doc_type, series_id)
        references invoice (id, doc_type, series_id),
    constraint invoice_due_date check (due_date >= issue_date),
    constraint invoice_rate_by_currency check (
        (currency = 'RON' and exchange_rate is null and exchange_rate_date is null and not fallback_rate)
        or (currency <> 'RON' and exchange_rate is not null and exchange_rate_date < issue_date)),
    constraint invoice_totals check (total = net_total + vat_total and total_ron = net_total_ron + vat_total_ron),
    constraint invoice_ron_equivalent check (
        currency <> 'RON' or (net_total_ron = net_total and vat_total_ron = vat_total and total_ron = total)),
    constraint invoice_total_sign check (case when doc_type = 'STORNO' then total <= 0 else total >= 0 end)
);

-- D-021: linia poate veni din catalog (product_id) sau poate fi scrisă liber; valorile sunt
-- copiate pe linie. D-013: discount procentual pe linie. D-020: cota din vat_rate.
-- quantity e negativă pe storno. net_amount e valoarea liniei după discount.
create table invoice_line (
    id               bigint       generated always as identity primary key,
    invoice_id       bigint       not null references invoice (id),
    position         smallint     not null check (position >= 1),
    product_id       bigint       references product (id),
    name             varchar(300) not null,
    unit             varchar(16)  not null,
    quantity         numeric      not null check (quantity <> 0 and scale(quantity) <= 4),
    unit_price       numeric      not null check (unit_price >= 0 and scale(unit_price) <= 4),
    discount_percent numeric      not null default 0
                                  check (discount_percent between 0 and 100 and scale(discount_percent) <= 2),
    vat_rate_code    varchar(8)   not null references vat_rate (code),
    net_amount       numeric      not null check (scale(net_amount) <= 2),
    unique (invoice_id, position)
);

-- TVA-ul pe cote, calculat la emitere după regula de rotunjire a firmei (D-007), pentru XML
-- și rapoarte.
create table invoice_vat_summary (
    invoice_id     bigint     not null references invoice (id),
    vat_rate_code  varchar(8) not null references vat_rate (code),
    taxable_amount numeric    not null check (scale(taxable_amount) <= 2),
    vat_amount     numeric    not null check (scale(vat_amount) <= 2),
    primary key (invoice_id, vat_rate_code)
);

-- D-015: mențiunile legale, generate automat și copiate pe factură la emitere.
create table invoice_legal_note (
    invoice_id bigint       not null references invoice (id),
    position   smallint     not null check (position >= 1),
    text       varchar(500) not null,
    primary key (invoice_id, position)
);

-- Factura emisă nu se modifică și nu se șterge (CONTEXT.md „Factură emisă”). Excepție:
-- tranzacția care a creat-o, ca să poată adăuga liniile, TVA-ul pe cote și mențiunile.
-- Proforma nu e blocată aici (regulile ei de editare nu sunt stabilite).
create function invoice_is_locked(p_invoice_id bigint) returns boolean
language sql as $$
    select coalesce((
        select doc_type in ('FACTURA', 'STORNO') and not (xmin = pg_current_xact_id()::xid)
        from invoice where id = p_invoice_id), false);
$$;

create function forbid_issued_invoice_change() returns trigger
language plpgsql as $$
begin
    if invoice_is_locked(old.id) then
        raise exception 'Factura % e emisă: nu se modifică și nu se șterge, doar se stornează.', old.id
            using errcode = 'restrict_violation';
    end if;
    if tg_op = 'DELETE' then
        return old;
    end if;
    return new;
end;
$$;

create function forbid_issued_invoice_child_change() returns trigger
language plpgsql as $$
begin
    if tg_op <> 'INSERT' then
        if invoice_is_locked(old.invoice_id) then
            raise exception 'Factura % e emisă: % pe %: nu e permis.', old.invoice_id, tg_op, tg_table_name
                using errcode = 'restrict_violation';
        end if;
    end if;
    if tg_op <> 'DELETE' then
        if invoice_is_locked(new.invoice_id) then
            raise exception 'Factura % e emisă: % pe %: nu e permis.', new.invoice_id, tg_op, tg_table_name
                using errcode = 'restrict_violation';
        end if;
        return new;
    end if;
    return old;
end;
$$;

create trigger invoice_immutable before update or delete on invoice
    for each row execute function forbid_issued_invoice_change();
create trigger invoice_line_immutable before insert or update or delete on invoice_line
    for each row execute function forbid_issued_invoice_child_change();
create trigger invoice_vat_summary_immutable before insert or update or delete on invoice_vat_summary
    for each row execute function forbid_issued_invoice_child_change();
create trigger invoice_legal_note_immutable before insert or update or delete on invoice_legal_note
    for each row execute function forbid_issued_invoice_child_change();
