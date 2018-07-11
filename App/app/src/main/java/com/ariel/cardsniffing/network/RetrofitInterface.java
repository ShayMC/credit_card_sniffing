package com.ariel.cardsniffing.network;


import com.ariel.cardsniffing.model.Card;
import com.ariel.cardsniffing.model.Response;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface RetrofitInterface {

    @POST("cards/new-card")
    Observable<Response> newCard(@Body Card card);

    @GET("cards/get-cards")
    Observable<Response> getCards();


}
