package com.example;

import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Singleton;
import io.micronaut.function.aws.proxy.MicronautLambdaHandler;


public class BookRequestHandler extends MicronautRequestHandler<BookRequest, BookResponse> {

    @Override
    public BookResponse execute(BookRequest input) {
        String msg = "Received book: " + input.getTitle() + " by " + input.getAuthor();
        return new BookResponse(msg);
    }
}


