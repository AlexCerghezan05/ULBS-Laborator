-- Încasări: parțiale și multiple pe aceeași factură, numerar sau transfer (docs/scope.md).
-- Doar pe o FACTURA (FK compus pe id, doc_type). Suma e în moneda facturii (D-028).
-- Plățile peste totalul facturii: Q-036.
create table payment (
    id               bigint      generated always as identity primary key,
    invoice_id       bigint      not null,
    invoice_doc_type varchar(8)  not null default 'FACTURA' check (invoice_doc_type = 'FACTURA'),
    paid_on          date        not null,
    amount           numeric     not null check (amount > 0 and scale(amount) <= 2),
    method           varchar(8)  not null check (method in ('NUMERAR', 'TRANSFER')),
    created_at       timestamptz not null default now(),
    constraint payment_invoice_fk foreign key (invoice_id, invoice_doc_type) references invoice (id, doc_type),
    constraint payment_id_method_unique unique (id, method)
);

-- Chitanța: o încasare în numerar are cel mult o chitanță (Q-012). Seria e de tip CHITANTA,
-- numărul îl pune trigger-ul la INSERT, ca la facturi (D-005). Suma, factura și data sunt cele
-- ale încasării, care devine nemodificabilă odată ce are chitanță (D-033).
create table receipt (
    id             bigint     generated always as identity primary key,
    payment_id     bigint     not null unique,
    payment_method varchar(8) not null default 'NUMERAR' check (payment_method = 'NUMERAR'),
    series_id      bigint     not null,
    series_type    varchar(8) not null default 'CHITANTA' check (series_type = 'CHITANTA'),
    issue_date     date       not null,
    fiscal_year    smallint   not null generated always as (extract(year from issue_date)::smallint) stored,
    number         integer    not null check (number >= 1),
    constraint receipt_payment_fk foreign key (payment_id, payment_method) references payment (id, method),
    constraint receipt_series_fk foreign key (series_id, series_type) references series (id, doc_type),
    constraint receipt_number_unique unique (series_id, fiscal_year, number)
);

create trigger receipt_number before insert on receipt
    for each row execute function assign_document_number();

-- D-033: chitanța emisă nu se modifică și nu se șterge (numerotare fără goluri).
create function forbid_receipt_change() returns trigger
language plpgsql
set search_path = pg_catalog, public, pg_temp
as $$
begin
    raise exception 'Chitanța % e emisă: nu se modifică și nu se șterge.', old.id
        using errcode = 'restrict_violation';
end;
$$;

create trigger receipt_immutable before update or delete on receipt
    for each row execute function forbid_receipt_change();

-- D-033: încasarea care are chitanță nu se modifică și nu se șterge, altfel chitanța ar arăta
-- retroactiv altă sumă, altă factură sau altă dată.
create function forbid_receipted_payment_change() returns trigger
language plpgsql
set search_path = pg_catalog, public, pg_temp
as $$
begin
    if exists (select 1 from receipt where payment_id = old.id) then
        raise exception 'Încasarea % are chitanță: nu se modifică și nu se șterge.', old.id
            using errcode = 'restrict_violation';
    end if;
    if tg_op = 'DELETE' then
        return old;
    end if;
    return new;
end;
$$;

create trigger payment_receipted_immutable before update or delete on payment
    for each row execute function forbid_receipted_payment_change();

create trigger payment_no_truncate before truncate on payment
    for each statement execute function forbid_truncate();
create trigger receipt_no_truncate before truncate on receipt
    for each statement execute function forbid_truncate();
