package com.example.gamef;

import java.sql.Timestamp;

public class IssuedBook {
    private String bookId;
    private String bookTitle;
    private String memberId;
    private String memberName;
    private Timestamp issueTime;
    private int renewCount;

    public IssuedBook(String bookId, String bookTitle, String memberId, String memberName, Timestamp issueTime, int renewCount) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberId = memberId;
        this.memberName = memberName;
        this.issueTime = issueTime;
        this.renewCount = renewCount;
    }

    // Getters for PropertyValueFactory
    public String getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public Timestamp getIssueTime() {
        return issueTime;
    }

    public int getRenewCount() {
        return renewCount;
    }
}