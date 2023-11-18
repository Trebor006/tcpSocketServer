package com.fldsmdfr;

import com.fldsmdfr.event.MyClass;
import com.fldsmdfr.event.MyEvent;
import com.fldsmdfr.event.MyEventListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebSocketServer {

  public static void main(String[] args) {
    int port = 8080;

    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Servidor WebSocket en ejecución en el puerto " + port);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        System.out.println(
            "Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress());


        MyClass c = new MyClass();
        c.addMyEventListener(new MyEventListener() {
          public void myEventOccurred(MyEvent evt) {
            System.out.println("fired");
          }
        });

        WebSocketHandler handler = new WebSocketHandler(clientSocket);
        handler.start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
