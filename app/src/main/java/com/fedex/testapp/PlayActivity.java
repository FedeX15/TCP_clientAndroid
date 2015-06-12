package com.fedex.testapp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class PlayActivity extends ActionBarActivity {
    DatagramSocket streamsocket;
    byte[] sendData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent me) {
                Button btn = (Button) findViewById(R.id.btnUser);
                final int x = (int) (me.getRawX() - (btn.getWidth() / 2));
                btn.setX(x);
                new Thread() {
                    public void run() {
                        try {
                            sendData = ("" + x).getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, Home.connessione.socket.getInetAddress(), 8890);
                            streamsocket.send(sendPacket);
                        } catch (IOException ex) {
                        }
                    }
                }.start();
                return true;
            }

            ;
        });
        try {
            streamsocket = new DatagramSocket(8890);
        } catch (SocketException ex) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_play, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new Thread() {
            public void run() {
                try {
                    sendData = ("Close").getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, Home.connessione.socket.getInetAddress(), 8890);
                    streamsocket.send(sendPacket);
                    streamsocket.close();
                } catch (IOException e) {
                }
            }
        }.start();
        super.onBackPressed();
    }
}