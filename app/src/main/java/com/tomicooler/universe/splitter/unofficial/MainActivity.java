package com.tomicooler.universe.splitter.unofficial;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageButton button;
    private EditText optionA;
    private EditText optionB;
    private AlertDialog dialog;
    private boolean welcomeHasBeenShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            welcomeHasBeenShown = savedInstanceState.getBoolean("WELCOME_HAS_BEEN_SHOWN", false);
        }
        if (!welcomeHasBeenShown) {
            showWelcomeDialog();
            welcomeHasBeenShown = true;
        }

        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        button = findViewById(R.id.splitButton);
        button.setEnabled(false);
        button.setOnClickListener(view -> {
            setEnable(false);
            splitTheWorld();
        });

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                button.setEnabled(optionA.getText().length() > 0 && optionB.getText().length() > 0);
            }
        };

        optionA.addTextChangedListener(tw);
        optionB.addTextChangedListener(tw);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("WELCOME_HAS_BEEN_SHOWN", welcomeHasBeenShown);
        super.onSaveInstanceState(outState);
    }

    private void showWelcomeDialog() {
        final StringBuilder textBuilder = new StringBuilder();
        TextView msg = new TextView(this);
        try {
            Resources res = getResources();
            try (InputStream in = res.openRawResource(R.raw.welcome)) {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    textBuilder.append(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            textBuilder.append("error: ");
            textBuilder.append(e.getMessage());
        }

        msg.setText(HtmlCompat.fromHtml(textBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        msg.setClickable(true);
        msg.setMovementMethod(LinkMovementMethod.getInstance());
        msg.setPadding(30, 30, 30, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setView(msg);
        builder.setPositiveButton("OK", null);
        dialog = builder.create();
        dialog.show();
    }

    private void setEnable(boolean enable) {
        button.setEnabled(enable);
        optionA.setEnabled(enable);
        optionB.setEnabled(enable);
    }

    private void splitTheWorld() {
        executor.execute(() -> {
            final Response response = quantumAorB();
            handler.post(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                switch (response.result) {
                    case A:
                        builder.setTitle(R.string.action);
                        builder.setMessage(optionA.getText());
                        break;
                    case B:
                        builder.setTitle(R.string.action);
                        builder.setMessage(optionB.getText());
                        break;
                    case Error:
                        builder.setTitle(R.string.error);
                        builder.setMessage(response.error);
                        break;
                }
                dialog = builder.create();
                dialog.show();
                setEnable(true);
            });
        });
    }

    static final class Response {
        enum Result {
            A, B, Error,
        }

        Result result;
        String error;

        public Response(Result result, String error) {
            this.result = result;
            this.error = error;
        }
    }

    private Response quantumAorB() {
        try {
            final URL url = new URL("http://qrng.ethz.ch/api/randint?min=0&max=1&size=1");
            // Valid responses:
            //   {"result": [0]}
            //   {"result": [1]}
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5 * 1000); // 5 seconds
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
                    return new Response(Response.Result.Error, "result must be an array");
                }

                final JSONArray array = (JSONArray) result;
                if (array.length() != 1) {
                    return new Response(Response.Result.Error, "result array must contain exactly 1 element");
                }

                final int value = array.getInt(0);
                if (value < 0 || value > 1) {
                    return new Response(Response.Result.Error, "element must be either 0 or 1");
                }

                // Valid response
                return new Response(value == 1 ? Response.Result.A : Response.Result.B, "");
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(Response.Result.Error, e.getMessage());
        }
    }
}