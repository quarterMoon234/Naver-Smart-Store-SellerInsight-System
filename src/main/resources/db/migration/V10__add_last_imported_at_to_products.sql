alter table products
    add column last_imported_at timestamp with time zone;

update products
set last_imported_at = updated_at
where last_imported_at is null;

alter table products
    alter column last_imported_at set not null;
