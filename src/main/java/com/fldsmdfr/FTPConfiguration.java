package com.fldsmdfr;

import java.io.File;

public class FTPConfiguration {
    public static int port = 8080;
    public static String rootDirectory = System.getProperty("user.home") + File.separator + "ftpServerDirectory";
}
