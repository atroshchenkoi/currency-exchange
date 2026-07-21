-- Run this script once while connected to the PostgreSQL database named postgres.
-- Replace change_me with your own password before running the script.

CREATE ROLE currency_exchange_user LOGIN PASSWORD 'change_me';
CREATE DATABASE currency_exchange OWNER currency_exchange_user;
