package com.fedex.testapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;


public class CommActivity extends ActionBarActivity {
    private boolean streaming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm);
        setTitle(getString(R.string.comunicazione));

        TextView output = (TextView) findViewById(R.id.txtConnessione);
        TextView outputview = (TextView) findViewById(R.id.txtOutput);
        TextView log = (TextView) findViewById(R.id.txtLog);
        Button disconnetti = (Button) findViewById(R.id.disconnettiBtn);
        Button invio = (Button) findViewById(R.id.inviaBtn);

        Ricezione ricezione = new Ricezione(Home.connessione, outputview, log);
        ricezione.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        log("Connesso [" + System.currentTimeMillis() / 1000 + "]");
        output.setText("Connesso");
        output.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        invio.setEnabled(true);
        disconnetti.setEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_comm, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        disconnetti(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void disconnetti(View v) {
        TextView output = (TextView) findViewById(R.id.txtConnessione);

        try {
            if (Home.SQL) {
                Home.connessione.connection.close();
                Home.connessione = null;
            } else {
                Home.connessione.socket.close();
                Home.connessione = null;
            }
            output.setText("Disconnesso");
            output.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            super.onBackPressed();
        } catch (IOException e) {
            output.setText("ERRORE [IO]");
        } catch (SQLException e) {
            output.setText("ERRORE [SQL]");
        }
    }

    public void invia(View v) { //Funzione richiamata dal pulsante "Invia"
        EditText outstring = (EditText) findViewById(R.id.txtStringa);
        TextView outputview = (TextView) findViewById(R.id.txtOutput); //Inizializzazione da interfaccia

        if (Home.SQL) {
            Invio invio = new Invio(Home.connessione.socket, Home.connessione.connection, Home.SQL, outstring.getText().toString());

            try {
                String output = invio.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get(); //permette più asynctask contemporaneamente
                if (output.startsWith("ERRORE")) {
                    log(output);
                } else {
                    outputview.setText(output);
                }
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        } else {
            try {
                DataOutputStream outstream = new DataOutputStream(Home.connessione.socket.getOutputStream());
                outstream.writeBytes(outstring.getText().toString() + "\n");
                outputview.append(outstring.getText().toString() + "\n");
                outstring.setText("");
            } catch (IOException e) {
            }
        }
    }

    public void inviaGetServerInfo(View v) {
        EditText outstring = (EditText) findViewById(R.id.txtStringa);
        TextView outputview = (TextView) findViewById(R.id.txtOutput);
        String txt = "GetServerInfo";

        try {
            DataOutputStream outstream = new DataOutputStream(Home.connessione.socket.getOutputStream());
            outstream.writeBytes(txt + "\n");
            outputview.append(txt + "\n");
            outstring.setText("");
        } catch (IOException e) {
        }
    }

    public void inviaStreamCamera(View v) {
        EditText outstring = (EditText) findViewById(R.id.txtStringa);
        TextView outputview = (TextView) findViewById(R.id.txtOutput);
        Button disconnetti = (Button) findViewById(R.id.disconnettiBtn);
        Button btnStream = (Button) findViewById(R.id.btnStream);
        Button btnInfo = (Button) findViewById(R.id.btnInfo);
        String txt = "StreamCamera";

        try {
            if (!streaming) {
                streaming = true;
                disconnetti.setEnabled(false);
                btnInfo.setEnabled(false);
                btnStream.setText("StreamStop");
                DataOutputStream outstream = new DataOutputStream(Home.connessione.socket.getOutputStream());
                outstream.writeBytes(txt + "\n");
                outputview.append(txt + "\n");
                outstring.setText("");
                new Thread() {
                    public void run() {
                        try {
                            DatagramSocket streamsocket = new DatagramSocket(8890);
                            byte[] sendData;

                            double n;
                            do {
                                n = Math.random();
                                sendData = (n + "").getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, Home.connessione.socket.getInetAddress(), 8890);
                                streamsocket.send(sendPacket);
                            } while (streaming);
                            sendData = ("Close").getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, Home.connessione.socket.getInetAddress(), 8890);
                            streamsocket.send(sendPacket);
                            streamsocket.close();
                        } catch (IOException e) {
                        }
                    }
                }.start();
            } else {
                streaming = false;
                disconnetti.setEnabled(true);
                btnInfo.setEnabled(true);
                btnStream.setText("StreamStart");
            }
        } catch (IOException e) {
        }
    }

    public void log(String str) {
        TextView logview = (TextView) findViewById(R.id.txtLog);
        logview.append(str + "\n");
    }

    public void espandiOutput(View v) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layLog);

        if (layout.getVisibility() == View.GONE) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    public void espandiLog(View v) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layOutput);

        if (layout.getVisibility() == View.GONE) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }
}
