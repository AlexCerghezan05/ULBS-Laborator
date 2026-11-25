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
-- numărul vine din allocate_number, ca la facturi (D-005). Suma e cea a încasării.
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

-- D-033: chitanța emisă nu se modifică și nu se șterge (numerotare fără goluri).
create function forbid_receipt_change() returns trigger
language plpgsql as $$
begin
    raise exception 'Chitanța % e emisă: nu se modifică și nu se șterge.', old.id
        using errcode = 'restrict_violation';
end;
$$;

create trigger receipt_immutable before update or delete on receipt
    for each row execute function forbid_receipt_change();
