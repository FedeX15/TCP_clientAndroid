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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import android.app.Activity;

/**
 * Created by Federico Matteoni on 28/01/15.
 */
public class Connessione extends AsyncTask<String, Void, String> {
    private String db;
    private String pwd;
    private String usr;
    private InetAddress serveraddr;
    private int porta;
    private boolean SQL;
    public Socket socket;
    public Connection connection;

    public Connessione (String ip, int porta, boolean sql, String usr, String pwd, String db) {
        try {
            this.serveraddr = InetAddress.getByName(ip);
            this.porta = porta;
            this.socket = null;
            this.connection = null;
            this.SQL = sql;
            this.usr = usr;
            this.pwd = pwd;
            this.db = db;
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        }

        Log.d("Info", "Connessione creata");
    }

    @Override
    protected String doInBackground(String... params) {
        if (SQL) {
            String DRIVER = "com.mysql.jdbc.Driver";
            String dbUrl = "jdbc:mysql:/" + serveraddr.toString() + ":" + porta;
            try {
                //Carico driver
                Class.forName(DRIVER);
                //Apro la connessione
                this.connection = DriverManager.getConnection(dbUrl + "/" + db, usr, pwd);
                return "0";
            } catch (SQLException ex) {
                ex.printStackTrace();
                return "-1";
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                return "-2";
            }

        } else {
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
        }
        return "";
    }
}
