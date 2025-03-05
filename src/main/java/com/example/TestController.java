package com.example;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;


@Controller("/hello")
public class TestController {

    @Get("/")
            public String sayHello() {
                return "Hello Micronaout!";
            }
}