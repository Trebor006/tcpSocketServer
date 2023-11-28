package com.fldsmdfr;

import com.fldsmdfr.event.MyClassEventManager;
import com.fldsmdfr.event.MyEvent;
import org.json.JSONObject;

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

    public static final String ACTION_USERNAME = "USERNAME";
    public static final String ACTION_CONNECT_CLIENT = "CONNECT_CLIENT";
    public static final String ACTION_DISCONNECT_CLIENT = "DISCONNECT_CLIENT";
    public static final String ACTION_LIST_CLIENTS = "LIST_CLIENTS";
    public static final String ACTION_MESSAGE = "MESSAGE";
    public static final String ACTION_FILE = "FILE";


    private MyClassEventManager myClassEventManager;

    public WebSocketHandler(Socket socket, String id, MyClassEventManager myClassEventManager) {
        this.clientSocket = socket;
        this.id = id;
        this.myClassEventManager = myClassEventManager;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            dis = new DataInputStream(clientSocket.getInputStream());
            dout = new DataOutputStream(clientSocket.getOutputStream());
            bis = new BufferedInputStream(clientSocket.getInputStream());

            while (true) {
                String action = in.readLine();
                this.executeAction(action);
            }

        } catch (IOException e) {
            JSONObject data = new JSONObject();
            data.put("action", ACTION_DISCONNECT_CLIENT);
            data.put("userName", this.userName);
            data.put("id", this.id);
            data.put("source", id);
            myClassEventManager.fireMyEvent(new MyEvent(this, data));
            e.printStackTrace();
        }
    }

    private void executeAction(String action) {
        System.out.println("Accion recibida: " + action);
        switch (action) {
            case ACTION_USERNAME: {
                try {
                    this.userName = this.in.readLine();
                    System.out.println("userName: " + this.userName);
                    JSONObject data = new JSONObject();
                    data.put("action", ACTION_USERNAME);
                    data.put("userName", this.userName);
                    data.put("id", this.id);
                    data.put("source", id);
                    myClassEventManager.fireMyEvent(new MyEvent(this, data));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case ACTION_MESSAGE: {
                try {
                    String target = this.in.readLine();
                    String message = this.in.readLine();
                    System.out.println("target: " + target);
                    System.out.println("message: " + message);
                    JSONObject data = new JSONObject();
                    data.put("action", ACTION_MESSAGE);
                    data.put("id", this.id);
                    data.put("source", id);
                    data.put("target", target);
                    data.put("message", message);
                    myClassEventManager.fireMyEvent(new MyEvent(this, data));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                break;
            }
            case ACTION_FILE: {
                try {
                    String target = this.in.readLine();
                    String fileName = this.in.readLine();
                    System.out.println("fileName: " + fileName);
                    fileName = fileName.substring(fileName.indexOf(File.separator) + 1, fileName.length());
                    System.out.println("fileName cast: " + fileName);
                    byte[] receivedData = new byte[1024];
                    int sizeReceive;

                    String pathFile = FTPConfiguration.rootDirectory + File.separator + fileName;

                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pathFile));
                    while ((sizeReceive = bis.read(receivedData)) != -1) {
                        bos.write(receivedData, 0, sizeReceive);
                    }
                    bos.close();

                    JSONObject data = new JSONObject();
                    data.put("action", ACTION_FILE);
                    data.put("id", this.id);
                    data.put("source", id);
                    data.put("target", target);
                    data.put("file", pathFile);
                    myClassEventManager.fireMyEvent(new MyEvent(this, data));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                break;
            }
        }
    }


    public void sendListClients(String listClients) {
        System.out.println("Enviando LISTCLIENTS: ");
        out.println(ACTION_LIST_CLIENTS);
        sleepClient(100);
        out.println(listClients);
        sleepClient(100);
    }

    public void sendConnectClient(String clientIdNew, String clientUserNameNew) {
        System.out.println("Enviando clientNew: " + clientIdNew + " - " + clientUserNameNew);
        out.println(ACTION_CONNECT_CLIENT);
        sleepClient(100);
        out.println(clientIdNew);
        sleepClient(100);
        out.println(clientUserNameNew);
        sleepClient(100);
    }

    public void sendDisconnectClient(String clientId) {
        System.out.println("Enviando client disconect: " + clientId);
        out.println(ACTION_DISCONNECT_CLIENT);
        sleepClient(100);
        out.println(clientId);
        sleepClient(100);
    }

    public void sendMessage(String source, String target, String message) {
        System.out.println("Enviando clientNew: " + source + " - " + target + " - " + message);
        out.println(ACTION_MESSAGE);
        sleepClient(100);
        out.println(source);
        sleepClient(100);
        out.println(message);
        sleepClient(100);
    }


    private void sleepClient(int sleep) {
        try {
            sleep(sleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
