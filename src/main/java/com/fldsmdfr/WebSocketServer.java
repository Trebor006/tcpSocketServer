package com.fldsmdfr;

import com.fldsmdfr.event.EventServerManager;

import java.io.IOException;
import java.net.ServerSocket;

public class WebSocketServer {

    private ServerSocket serverSocket;
    private int port;
    private volatile boolean started;
    private WebSocketConnectionManager connectionManager;
    private EventServerManager eventServerManager; // Notificacion a la vista

    public static final int DEFAULT_PORT = 8080;

    public WebSocketServer(int port) {
        this.init(port);
    }

    public WebSocketServer() {
        this.init(DEFAULT_PORT);
    }

    private void init(int port) {
        this.port = port;
        this.started = false;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isStarted() {
        return started;
    }

    public void setEventServerManager(EventServerManager eventServerManager) {
        this.eventServerManager = eventServerManager;
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(port);
            this.started = true;
            String log = "Servidor WebSocket en ejecución en el puerto " + this.port;
            System.out.println(log);
            this.connectionManager = new WebSocketConnectionManager();
            connectionManager.setEventServerManager(eventServerManager);
            connectionManager.startManager(this.serverSocket);
            connectionManager.notifyEventServer(log);
        } catch (IOException e) {
            e.printStackTrace();
            this.started = false;
            String log = "Error al iniciar Servidor WebSocket en el puerto " + this.port;
            System.out.println(log);
            connectionManager.notifyEventServer(log);
        }
    }

    public void stop() {
        this.started = false;
        connectionManager.stopManager();
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String log = "Servidor WebSocket en Detenido ";
        System.out.println(log);
        connectionManager.notifyEventServer(log);
    }


    public static void main(String[] args) {
        int port = 8080;
        WebSocketServer webSocketServer =  new WebSocketServer(port);
        webSocketServer.start();

    }
}
