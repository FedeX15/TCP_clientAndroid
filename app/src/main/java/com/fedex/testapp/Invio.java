package com.fedex.testapp;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Federico Matteoni on 29/01/2015.
 */
public class Invio extends AsyncTask<String, Void, String> {
    public Socket socket;
    public Connection connection;
    private Statement stat, statement;
    private ResultSet resl, resultset;
    public String columnTitle[] = null;
    public String table[][] = null;
    private boolean SQL;
    private String query;

    public Invio (Socket socket, Connection connection, boolean SQL, String query) {
        this.socket = socket;
        this.connection = connection;
        stat = null;
        statement = null;
        resl = null;
        resultset = null;
        this.SQL = SQL;
        this.query = query;
    }

    @Override
    protected String doInBackground(String... params) {
        if (SQL) {
            try {
                String output = "";
                stat = connection.createStatement();
                //Interrogo il DBMS con una query SQL
                resl = stat.executeQuery(query);

                columnTitle = new String[resl.getMetaData().getColumnCount()];
                for (int i = 1; i <= columnTitle.length; i++) {
                    columnTitle[i-1] = resl.getMetaData().getColumnLabel(i); //titoli colonne in vettore
                    output += columnTitle[i-1];
                }
                output += "\n";
                resl.last();
                table = new String[resl.getRow()][columnTitle.length]; //prendo numero righe e inizializzo tabella
                resl.beforeFirst(); //torno in cima

                int j = 0;
                while (resl.next()) {
                    for (int i = 1; i <= columnTitle.length; i++) {
                        table[j][i-1] = resl.getString(i);
                        output += table[j][i-1] + "\t";
                    }
                    output += "\n";
                    j++;
                }
                return output;
            } catch (SQLException e) {
                e.printStackTrace();
                return "ERRORE INVIO [SQL]";
            }
        } else {
            return "";
            /*try {
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
            }*/
        }
    }
}
