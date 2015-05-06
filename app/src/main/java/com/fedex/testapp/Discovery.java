package com.fedex.testapp;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

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
            //Open a random port to send the package
            DatagramSocket c = new DatagramSocket();
            c.setBroadcast(true);

            byte[] sendData = "SocketTest_discovery".getBytes();
            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8889);
                c.send(sendPacket);
            } catch (Exception e) {
            }

            DatagramSocket socket = new DatagramSocket(8889);
            for (int i = 0; i < 10; i++) {
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                publishProgress(new String(recvBuf, 0, packet.getLength()));
                Log.d("Discovery", new String(recvBuf, 0, packet.getLength()));
            }
        } catch (Exception e) {
        }
        return "";
    }

    protected void onProgressUpdate(String... values) {
        if (values[0].contains("SocketTest")) {
            String hostname = values[0].split("&")[1].split("/")[0];
            String ip = values[0].split("&")[1].split("/")[1];
            servers.put(ip, hostname);
            home.updateServers();
            Log.d(hostname, ip);
        }
    }
}
