-- D-008: cursurile zilnice BNR (import XML), doar pentru monedele facturilor: EUR și USD.
-- Cursul are cel mult 4 zecimale, ca în XML-ul BNR (D-031).
create table bnr_rate (
    rate_date date    not null,
    currency  char(3) not null check (currency in ('EUR', 'USD')),
    rate      numeric not null check (rate > 0 and scale(rate) <= 4),
    primary key (rate_date, currency)
);

-- D-012: Admin-ul configurează șablonul și regula reminder-elor. Un singur rând (id = 1).
-- Valorile implicite (zile, text, server de email) așteaptă Q-019, deci rândul pornește gol.
create table reminder_settings (
    id                smallint     primary key default 1 check (id = 1),
    days_after_due    integer      check (days_after_due >= 0),
    repeat_every_days integer      check (repeat_every_days >= 1),
    email_subject     varchar(200),
    email_body        text
);

insert into reminder_settings (id) values (1);

-- Istoricul reminder-elor trimise, doar pentru facturi (nu proforme, nu storno).
-- AUTO = trimis de job; MANUAL = „Trimite reminder”, cu utilizatorul care l-a trimis.
create table reminder_log (
    id               bigint       generated always as identity primary key,
    invoice_id       bigint       not null,
    invoice_doc_type varchar(8)   not null default 'FACTURA' check (invoice_doc_type = 'FACTURA'),
    sent_at          timestamptz  not null default now(),
    recipient_email  varchar(254) not null,
    kind             varchar(6)   not null check (kind in ('AUTO', 'MANUAL')),
    sent_by          bigint       references app_user (id),
    constraint reminder_log_invoice_fk foreign key (invoice_id, invoice_doc_type) references invoice (id, doc_type),
    constraint reminder_log_sender check ((kind = 'MANUAL') = (sent_by is not null))
);
