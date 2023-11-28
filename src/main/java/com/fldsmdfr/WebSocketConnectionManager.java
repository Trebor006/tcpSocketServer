package com.fldsmdfr;

import com.fldsmdfr.event.MyClass;
import com.fldsmdfr.event.MyEvent;
import com.fldsmdfr.event.MyEventListener;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class WebSocketConnectionManager extends Thread implements MyEventListener {

    private ServerSocket serverSocket;
    private volatile HashMap<String, WebSocketHandler> clients;
    private volatile boolean started;

    private MyClass myClass;

    public WebSocketConnectionManager() {
        myClass = new MyClass();
        myClass.addMyEventListener(this);
    }


    @Override
    public void run() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (!started) {
                    return;
                }

                String id = clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort();
                acceptClient(clientSocket, id);
            }
        } catch (IOException ex) {

        }
    }

    private void acceptClient(Socket clientSocket, String id) {
        WebSocketHandler client = new WebSocketHandler(clientSocket, id, myClass);
        System.out.println("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress());
        clients.put(id, client);
        client.start();
    }

    public void startManager(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clients = new HashMap<>();
        this.started = true;
        this.start();
    }

    public void stopManager() {
        this.started = false;
    }

    @Override
    public void myEventOccurred(MyEvent evt) {
        JSONObject data = (JSONObject) evt.getSource();
        String action =data.getString("action");
        String id = data.getString("id");
        WebSocketHandler client = clients.get(id);
        switch (action) {
            case WebSocketHandler.ACTION_USERNAME : {
                JSONObject listClients =  new JSONObject();
                for (var entry : clients.entrySet()) {
                    listClients.put(entry.getKey() , entry.getValue().userName);
                    if(!id.equals(entry.getKey())) {
                        entry.getValue().sendConnectClient(id, client.userName);
                    }
                }
                client.sendListClients(listClients.toString());
                break;
            }
            case WebSocketHandler.ACTION_MESSAGE: {
                String target = data.getString("target");
                String message = data.getString("message");
                if(target.equals("server")) {
                    return;
                }
                if(target.equals("all")) {
                    for (var entry : clients.entrySet()) {
                        if(!id.equals(entry.getKey())) {
                            entry.getValue().sendMessage(id, target, message);
                        }
                    }
                }

                WebSocketHandler clientTarget = clients.get(target);
                if(clientTarget != null) {
                    clientTarget.sendMessage(id, target, message);

                }
                break;
            }
        }
    }


}
