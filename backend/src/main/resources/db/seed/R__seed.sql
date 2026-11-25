-- Seed: datele de pornire, doar local și în teste (locația db/seed; la deploy se poate scoate
-- din SPRING_FLYWAY_LOCATIONS). Datele reale sunt anonimizate ca în SAMPLES.md.
-- Migrare repetabilă (R__): rulează după toate migrările V și din nou doar când se schimbă,
-- deci fiecare insert e idempotent. Un V100 ar fi blocat migrările V8+ care vin după.
--
-- Parola utilizatorilor de test nu e în repo: hash-ul BCrypt vine din .env
-- (P21_SEED_PASSWORD_HASH, placeholder Flyway). Gol = conturile nu se pot loga.

-- Firma furnizor (D-003), din S1. Regula de rotunjire: null până la Q-007.
insert into company (id, name, cif, reg_com, address, city, county_code, vat_payer, vat_on_collection)
values (1, 'FURNIZOR EXEMPLU S.R.L.', 'RO12345674', 'J2023000123456',
        'Str. Exemplului, Nr.1, Ap.1', 'Sibiu', 'RO-SB', true, false)
on conflict (id) do nothing;

-- Câte un utilizator de test pe rol (D-012). „Ion Ionescu” e numele anonimizat de pe S1.
insert into app_user (username, full_name, password_hash, role) values
    ('admin',    'Admin Test',    nullif('${seed_password_hash}', ''), 'ADMIN'),
    ('operator', 'Ion Ionescu',   nullif('${seed_password_hash}', ''), 'OPERATOR'),
    ('contabil', 'Contabil Test', nullif('${seed_password_hash}', ''), 'CONTABIL')
on conflict (username) do nothing;

-- Clientul S1: PFA, CIF fără RO, Reg. com. cu F.
insert into client (client_type, name, cif, reg_com, address, city, county_code)
select 'PFA', 'CLIENT EXEMPLU PERSOANA FIZICA AUTORIZATA', '76543210', 'F2022000654321',
       'Str. Fictiva, Nr.2, Sc.1, Et.P, Ap.2', 'Sibiu', 'RO-SB'
where not exists (select 1 from client where cif = '76543210');

-- Seria Aa (D-005). Fără serie de proformă (D-017).
insert into series (name, doc_type, number_width) values ('Aa', 'FACTURA', 4)
on conflict (name, doc_type) do nothing;

-- Factura reală S1 (D-026): Aa0060, 10.11.2026, 297.52 + 62.48 = 360.00 RON. Contorul Aa/2026
-- pornește de la 60, deci următoarea factură e Aa0061.
do $$
declare
    v_series  bigint := (select id from series where name = 'Aa' and doc_type = 'FACTURA');
    v_client  bigint := (select id from client where cif = '76543210' order by id limit 1);
    v_user    bigint := (select id from app_user where username = 'operator');
    v_invoice bigint;
begin
    if exists (select 1 from invoice where series_id = v_series and fiscal_year = 2026 and number = 60) then
        return;
    end if;

    insert into series_counter (series_id, year, last_number) values (v_series, 2026, 60)
    on conflict (series_id, year) do nothing;

    insert into invoice (doc_type, series_id, series_type, issue_date, number, due_date,
                         client_id, client_type, client_name, client_cif, client_reg_com,
                         client_address, client_city, client_county_code,
                         supplier_name, supplier_cif, supplier_reg_com, supplier_address, supplier_city,
                         supplier_county_code, currency,
                         net_total, vat_total, total, net_total_ron, vat_total_ron, total_ron, issued_by)
    values ('FACTURA', v_series, 'FACTURA', date '2026-11-10', 60, date '2026-11-10',
            v_client, 'PFA', 'CLIENT EXEMPLU PERSOANA FIZICA AUTORIZATA', '76543210', 'F2022000654321',
            'Str. Fictiva, Nr.2, Sc.1, Et.P, Ap.2', 'Sibiu', 'RO-SB',
            'FURNIZOR EXEMPLU S.R.L.', 'RO12345674', 'J2023000123456', 'Str. Exemplului, Nr.1, Ap.1', 'Sibiu',
            'RO-SB', 'RON',
            297.52, 62.48, 360.00, 297.52, 62.48, 360.00, v_user)
    returning id into v_invoice;

    -- U.M. „1”, ca pe factura reală (Q-015).
    insert into invoice_line (invoice_id, position, name, unit, quantity, unit_price, discount_percent,
                              vat_rate_code, net_amount)
    values (v_invoice, 1, 'Inchiriere masina 7 zile', '1', 1, 297.52, 0, 'S21', 297.52);

    insert into invoice_vat_summary (invoice_id, vat_rate_code, taxable_amount, vat_amount)
    values (v_invoice, 'S21', 297.52, 62.48);

    insert into invoice_legal_note (invoice_id, position, text)
    values (v_invoice, 1,
            'Factura este valabila fara semnatura si stampila, conform art. 319 alin. 29 din legea 227/2015.');
end;
$$;
