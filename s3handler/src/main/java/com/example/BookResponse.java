package com.example;

public class BookResponse {
    private String message;

    public BookResponse() {}

    public BookResponse(String message) {
        this.message = message;
    }

    // Getter and Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
