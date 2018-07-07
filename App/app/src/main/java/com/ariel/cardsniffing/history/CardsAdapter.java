package com.ariel.cardsniffing.history;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.ariel.cardsniffing.R;

import com.ariel.cardsniffing.model.Card;

import java.util.ArrayList;
import java.util.List;

public class CardsAdapter extends ArrayAdapter<Card> {
    private Context mContext;
    private List<Card> cardsList;

    public CardsAdapter(Activity context, ArrayList<Card> list) {
        super(context, 0, list);
        mContext = context;
        cardsList = list;
    }

    public List<Card> getCoursesList() {
        return cardsList;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        Card currentCard = cardsList.get(position);

        if (convertView == null) {

            listItem = LayoutInflater.from(mContext).inflate(R.layout.item_card, parent, false);

        }
        TextView type = listItem.findViewById(R.id.type);
        TextView num = listItem.findViewById(R.id.num);
        TextView exp = listItem.findViewById(R.id.exp);

        type.setText(currentCard.getCardtype());
        num.setText(currentCard.getCardnumber());
        exp.setText(currentCard.getCardexpiration());


        return listItem;
    }
}

