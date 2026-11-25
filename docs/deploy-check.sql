-- Verificarea bazei de pe Render după primul deploy (docs/deploy.md, pasul 7; Q-038).
-- Doar citește; singura încercare de schimbare (session_replication_role) trebuie să fie refuzată.
\set ON_ERROR_STOP off
\echo '1. Utilizatorul dat de Render și drepturile lui'
select current_user, r.rolsuper, r.rolcreaterole, r.rolcreatedb, r.rolbypassrls, r.rolreplication
from pg_roles r where r.rolname = current_user;
\echo '2. Proprietarul bazei și al schemei public'
select pg_get_userbyid(d.datdba) as db_owner, (select pg_get_userbyid(nspowner) from pg_namespace where nspname = 'public') as public_owner
from pg_database d where d.datname = current_database();
\echo '3. Proprietarii tabelelor'
select tableowner, count(*) as tables from pg_tables where schemaname = 'public' group by tableowner;
\echo '4. Alte roluri cu login (fără cele interne pg_*)'
select rolname, rolsuper, rolcreaterole from pg_roles where rolcanlogin and rolname !~ '^pg_' order by rolname;
\echo '5. Poate seta session_replication_role? (așteptat: f)'
select has_parameter_privilege('session_replication_role', 'SET') as can_set_replication_role;
\echo '6. Încercarea de a ocoli trigger-ele (așteptat: ERROR permission denied)'
set session_replication_role = replica;
reset session_replication_role;
\echo '7. Trigger-e oprite (așteptat: 0 rânduri)'
select tgrelid::regclass as tabel, tgname from pg_trigger where not tgisinternal and tgenabled <> 'O';
\echo '8. Numerele (așteptat: factura 60, chitanța 21, contoarele FACTURA 60 și CHITANTA 21)'
select 'factura' as doc, number from invoice union all select 'chitanta', number from receipt;
select s.name, s.doc_type, c.year, c.last_number from series_counter c join series s on s.id = c.series_id order by s.doc_type;
\echo '9. Conturile (fără hash-uri)'
select username, role, active, password_hash is not null as has_password from app_user order by id;
\echo '10. TEMP pe bază (PUBLIC are TEMP dacă apare =T în acl; Q-038, condiția 1)'
select datacl from pg_database where datname = current_database();
