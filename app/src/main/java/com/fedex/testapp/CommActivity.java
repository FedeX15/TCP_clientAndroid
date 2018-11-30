package com.fedex.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;


public class CommActivity extends Activity {
    static int width;
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
        disconnetti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    onBackPressed();
                } catch (IOException e) {
                    output.setText("ERRORE [IO]");
                } catch (SQLException e) {
                    output.setText("ERRORE [SQL]");
                }
            }
        });
        Button invio = (Button) findViewById(R.id.inviaBtn);
        invio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                         final EditText outstring = (EditText) findViewById(R.id.txtStringa);
                         final TextView outputview = (TextView) findViewById(R.id.txtOutput); //Inizializzazione da interfaccia

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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        outputview.append(outstring.getText().toString() + "\n");
                                    }
                                });
                                outstring.setText("");
                            } catch (IOException e) {
                            }
                        }

                    }
                };
                t.start();
            }
        });
        Button stream = (Button) findViewById(R.id.btnStream);
        stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        EditText outstring = (EditText) findViewById(R.id.txtStringa);
                        final Button disconnetti = (Button) findViewById(R.id.disconnettiBtn);
                        final Button play = (Button) findViewById(R.id.btnPlay);
                        final Button btnStream = (Button) findViewById(R.id.btnStream);
                        final Button btnInfo = (Button) findViewById(R.id.btnInfo);
                        final String txt = "StartUDPStream";

                        try {
                            if (!streaming) {
                                streaming = true;
                                Looper.prepare();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        disconnetti.setEnabled(false);
                                        btnInfo.setEnabled(false);
                                        play.setEnabled(false);
                                    }
                                }, 100);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        btnStream.setText("Stream Stop");
                                    }
                                });
                                DataOutputStream outstream = new DataOutputStream(Home.connessione.socket.getOutputStream());
                                outstream.writeBytes(txt + "\n");
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        log(txt);
                                    }
                                });
                                outstring.setText("");
                                new Thread() {
                                    public void run() {
                                        try {
                                            DatagramSocket streamsocket = new DatagramSocket(8890);
                                            byte[] sendData;
                                            final TextView outputview = (TextView) findViewById(R.id.txtOutput);
                                            final String oldtxt = outputview.getText().toString();
                                            double n;
                                            do {
                                                n = Math.random();
                                                sendData = (n + "").getBytes();
                                                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, Home.connessione.socket.getInetAddress(), 8890);
                                                streamsocket.send(sendPacket);
                                                final double x = n;
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        outputview.setText(oldtxt + x + "\n");
                                                    }
                                                });
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
                                Looper.prepare();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        disconnetti.setEnabled(true);
                                        btnInfo.setEnabled(true);
                                        play.setEnabled(true);
                                    }
                                }, 100);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        btnStream.setText("Stream Start");
                                    }
                                });
                            }
                        } catch (IOException e) {
                        }
                    }
                };
                t.start();
            }
        });
        Button info = (Button) findViewById(R.id.btnInfo);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        final EditText outstring = (EditText) findViewById(R.id.txtStringa);
                        final TextView outputview = (TextView) findViewById(R.id.txtOutput);
                        final String txt = "GetServerInfo";

                        try {
                            DataOutputStream outstream = new DataOutputStream(Home.connessione.socket.getOutputStream());
                            outstream.writeBytes(txt + "\n");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    outputview.append(txt + "\n");
                                }
                            });
                            outstring.setText("");
                        } catch (IOException e) {
                        }
                    }
                };
                t.start();
            }
        });
        Button play = (Button) findViewById(R.id.btnPlay);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            ScrollView layout = (ScrollView) findViewById(R.id.scrollView4);

                            Log.d("Dimensions", layout.getWidth() + " " + layout.getHeight());
                            CommActivity.width = (layout.getWidth() * 10) / 100;
                            final String txt = "Play&" + layout.getWidth() + "-" + layout.getHeight();
                            DataOutputStream outstream = new DataOutputStream(Home.connessione.socket.getOutputStream());
                            outstream.writeBytes(txt + "\n");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                  log(txt);
                                }
                            });
                            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                            startActivity(intent);
                        } catch (IOException ex) {
                        }
                    }
                };
                t.start();
            }
        });

        Ricezione ricezione = new Ricezione(Home.connessione, outputview, log);
        ricezione.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        log("Connesso");
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
