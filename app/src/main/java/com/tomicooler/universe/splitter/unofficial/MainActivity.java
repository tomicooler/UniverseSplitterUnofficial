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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String STATE_WELCOME_HAS_BEEN_SHOWN = "WELCOME_HAS_BEEN_SHOWN";
    private static final String STATE_ANU_LAST_USE_MILLIS = "ANU_LAST_USE_MILLIS";
    private static final long ONE_MINUTE = 60 * 1000;

    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ImageButton button;
    private EditText optionA;
    private EditText optionB;
    private AlertDialog dialog;
    private boolean welcomeHasBeenShown = false;
    private final List<Splitter> splitters = new ArrayList<>();
    private Splitter activeSplitter = null;

    private ANUWorldSplitter anuSplitter = null;

    private static class Splitter {
        WorldSplitter splitter;
        RadioButton button;

        Splitter(WorldSplitter splitter, RadioButton button) {
            this.splitter = splitter;
            this.button = button;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        long lastUseAnuMillis = 0;
        if (savedInstanceState != null) {
            welcomeHasBeenShown = savedInstanceState.getBoolean(STATE_WELCOME_HAS_BEEN_SHOWN, false);
            lastUseAnuMillis = savedInstanceState.getLong(STATE_ANU_LAST_USE_MILLIS, 0);
        }
        if (!welcomeHasBeenShown) {
            showWelcomeDialog();
            welcomeHasBeenShown = true;
        }

        // TODO: better abstraction
        anuSplitter = new ANUWorldSplitter(lastUseAnuMillis);
        splitters.add(new Splitter(new QRandomWorldSplitter(), findViewById(R.id.radio_qrandom)));
        splitters.add(new Splitter(new CamachoLabWorldSplitter(), findViewById(R.id.radio_camacho)));
        splitters.add(new Splitter(anuSplitter, findViewById(R.id.radio_anu)));
        for (Splitter splitter : splitters) {
            splitter.button.setEnabled(false);
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
                setSplitButtonEnable();
            }
        };

        optionA.addTextChangedListener(tw);
        optionB.addTextChangedListener(tw);

        RadioGroup group = findViewById(R.id.radio_group);
        group.setOnCheckedChangeListener((group1, checkedId) -> setSplitButtonEnable());

        setAvailableBackends();
        if ((System.currentTimeMillis() - lastUseAnuMillis) < ONE_MINUTE) {
            setAvailableBackend(splitters.get(2), ONE_MINUTE + 5 * 1000);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_WELCOME_HAS_BEEN_SHOWN, welcomeHasBeenShown);
        outState.putLong(STATE_ANU_LAST_USE_MILLIS, anuSplitter.getLastUseMillis());
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
        optionA.setEnabled(enable);
        optionB.setEnabled(enable);
        setSplitButtonEnable();
    }

    private void setSplitButtonEnable() {
        resetActiveSplitter();
        button.setEnabled(optionA.getText().length() > 0 && optionB.getText().length() > 0 && activeSplitter != null);
    }

    private void resetActiveSplitter() {
        activeSplitter = null;
        for (Splitter splitter : splitters) {
            if (splitter.button.isEnabled() && splitter.button.isChecked()) {
                activeSplitter = splitter;
                return;
            }
        }
    }

    private void splitTheWorld() {
        if (activeSplitter == null) {
            button.setEnabled(false);
            return;
        }

        executor.execute(() -> {
            String err = "";
            WorldSplitter.World w = WorldSplitter.World.A;
            try {
                w = activeSplitter.splitter.split();
            } catch (WorldSplitException e) {
                err = e.getMessage();
            }

            final String error = err;
            final WorldSplitter.World world = w;
            handler.post(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                if (error != null && !error.isEmpty()) {
                    builder.setTitle(R.string.error);
                    builder.setMessage(error);
                } else {
                    switch (world) {
                        case A:
                            builder.setTitle(R.string.action);
                            builder.setMessage(optionA.getText());
                            break;
                        case B:
                            builder.setTitle(R.string.action);
                            builder.setMessage(optionB.getText());
                            break;
                    }
                }
                dialog = builder.create();
                dialog.show();
                if (activeSplitter.splitter.throttled()) {
                    activeSplitter.button.setEnabled(false);
                    setAvailableBackend(activeSplitter, ONE_MINUTE + 5 * 1000);
                }
                setEnable(true);
            });
        });
    }

    private void setAvailableBackends() {
        for (int i = 0; i < splitters.size(); i++) {
            final int index = i;
            executor.execute(() -> {
                final Splitter splitter = splitters.get(index);
                setAvailableBackend(splitter, 0);
            });
        }
    }

    private void setAvailableBackend(final Splitter splitter, final long initialDelay) {
        executor.execute(() -> {
            if (initialDelay > 0) {
                try {
                    Thread.sleep(initialDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            final boolean available = splitter.splitter.available();
            handler.post(() -> {
                splitter.button.setEnabled(available);
                setSplitButtonEnable();
            });
        });
    }
}