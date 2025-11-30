package com.example.gamef;

public class Book {
    private String bookId;
    private String title;
    private String author;
    private String publisher;
    private String available;

    public Book(String bookId, String title, String author, String publisher, String available) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.available = available;
    }

    public String getBookId() {
        return bookId;
    }
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getPublisher() {
        return publisher;
    }
    public String getAvailable() {
        return available;
    }
}
