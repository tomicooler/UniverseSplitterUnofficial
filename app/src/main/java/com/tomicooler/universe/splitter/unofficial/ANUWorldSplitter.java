package com.tomicooler.universe.splitter.unofficial;

import static com.tomicooler.universe.splitter.unofficial.NetUtils.TIMEOUT;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

// The QRNG API is limited to 1 requests per minute.
public class ANUWorldSplitter implements WorldSplitter {
    private long lastUseMillis = 0;

    ANUWorldSplitter(long lastUseMillis) {
        this.lastUseMillis = lastUseMillis;
    }

    @Override
    public World split() throws WorldSplitException {
        try {
            final URL url = new URL("https://qrng.anu.edu.au/API/jsonI.php?length=1&type=uint8");
            // Valid responses:
            //   {"type":"uint8","length":1,"data":[175],"success":true}
            final HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.setReadTimeout(TIMEOUT);
            try {
                final InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                final StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                final JSONObject jsonObject = new JSONObject(builder.toString());

                // Response validation
                final Object data = jsonObject.get("data");
                if (!(data instanceof JSONArray)) {
                    throw new WorldSplitException("data must be an array");
                }

                final JSONArray array = (JSONArray) data;
                if (array.length() != 1) {
                    throw new WorldSplitException("data array must contain exactly 1 element");
                }

                final int value = array.getInt(0);
                return (value % 2) == 0 ? World.A : World.B;
            } finally {
                lastUseMillis = System.currentTimeMillis();
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WorldSplitException(e);
        }
    }

    @Override
    public boolean available() {
        return ((System.currentTimeMillis() - lastUseMillis) > 60 * 1000) && NetUtils.pingURL("https://qrng.anu.edu.au");
    }

    @Override
    public boolean throttled() {
        return true;
    }

    public long getLastUseMillis() {
        return lastUseMillis;
    }
}
