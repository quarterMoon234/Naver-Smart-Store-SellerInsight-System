insert into sellers (
    external_seller_id,
    seller_name,
    status,
    connected_at
)
select
    'seller-demo',
    'seller-demo-store',
    'CONNECTED',
    current_timestamp
where not exists (
    select 1
    from sellers
    where external_seller_id = 'seller-demo'
);
