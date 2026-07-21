-- Current data snapshot from currency_exchange.

INSERT INTO currencies (id, full_name, code, sign) VALUES
    (1, 'American Dollar', 'USD', '$'),
    (2, 'Tether Dollar', 'UST', '$'),
    (3, 'Russian Ruble', 'RUB', '₽');

INSERT INTO exchange_rates (id, base_currency_id, target_currency_id, rate) VALUES
    (2, 1, 3, 68.000000),
    (3, 1, 2, 0.999999),
    (4, 3, 1, 0.014705);

SELECT setval('currencies_id_seq', 3, true);
SELECT setval('exchange_rates_id_seq', 4, true);
