package com.example.gamef;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class IssueRequest {
    private final SimpleIntegerProperty requestID;
    private final SimpleStringProperty bookID;
    private final SimpleStringProperty bookTitle;
    private final SimpleStringProperty memberID;
    private final SimpleStringProperty memberName;

    public IssueRequest(int requestID, String bookID, String bookTitle, String memberID, String memberName) {
        this.requestID = new SimpleIntegerProperty(requestID);
        this.bookID = new SimpleStringProperty(bookID);
        this.bookTitle = new SimpleStringProperty(bookTitle);
        this.memberID = new SimpleStringProperty(memberID);
        this.memberName = new SimpleStringProperty(memberName);
    }

    public int getRequestID() { return requestID.get(); }
    public String getBookID() { return bookID.get(); }
    public String getBookTitle() { return bookTitle.get(); }
    public String getMemberID() { return memberID.get(); }
    public String getMemberName() { return memberName.get(); }
}