package com.fedex.testapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Home extends ActionBarActivity {
    public static Connessione connessione;
    public static boolean SQL = false;
    public static HashMap<String, String> servers = new HashMap<>();
    private String input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Log.d("Home", "discovery");
        Discovery discovery = new Discovery(this, servers); //Avvio il discovery dei server
        discovery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView usrlbl = (TextView) findViewById(R.id.textView4);
        EditText txtusr = (EditText) findViewById(R.id.txtUsr);
        TextView pwdlbl = (TextView) findViewById(R.id.pwdText);
        EditText txtpwd = (EditText) findViewById(R.id.txtPwd);
        TextView dblbl = (TextView) findViewById(R.id.textView5);
        EditText txtdb = (EditText) findViewById(R.id.txtDb);
        EditText txtporta = (EditText) findViewById(R.id.txtPorta);

        if (SQL) {
            usrlbl.setVisibility(View.VISIBLE);
            txtusr.setVisibility(View.VISIBLE);
            pwdlbl.setVisibility(View.VISIBLE);
            txtpwd.setVisibility(View.VISIBLE);
            dblbl.setVisibility(View.VISIBLE);
            txtdb.setVisibility(View.VISIBLE);
            txtporta.setHint("3306");
        } else {
            usrlbl.setVisibility(View.GONE);
            txtusr.setVisibility(View.GONE);
            pwdlbl.setVisibility(View.GONE);
            txtpwd.setVisibility(View.GONE);
            dblbl.setVisibility(View.GONE);
            txtdb.setVisibility(View.GONE);
            txtporta.setHint("1234");
        }
        Spinner spinner = (Spinner) findViewById(R.id.serverList);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                TextView ip = (TextView) findViewById(R.id.txtIp);
                TextView porta = (TextView) findViewById(R.id.txtPorta);
                ip.setText(parentView.getItemAtPosition(position).toString());
                porta.setText("8888");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                TextView ip = (TextView) findViewById(R.id.txtIp);
                TextView porta = (TextView) findViewById(R.id.txtPorta);
                ip.setText("");
                porta.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connetti(View v) { //Funzione richiamata dal pulsante "Connetti"
        EditText iptxt = (EditText) findViewById(R.id.txtIp);
        EditText portatxt = (EditText) findViewById(R.id.txtPorta);
        EditText txtusr = (EditText) findViewById(R.id.txtUsr);
        EditText txtpwd = (EditText) findViewById(R.id.txtPwd);
        EditText txtdb = (EditText) findViewById(R.id.txtDb);
        Switch sql = (Switch) findViewById(R.id.switch1);
        //Inizializzazione da interfaccia
        try {
            connessione = new Connessione(iptxt.getText().toString(), Integer.parseInt(portatxt.getText().toString()), SQL, txtusr.getText().toString(), txtpwd.getText().toString(), txtdb.getText().toString()); //Creo la connessione
            String cod = connessione.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "connetti").get(); //Avvio la connessione
            Log.v("Connessione", "tentativone");
            //Perché su classe diversa: necessario thread a parte per operazioni di rete (perché Android vuole così, specifica di sicurezza)

            switch (Integer.parseInt(cod)) { //A seconda del codice ritornato, stampo il risultato della connessione
                case -1:
                    if (SQL) {
                        //output.setText("ERRORE [SQL]");
                    } else {
                        //output.setText("ERRORE [UnknownHost]");
                    }
                    break;

                case -2:
                    if (SQL) {
                        //output.setText("ERRORE [ClassNotFound]");
                    } else {
                        //output.setText("ERRORE [IO]");
                    }
                    break;

                case -3:
                    //output.setText("ERRORE [NumberFormat]");
                    break;

                case 0:
                    Intent intent = new Intent(this, CommActivity.class);
                    startActivity(intent);
                    break;
            }
        } catch (Exception ex) {
            Log.d("Eccezione", ex.getMessage());
        } //TODO correggere
        //TODO
    }

    public void updateServers() {
        Spinner serverLister = (Spinner) findViewById(R.id.serverList);

        List<String> list = new ArrayList<String>(servers.keySet());
        HashMap<String, String> spinnerMap = new HashMap<String, String>();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serverLister.setAdapter(adapter);
    }

    /*public void abilitaSQL(View v) {
        SQL = !SQL;

    }*/
}