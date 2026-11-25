-- D-036, D-037, D-038: încercările greșite de login se numără pe cont; la a 5-a, contul se
-- blochează 15 minute și numărătoarea revine la 0. Blocarea expiră singură (locked_until).
alter table app_user
    add column failed_login_count smallint    not null default 0 check (failed_login_count >= 0),
    add column locked_until       timestamptz;
