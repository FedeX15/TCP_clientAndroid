package com.fedex.testapp;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Federico Matteoni on 28/01/15.
 */
public class Connessione extends AsyncTask<String, Void, String> {
    private InetAddress serveraddr;
    private int porta;
    public Socket socket;

    public Connessione (String ip, int porta) {
        try {
            this.serveraddr = InetAddress.getByName(ip);
            this.porta = porta;
            this.socket = null;
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }

        Log.d("Info", "Connessione creata");
    }

    @Override
    protected String doInBackground(String... params) {
        if (socket == null) {
            Log.d("Info", "In connessione");
            try {
                socket = new Socket(serveraddr, porta);
                Log.d("Info", "Connesso");
                return "0";
            } catch (UnknownHostException e) {
                return "-1";
            } catch (IOException e) {
                return "-2";
            } catch (NumberFormatException e) {
                return "-3";
            }
        }
        return "";
    }
}
