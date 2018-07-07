package com.ariel.cardsniffing.history;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ariel.cardsniffing.R;
import com.ariel.cardsniffing.model.Card;
import com.ariel.cardsniffing.model.Response;
import com.ariel.cardsniffing.network.RetrofitRequests;
import com.ariel.cardsniffing.network.ServerResponse;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class History extends AppCompatActivity {

    private ServerResponse mServerResponse;
    private CompositeSubscription mSubscriptions;
    private ListView cardsList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CardsAdapter mAdapter;
    private ShimmerFrameLayout mShimmerViewContainer;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mSubscriptions = new CompositeSubscription();
        mServerResponse = new ServerResponse(findViewById(R.id.tool_bar));
        initViews();
        mSwipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            pullCards();
            mSwipeRefreshLayout.setRefreshing(false);
        }, 1000));
    }

    private void initViews() {
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        mShimmerViewContainer.startShimmerAnimation();
        cardsList = (ListView) findViewById(R.id.cards);
        ImageButton buttonBack = (ImageButton) findViewById(R.id.image_Button_back);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        buttonBack.setOnClickListener(view -> finish());
    }

    private void pullCards() {
        mSubscriptions.add(RetrofitRequests.getRetrofit().getCards()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, i -> mServerResponse.handleErrorDown(i)));
    }


    private void handleResponse(Response response) {
        ArrayList<Card> saveCards = new ArrayList<>(Arrays.asList(response.getAns()));
        Collections.reverse(saveCards);
        mAdapter = new CardsAdapter(this, new ArrayList<>(saveCards));
        cardsList.setAdapter(mAdapter);
        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.GONE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pullCards();
    }



}
