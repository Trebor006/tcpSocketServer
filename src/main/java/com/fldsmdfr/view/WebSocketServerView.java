package com.fldsmdfr.view;

import com.fldsmdfr.FTPConfiguration;
import com.fldsmdfr.WebSocketServer;
import com.fldsmdfr.event.EventServer;
import com.fldsmdfr.event.EventServerListener;
import com.fldsmdfr.event.EventServerManager;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WebSocketServerView implements EventServerListener {
    private JButton buttonStatus;
    private JTextField textFieldPort;
    private JTextArea textAreaLog;
    private JLabel labelPort;
    private JPanel jpanel;
    private JTextArea textAreaClients;
    private JTextField textFieldPath;
    private JLabel labelPath;
    private JLabel labelClients;

    WebSocketServer socketServer;
    EventServerManager eventServerManager;

    public WebSocketServerView() {
        textFieldPort.setText(FTPConfiguration.port + "");
        textFieldPath.setText(FTPConfiguration.rootDirectory);

        socketServer =  new WebSocketServer();
        eventServerManager = new EventServerManager();
        eventServerManager.addEventListener(this);
        socketServer.setEventServerManager(eventServerManager);

        buttonStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonStatus.setEnabled(false);
                if(socketServer.isStarted()) {
                    buttonStatus.setText("Deteniendo");
                    socketServer.stop();
                    return;
                }


                int port;
                try {
                    port = Integer.parseInt(textFieldPort.getText());
                } catch (Exception exception) {
                    buttonStatus.setEnabled(false);
                    JOptionPane.showMessageDialog(null, "El puerto es un numero entero", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                buttonStatus.setText("Iniciando");
                FTPConfiguration.port = port;
                socketServer.setPort(port);
                socketServer.start();
            }
        });
    }

    public JPanel getJpanel() {
        return jpanel;
    }




    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("WebSocketServer");
        frame.setContentPane(new WebSocketServerView().jpanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void eventServerOccurred(EventServer evt) {
        JSONObject data = evt.getData();

        // Paint Boton status
        if(socketServer.isStarted()) {
            buttonStatus.setText("Detener");
        } else {
            buttonStatus.setText("Iniciar");
        }
        buttonStatus.setEnabled(true);

        // Paint Clientes
        JSONObject clientsConnected =  data.getJSONObject("clientsConnected");
        StringBuilder clientsString = new StringBuilder();
        for (var key : clientsConnected.keySet()) {
            clientsString.append(key).append('\n');
        }
        textAreaClients.setText(clientsString.toString());

        // Paint Log
        String log = data.getString("log");
        textAreaLog.append(log + '\n');


    }
}
