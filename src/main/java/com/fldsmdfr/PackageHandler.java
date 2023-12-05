package com.fldsmdfr;

import com.fldsmdfr.event.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class PackageHandler extends Thread {

    private String packageString;
    private EventProcessPackageManager eventProcessPackageManager;

    public PackageHandler(String packageString, EventProcessPackageManager eventProcessPackageManage) {
        this.packageString = packageString;
        this.eventProcessPackageManager = eventProcessPackageManage;
    }

    @Override
    public void run() {
        DataPackage dataPackage = DataPackage.toDataPackage(packageString);
        executeAction(dataPackage);

    }

    private void executeAction(DataPackage dataPackage) {
        System.out.println("Accion recibida: " + dataPackage.getAction());
        switch (dataPackage.getAction()) {
            case Protocol.ACTION_USERNAME: {
                notifyEventProcessPackage(dataPackage);
                break;
            }
            case Protocol.ACTION_MESSAGE: {
                notifyEventProcessPackage(dataPackage);
                break;
            }
            case Protocol.ACTION_FILE: {
                FileInformation fileInformation = new FileInformation();
                fileInformation.toFileInformation(new JSONObject(dataPackage.getData()));

                fileInformation.idServer = String.valueOf(new Date().getTime());
                String fileName = fileInformation.name.substring(fileInformation.name.indexOf(File.separator) + 1, fileInformation.name.length());
                String pathFile = FTPConfiguration.rootDirectory + File.separator + fileName;
                fileInformation.filePathServer = pathFile;

                DataPackage dataPackageSend = new DataPackage(dataPackage.getSource(), dataPackage.getTarget(), fileInformation.toString(), Protocol.ACTION_FILE);
                notifyEventProcessPackage(dataPackageSend);
                break;
            }
            case Protocol.ACTION_FILE_PART: {
                try {
                    FileInformation fileInformation = new FileInformation();
                    fileInformation.toFileInformation(new JSONObject(dataPackage.getData()));

                    FileInformation fileInformationOut = FileTransfer.writePart(fileInformation);

                    DataPackage dataPackageSend = new DataPackage(dataPackage.getSource(), dataPackage.getTarget(), fileInformationOut.toString(), Protocol.ACTION_FILE_PART);
                    if(fileInformationOut.sizeSend >= fileInformationOut.size) {
                        dataPackageSend.setAction(Protocol.ACTION_FILE_END);
                    }
                    notifyEventProcessPackage(dataPackageSend);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }

    public void notifyEventProcessPackage(DataPackage dataPackage) {
        if (this.eventProcessPackageManager == null) {
            return;
        }
        eventProcessPackageManager.fireEventProcessPackage(new EventProcessPackage(this, dataPackage));
    }
}


