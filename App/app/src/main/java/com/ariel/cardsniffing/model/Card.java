package com.ariel.cardsniffing.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable {
    String cardtype;            /*!< string with card type */
    String cardnumber;          /*!< string with card number */
    String cardexpiration;      /*!< string with card expiration*/
    String file;

    public Card(){}


    protected Card(Parcel in) {
        cardtype = in.readString();
        cardnumber = in.readString();
        cardexpiration = in.readString();
        file = in.readString();
    }

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(cardtype);
        parcel.writeString(cardnumber);
        parcel.writeString(cardexpiration);
        parcel.writeString(file);
    }
}
