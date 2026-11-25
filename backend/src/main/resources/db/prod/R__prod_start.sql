-- Pornirea în producție (D-044, D-045, D-046): rulează doar cu locația db/prod (render.yaml),
-- niciodată împreună cu db/seed. Ca R__seed.sql: migrare repetabilă, fiecare pas idempotent.
--
-- Datele reale anonimizate din S1 (SAMPLES.md, D-026): firma, clientul, factura Aa0060 și
-- chitanța ei Aa 0021, deci contoarele Aa/2026 rămân la 60 (facturi) și 21 (chitanțe) și
-- următoarele numere sunt Aa0061 și Aa 0022 (Q-039).
--
-- Pe Render aplicația nu e superuser, ci proprietarul tabelelor (Q-038). Numerele 60 și 21 și
-- contoarele sunt alese, nu alocate, deci trigger-ele care le-ar refuza (numerotarea și garda
-- contorului) se opresc cu ALTER TABLE ... DISABLE TRIGGER doar în acest bloc. Flyway rulează
-- migrarea într-o tranzacție: la orice eroare, oprirea trigger-elor se anulează odată cu restul.
--
-- Conturile vin din variabile secrete Render (placeholder-ele Flyway din application.properties),
-- doar ca hash BCrypt, niciodată parola. Admin e obligatoriu; Operator și Contabil sunt opționale
-- (amândouă variabilele lipsă = contul nu se creează). Un cont existent nu se schimbă.
do $prod$
declare
    v_accounts text[][] := array[
        ['ADMIN',    'Administrator', 'P21_ADMIN',    '${admin_username}',    '${admin_password_hash}'],
        ['OPERATOR', 'Operator',      'P21_OPERATOR', '${operator_username}', '${operator_password_hash}'],
        ['CONTABIL', 'Contabil',      'P21_CONTABIL', '${contabil_username}', '${contabil_password_hash}']];
    v_admin   bigint;
    v_invoice bigint;
    v_payment bigint;
    v_series  bigint;
    v_receipt_series bigint;
    v_client  bigint;
    i int;
begin
    for i in 1 .. array_length(v_accounts, 1) loop
        if v_accounts[i][4] = '' and v_accounts[i][5] = '' and v_accounts[i][1] <> 'ADMIN' then
            continue;
        end if;
        if v_accounts[i][4] = '' then
            raise exception 'Lipsește %_USERNAME.', v_accounts[i][3];
        end if;
        if v_accounts[i][5] = '' then
            raise exception 'Lipsește %_PASSWORD_HASH.', v_accounts[i][3];
        end if;
        if v_accounts[i][4] !~ '^[A-Za-z0-9._-]{1,64}$' then
            raise exception '%_USERNAME: doar litere, cifre, punct, cratimă și underscore (cel mult 64).', v_accounts[i][3];
        end if;
        if v_accounts[i][5] !~ '^\$2[aby]\$[0-9]{2}\$[./A-Za-z0-9]{53}$' then
            raise exception '%_PASSWORD_HASH nu e un hash BCrypt (puneți hash-ul, nu parola).', v_accounts[i][3];
        end if;
        insert into app_user (username, full_name, password_hash, role)
        values (v_accounts[i][4], v_accounts[i][2], v_accounts[i][5], v_accounts[i][1])
        on conflict (username) do nothing;
    end loop;
    v_admin := (select id from app_user where username = v_accounts[1][4]);

    -- Firma furnizor (D-003) și clientul din S1, ca în R__seed.sql. Regula de rotunjire: Q-007.
    insert into company (id, name, cif, reg_com, address, city, county_code, vat_payer, vat_on_collection)
    values (1, 'FURNIZOR EXEMPLU S.R.L.', 'RO12345674', 'J2023000123456',
            'Str. Exemplului, Nr.1, Ap.1', 'Sibiu', 'RO-SB', true, false)
    on conflict (id) do nothing;

    insert into client (client_type, name, cif, reg_com, address, city, county_code)
    select 'PFA', 'CLIENT EXEMPLU PERSOANA FIZICA AUTORIZATA', '76543210', 'F2022000654321',
           'Str. Fictiva, Nr.2, Sc.1, Et.P, Ap.2', 'Sibiu', 'RO-SB'
    where not exists (select 1 from client where cif = '76543210');

    -- Seria Aa pentru facturi și pentru chitanțe (D-005).
    insert into series (name, doc_type, number_width) values ('Aa', 'FACTURA', 4), ('Aa', 'CHITANTA', 4)
    on conflict (name, doc_type) do nothing;
    v_series := (select id from series where name = 'Aa' and doc_type = 'FACTURA');
    v_receipt_series := (select id from series where name = 'Aa' and doc_type = 'CHITANTA');

    -- S1 și chitanța ei intră o singură dată și doar cât seriile n-au emis nimic în aplicație:
    -- un contor existent (o factură sau chitanță emisă deja) nu se suprascrie.
    if exists (select 1 from series_counter where series_id in (v_series, v_receipt_series)) then
        return;
    end if;

    v_client := (select id from client where cif = '76543210' order by id limit 1);

    alter table series_counter disable trigger series_counter_guard;
    alter table invoice disable trigger invoice_number;
    alter table receipt disable trigger receipt_number;

    insert into series_counter (series_id, year, last_number) values (v_series, 2026, 60), (v_receipt_series, 2026, 21);

    -- Factura reală S1 (D-026): Aa0060, 10.11.2026, 297.52 + 62.48 = 360.00 RON. Emitentul e
    -- Admin-ul (D-030): în producție nu există Operatorul de test.
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
            297.52, 62.48, 360.00, 297.52, 62.48, 360.00, v_admin)
    returning id into v_invoice;

    -- U.M. „1”, ca pe factura reală (Q-015). Procentul și categoria TVA le pune trigger-ul (D-027).
    insert into invoice_line (invoice_id, position, name, unit, quantity, unit_price, discount_percent,
                              vat_rate_code, net_amount)
    values (v_invoice, 1, 'Inchiriere masina 7 zile', '1', 1, 297.52, 0, 'S21', 297.52);

    insert into invoice_vat_summary (invoice_id, vat_rate_code, taxable_amount, vat_amount)
    values (v_invoice, 'S21', 297.52, 62.48);

    insert into invoice_legal_note (invoice_id, position, text)
    values (v_invoice, 1,
            'Factura este valabila fara semnatura si stampila, conform art. 319 alin. 29 din legea 227/2015.');

    -- Încasarea în numerar a lui S1 și chitanța ei, Aa 0021, din aceeași zi (termenul de plată, D-016).
    insert into payment (invoice_id, paid_on, amount, method)
    values (v_invoice, date '2026-11-10', 360.00, 'NUMERAR')
    returning id into v_payment;

    insert into receipt (payment_id, series_id, issue_date, number)
    values (v_payment, v_receipt_series, date '2026-11-10', 21);

    alter table receipt enable trigger receipt_number;
    alter table invoice enable trigger invoice_number;
    alter table series_counter enable trigger series_counter_guard;
end;
$prod$;
