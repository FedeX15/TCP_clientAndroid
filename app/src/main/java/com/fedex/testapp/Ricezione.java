package com.fedex.testapp;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Federico on 13/02/2015.
 */
public class Ricezione extends AsyncTask<String, String, Void> {
    private Connessione connessione;
    private TextView output;

    public Ricezione (Connessione connessione, TextView output) {
        this.connessione = connessione;
        this.output = output;
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            InputStream instream = null;
            instream = connessione.socket.getInputStream();
            BufferedReader readerbuff = new BufferedReader(new InputStreamReader(instream));
            do {
                String input = readerbuff.readLine();
                if (input.equals("FINE")) {
                    connessione.socket.close();
                    connessione.socket = null;
                    Log.v("FINE", "FINE ricevuto");
                }
                publishProgress(input);
            } while (connessione.socket != null);
        } catch (IOException e) {
        } catch (NullPointerException e) {
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        output.setText(values[0]);
    }
}
