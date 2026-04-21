create index idx_orders_seller_id_ordered_at
    on orders (seller_id, ordered_at);

create index idx_products_seller_id_stock_quantity
    on products (seller_id, stock_quantity);

create index idx_order_items_product_id_order_id
    on order_items (product_id, order_id);
