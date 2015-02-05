package com.fedex.testapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Federico Matteoni on 29/01/2015.
 */
public class Invio extends AsyncTask<String, Void, String> {
    public Socket socket;

    public Invio (Socket socket) {
        this.socket = socket;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            InputStream instream = socket.getInputStream();
            BufferedReader readerbuff = new BufferedReader(new InputStreamReader(instream));
            String input = readerbuff.readLine();
            if (input.equals("FINE")) {
                socket.close();
                socket = null;
                Log.v("FINE", "FINE ricevuto");
            }
            return input;
        } catch (IOException ex) {
            return "ERRORE INVIO [IO]";
        }
    }
}
