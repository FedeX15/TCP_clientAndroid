package com.fedex.testapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import static java.lang.Thread.sleep;

/**
 * Created by Federico on 05/05/2015.
 */
public class Discovery extends AsyncTask<String, String, String> {
    public HashMap<String, String> servers;
    public Home home;

    public Discovery(Home home, HashMap<String, String> servers) {
        Log.d("Discovery", "creato");
        this.servers = servers;
        this.home = home;
    }

    protected String doInBackground(String... params) {
        Log.d("Discovery", "avviato");

        try {
            DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);
            DatagramSocket socket = new DatagramSocket(8889);
            do {
                byte[] sendData = "SocketTest_discovery".getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8889);
                try {
                    c.send(sendPacket);
                } catch (IOException ex) {
                    //TODO FIX PLS
                    Log.d("Discovery", "errore pacchetto inviato");
                    ex.printStackTrace();
                }

                try {
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.setSoTimeout(500);
                    socket.receive(packet);
                    publishProgress(new String(recvBuf, 0, packet.getLength()));
                    Log.d("Discovery", new String(recvBuf, 0, packet.getLength()));
                } catch (SocketTimeoutException ex) {
                } catch (IOException ex) {
                    Log.d("Discovery", "errore pacchetto ricevuto");
                    ex.printStackTrace();
                }
                c.disconnect();
                c.close();
                sleep(1000);
            } while (true);
        } catch (SocketException | UnknownHostException | InterruptedException e) {
        }
        Log.d("Discovery", "fine");
        return "";
    }

    protected void onProgressUpdate(String... values) {
        if (!values[0].equals("SocketTest_discovery")) {
            if (values[0].contains("SocketTest")) {
                String hostname = values[0].split("&")[1].split("/")[0];
                String ip = values[0].split("&")[1].split("/")[1];
                servers.put(hostname, ip);
                home.updateServers();
                Log.d(hostname, ip);
            }
        }
    }
}
