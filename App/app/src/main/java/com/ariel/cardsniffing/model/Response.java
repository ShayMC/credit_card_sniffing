package com.ariel.cardsniffing.model;

public class Response {

    private String message;
    private Card ans[];


    public void setMessage(String message) {
        this.message = message;
    }

    public Card[] getAns() {
        return ans;
    }

    public void setAns(Card[] ans) {
        this.ans = ans;
    }

    public String getMessage() {
        return message;
    }


}
