package com.ariel.cardsniffing.model;

public class Card {
    String cardtype;            /*!< string with card type */
    String cardnumber;          /*!< string with card number */
    String cardexpiration;      /*!< string with card expiration*/

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getCardexpiration() {
        return cardexpiration;
    }

    public void setCardexpiration(String cardexpiration) {
        this.cardexpiration = cardexpiration;
    }
}
