package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private EditText et_from, et_to;
    private Button bt;
    public static final String MY_SP = "MY_SP";
    public static final String CURRENCIES = "CURRENCIES";
    private HashSet<String> currencies;
    private static String apiKey = "";
    public static final String site = "https://free.currconv.com";
    public static final String apiV = "/api/v7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiKey = getResources().getString(R.string.api_key);
        et_from = findViewById(R.id.et_convert_from);
        et_to = findViewById(R.id.et_convert_to);
        bt = findViewById(R.id.bt_convert);
        View.OnClickListener btOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_from = findViewById(R.id.et_convert_from);
                new ConvertLoader().execute(Double.parseDouble(et_from.getText().toString()));
            }
        };
        SharedPreferences sp = getSharedPreferences(MY_SP, MODE_PRIVATE);
        sp.getStringSet(CURRENCIES, currencies);
        if (currencies == null) {
            new CurrenciesLoader().execute();
        }
        bt.setOnClickListener(btOnClickListener);
    }

    void addCurrencies() {
        ArrayList<String> currencies_list = new ArrayList<String>();
        for (String cur : currencies) {
            currencies_list.add(cur);
        }
        Collections.sort(currencies_list);

        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, (List) currencies_list);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner sp1, sp2;
        sp1 = findViewById(R.id.spinner1);
        sp2 = findViewById(R.id.spinner2);

        sp1.setAdapter(spinnerAdapter);
        sp2.setAdapter(spinnerAdapter);

/*        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/
    }

    //load list of currencies
    class CurrenciesLoader extends AsyncTask<Void, Void, HashSet<String>> {

        @Override
        protected HashSet<String> doInBackground(Void... voids) {
            try {
                return connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashSet<String> strings) {
            super.onPostExecute(strings);
            currencies = strings;
            addCurrencies();
        }
    }

    public HashSet<String> connect() throws IOException {
        HashSet<String> curs = new HashSet<String>();
        URL url = new URL(site + apiV + "/currencies?apiKey=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = conn.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            String input = "";
            if (scanner.hasNext())
                input = scanner.next();
            JSONObject inputJson = new JSONObject(input);
            JSONObject res = inputJson.getJSONObject("results");
            Iterator<String> it = res.keys();
            for (Iterator<String> iter = it; iter.hasNext(); ) {
                String cur = iter.next();
                curs.add(cur);
            }
        } finally {
            conn.disconnect();
            return curs;
        }
    }

    //convert to second currency
    class ConvertLoader extends AsyncTask<Double, Void, Double> {

        @Override
        protected Double doInBackground(Double... doubles) {
            try {
                return convertation(doubles[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0.0;
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);
            EditText et2 = findViewById(R.id.et_convert_to);
            et2.setText(aDouble.toString());
        }
    }

    public double convertation(double from) throws IOException {
        Spinner sp1, sp2;
        String cur1, cur2;
        sp1 = (Spinner)findViewById(R.id.spinner1);
        cur1 = (String) sp1.getSelectedItem();
        sp2 = (Spinner)findViewById(R.id.spinner2);
        cur2 = (String) sp2.getSelectedItem();
        URL url = new URL(site + apiV + "/convert?q=" + cur1 + "_" + cur2 + "&compact=ultra&apiKey=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        double ret = 0.0;
        try {
            InputStream in = conn.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            String input = "";
            if (scanner.hasNext())
                input = scanner.next();
            JSONObject inputJson = new JSONObject(input);
            ret = inputJson.getDouble(cur1 + "_" + cur2);
        } finally {
            conn.disconnect();
            return ret * from;
        }
    }
}
