package com.fedex.testapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;


public class CommActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm);
        setTitle(getString(R.string.comunicazione));

        TextView output = (TextView) findViewById(R.id.txtConnessione);
        TextView outputview = (TextView) findViewById(R.id.txtOutput);
        Button disconnetti = (Button) findViewById(R.id.disconnettiBtn);
        Button invio = (Button) findViewById(R.id.inviaBtn);

        Ricezione ricezione = new Ricezione(Home.connessione, outputview);
        ricezione.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        log("Connesso [" + System.currentTimeMillis() + "]");
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
        /*Button disconnetti = (Button) findViewById(R.id.disconnettiBtn);
        Button invio = (Button) findViewById(R.id.inviaBtn);
        TextView outputview = (TextView) findViewById(R.id.txtOutput);
        EditText outstring = (EditText) findViewById(R.id.txtStringa);*/

        try {
            if (Home.SQL) {
                Home.connessione.connection.close();
                Home.connessione = null;
            } else {
                Home.connessione.socket.close();
                Home.connessione = null;
            }
            /*invio.setEnabled(false);
            disconnetti.setEnabled(false);*/
            /*outputview.setText("");
            outstring.setText("");*/
            output.setText("Disconnesso");
            output.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            super.onBackPressed();
            /*Intent intent = new Intent(this, Home.class);
            startActivity(intent);*/
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
                String output = invio.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                outputview.setText(output);
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        } else {
            try {
                DataOutputStream outstream = new DataOutputStream(Home.connessione.socket.getOutputStream());
                outstream.writeBytes(outstring.getText().toString() + "\n");
                outputview.append(">" + outstring.getText().toString() + "\n");
            } catch (IOException e) {
            }
        }
    }

    public void log(String str) {
        TextView logview = (TextView) findViewById(R.id.txtLog);
        logview.append(str + "\n");
    }
}
