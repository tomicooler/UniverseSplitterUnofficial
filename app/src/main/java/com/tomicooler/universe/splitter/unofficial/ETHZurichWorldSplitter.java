package com.tomicooler.universe.splitter.unofficial;

import static com.tomicooler.universe.splitter.unofficial.NetUtils.TIMEOUT;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ETHZurichWorldSplitter implements WorldSplitter {
    @Override
    public World split() throws WorldSplitException {
        try {
            final URL url = new URL("http://qrng.ethz.ch/api/randint?min=0&max=1&size=1");
            // Valid responses:
            //   {"result": [0]}
            //   {"result": [1]}
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
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
                final Object result = jsonObject.get("result");
                if (!(result instanceof JSONArray)) {
                    throw new WorldSplitException("result must be an array");
                }

                final JSONArray array = (JSONArray) result;
                if (array.length() != 1) {
                    throw new WorldSplitException("result array must contain exactly 1 element");
                }

                final int value = array.getInt(0);
                if (value < 0 || value > 1) {
                    throw new WorldSplitException("element must be either 0 or 1");
                }

                // Valid response
                return value == 1 ? World.A : World.B;
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new WorldSplitException(e);
        }
    }

    @Override
    public boolean available() {
        return NetUtils.pingURL("http://qrng.ethz.ch");
    }

    @Override
    public boolean throttled() {
        return false;
    }
}
