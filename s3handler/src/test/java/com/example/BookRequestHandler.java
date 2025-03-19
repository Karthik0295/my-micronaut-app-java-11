package com.example;

import io.micronaut.function.aws.MicronautRequestHandler;
import java.util.Map;

public class BookRequestHandler extends MicronautRequestHandler<Map<String, Object>, String> {
    @Override
    public String execute(Map<String, Object> input) {
        return "Hello from Lambda!";
    }
}
