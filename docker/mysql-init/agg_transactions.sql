CREATE TABLE if not exists agg_transactions (
    account_id  BIGINT,
    total_amount      BIGINT,
    window_start TIMESTAMP(3),
    window_end TIMESTAMP(3)
);
