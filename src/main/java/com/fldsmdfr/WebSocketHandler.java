package com.fldsmdfr;

import com.fldsmdfr.event.*;

import java.io.*;
import java.net.Socket;

public class WebSocketHandler extends Thread {
    private final Socket clientSocket;
    private String id;
    public String userName;
    private PrintWriter out;
    private BufferedReader in;
    private DataInputStream dis;
    private DataOutputStream dout;
    BufferedInputStream bis;

    private EventReceivePackageManager eventReceivePackageManager;

    public WebSocketHandler(Socket socket, String id, EventReceivePackageManager eventReceivePackageManager) {
        this.clientSocket = socket;
        this.id = id;
        this.eventReceivePackageManager = eventReceivePackageManager;
    }

    public void run() {
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.dis = new DataInputStream(clientSocket.getInputStream());
            this.dout = new DataOutputStream(clientSocket.getOutputStream());
            this.bis = new BufferedInputStream(clientSocket.getInputStream());

            while (true) {
                //String message = in.readLine();
                String data = dis.readUTF();
                this.notifyEventReceivePackage(data);
            }

        } catch (IOException e) {
            // todo  desconectar
            DataPackage dataPackage =  new DataPackage(this.id, "all", this.id, Protocol.ACTION_DISCONNECT_CLIENT);
            this.notifyEventReceivePackage(dataPackage.toString());
            e.printStackTrace();
        }
    }


    public void notifyEventReceivePackage(String data) {
        if (this.eventReceivePackageManager == null) {
            return;
        }
        eventReceivePackageManager.fireEventReceivePackage(new EventReceivePackage(this, data));
    }


    public synchronized void send(String data) {
        try {
            this.dout.writeUTF(data);
            this.dout.flush();
            sleepClient(100);
        } catch (IOException ex) {
            System.err.println(WebSocketHandler.class.getName() + ex.getMessage());
        }
    }


    private void sleepClient(int sleep) {
        try {
            sleep(sleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
