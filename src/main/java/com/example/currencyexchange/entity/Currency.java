package com.example.currencyexchange.entity;

import lombok.Value;

@Value
public class Currency {
    long id;
    String name;
    String code;
    String sign;
}
