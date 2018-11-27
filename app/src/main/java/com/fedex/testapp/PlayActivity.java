package com.fedex.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class PlayActivity extends Activity {
    DatagramSocket streamsocket;
    byte[] sendData;
    boolean play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setTitle(getString(R.string.play));
        Button opp = (Button) findViewById(R.id.btnOpponent);
        Button usr = (Button) findViewById(R.id.btnUser);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        opp.setWidth(CommActivity.width);
        usr.setWidth(CommActivity.width);
        play = true;
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

            new Thread() {
                public void run() {
                    String txt = "";
                    int x;
                    int y;
                    sendData = new byte[1500];
                    do {
                        try {
                            sendData = new byte[1500];
                            DatagramPacket recvPacket = new DatagramPacket(sendData, sendData.length);
                            streamsocket.receive(recvPacket);
                            txt = new String(sendData, 0, recvPacket.getLength());
                            if (txt.startsWith("Opponent")) {
                                x = Integer.parseInt(txt.split("&")[1]);
                                setOpponentPosition(x);
                            } else if (txt.startsWith("Ball")) {
                                txt = txt.split("&")[1];
                                x = Integer.parseInt(txt.split("-")[0]);
                                y = Integer.parseInt(txt.split("-")[1]);
                                setBallPosition(x, y);
                            }
                        } catch (IOException ex) {
                        } catch (NumberFormatException ex) {
                        } catch (StringIndexOutOfBoundsException ex) {
                        }
                    } while (play);
                }
            }.start();
        } catch (SocketException ex) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_play, menu);
        getActionBar().setHomeButtonEnabled(false);      // Disable the button
        getActionBar().setDisplayHomeAsUpEnabled(false); // Remove the left caret
        getActionBar().setDisplayShowHomeEnabled(false); // Remove the icon
        return true;
    }

    public void setOpponentPosition(final int x) {
        final Button btn = (Button) findViewById(R.id.btnOpponent);
        runOnUiThread(new Runnable() {
            public void run() {
                btn.setX(x);
            }
        });
    }

    public void setBallPosition(final int x, final int y) {
        final RadioButton ball = (RadioButton) findViewById(R.id.ball);
        final RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        runOnUiThread(new Runnable() {
            public void run() {
                ball.setX(x);
                ball.setY(layout.getHeight() - y - 100);
            }
        });
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
        play = false;
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
