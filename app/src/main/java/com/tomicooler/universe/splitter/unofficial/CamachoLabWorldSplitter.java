package com.tomicooler.universe.splitter.unofficial;

import static com.tomicooler.universe.splitter.unofficial.NetUtils.TIMEOUT;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class CamachoLabWorldSplitter implements WorldSplitter {
    @Override
    public World split() throws WorldSplitException {
        try {
            final URL url = new URL("https://camacholab.ee.byu.edu/qrng/decimal/1");
            // Valid response:
            //   50
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
                int value = Integer.parseInt(builder.toString().trim());
                return (value % 2) == 0 ? World.A : World.B;
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
        return NetUtils.pingURL("https://camacholab.ee.byu.edu");
    }

    @Override
    public boolean throttled() {
        return false;
    }
}
