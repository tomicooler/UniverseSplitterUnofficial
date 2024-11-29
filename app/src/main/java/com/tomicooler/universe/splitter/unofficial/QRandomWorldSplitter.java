package com.tomicooler.universe.splitter.unofficial;

import static com.tomicooler.universe.splitter.unofficial.NetUtils.TIMEOUT;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class QRandomWorldSplitter implements WorldSplitter {
    @Override
    public World split() throws WorldSplitException {
        try {
            final URL url = new URL("https://qrandom.io/api/random/int?min=0&max=1");
            // Valid responses:
            //   {"resultType":"randomInt","elapsedTime":0.0283,"id":"RjRDODBCQjctMjZCMi00MkJDLUIzMTYtQzVEQzE4RjNBQjA1","signature":"ORo2c9kiTQqifUjFnZ+0xYKgL6E6VEmJCcCrFgO6rDBsRcfPY9CYpeuKc+yx\/d5Lf7veDx9iOVSDLsp2y4xSf8lJEqPZr\/C5tYo2m6d9GQEJkBEobnTzpfIn7nONiS\/8B8VkLtrF3Q1hzDcvN4sWigdGYrpOMC7mC3Yz3bTI23Hu6EXtOjTXD7GAR1UJakbWbvGGPUTiNBvbJJecnt9wXEupSFBRuR5rfqpu0yQfeXnySGTrdbLotD7ZyH0W\/EASxN2ASqRt0c3MfMxPHVBiVBI1\/57a9Zkp42S9bVrsR6bGY1QuijEixcup\/KvyJzcuuNxLL7JMZqx7g5r5u5xamwA62rfWCJFUCHY9rSdNGt0Tw6gkb3pDZfxDp+KGKOtEwcq2ZZa9qwm74C5voaK1NNRsYssNI3n1i9n2rztDa8EpPyZJRLIcJMLqQ198Vl3QksVPyzts4714dDSZIEWaD57aobyH6iMLaD\/06BJcmt+RQ\/jw2T911ARHcnnyGkskHCcGsa47G3WNXWh5zfwpESzlpyWXUf8xJ5t8g2Zg6hVzMrc8tNsaP7U5Nba+ZvB0Wgajcu0cyWdclhHrlEmbjg7Yx9aT5maM0zfnPzEUapfao1bEWtlDxoDCltlBw9whxEduiVJwkgRw50Wbeb3HAiMcePZbl8kqk+r8TIMp0p5cWklQdKQKdLo0i4ikBtbO6lxxJR39VYnptaNm34b7j3NIoeGtTO7fNFIy1s4UmL93\/Bg93IpGl2wx\/aFFy6NEttHJNBJEofSrdOfX5y\/hqPfOmmlmT4uHJL1PHHuj6fox8zpZgrvz8fCk4hiqqb5uA1mrsKUKfiCEZyPalPoxsV7n30wKvMOUrGp7d6VjkXg=","timestamp":"2024-11-29T13:03:50Z","number":0,"message":"Request: From: 0, To: 1, ResultHash: af5570f5a1810b7af78caf4bc70a660f0df51e42baf91d4de5b2328de0e83dfc, Timestamp: 1732885430.627645"}
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
                final int value = jsonObject.getInt("number");
                if (value < 0 || value > 1) {
                    throw new WorldSplitException("number must be either 0 or 1");
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
        return NetUtils.pingURL("https://qrandom.io");
    }

    @Override
    public boolean throttled() {
        return false;
    }
}
