package com.tomicooler.universe.splitter.unofficial;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetUtils {

    public static final int TIMEOUT = 3 * 1000;

    public static boolean pingURL(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
