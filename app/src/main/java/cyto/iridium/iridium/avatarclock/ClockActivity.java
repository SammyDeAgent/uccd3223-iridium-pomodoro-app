package cyto.iridium.iridium.avatarclock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ClockActivity extends AppCompatActivity {
    private EditText EditTextIn;
    private TextView CountDown;
    private Button BtnSet;
    private Button BtnBeginPause;
    private Button BtnAbandon;

    private android.os.CountDownTimer CountDownTimer;

    private boolean TimerRunning;

    private long StartTimeMs;
    private long TimeLeftMs;
    private long EndTime;

    private Button treebutton;
    private TextView showpoint;
    private long totaltime;
    private long difftime;

    private SharedPreferences sharepoint;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        EditTextIn = findViewById(R.id.text_input);
        CountDown = findViewById(R.id.text_countdown);

        BtnSet = findViewById(R.id.btn_set);
        BtnBeginPause = findViewById(R.id.btn_begin_pause);
        BtnAbandon = findViewById(R.id.btn_abandon);

        showpoint = findViewById(R.id.points);

        BtnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = EditTextIn.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(ClockActivity.this, "Input can't be Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long msInput = Long.parseLong(input) * 60000;
                if (msInput == 0) {
                    Toast.makeText(ClockActivity.this, "Input Must Be Positive!", Toast.LENGTH_SHORT).show();
                    return;
                }

                totaltime = msInput;
                setTime(msInput);
                EditTextIn.setText("");
            }
        });

        BtnBeginPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TimerRunning) {
                    pauseTimer();
                } else {
                    beginTimer();
                }
            }
        });

        BtnAbandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abandonTimer();
            }
        });

        treebutton = findViewById(R.id.treeButton);
        treebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClockActivity.this, my.edu.utar.avatartesting.MainActivity.class);
                startActivity(intent);
            }
        });

        sharepoint = getSharedPreferences("MySharePoint", 0);
        prefEditor = sharepoint.edit();

        prefEditor.putLong("point", 0);
        prefEditor.commit();
    }

    private void setTime(long ms) {
        StartTimeMs = ms;
        abandonTimer();
        minKeyboard();
    }

    private void beginTimer() {
        EndTime = System.currentTimeMillis() + TimeLeftMs;

        CountDownTimer = new CountDownTimer(TimeLeftMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeLeftMs = millisUntilFinished;
                difftime = (totaltime / 1000) - (TimeLeftMs / 1000);
                prefEditor.putLong("point", difftime);
                prefEditor.commit();

                showpoint.setText(String.valueOf(difftime));

                updateCountDown();
            }

            @Override
            public void onFinish() {
                TimerRunning = false;
                updateFields();
            }
        }.start();

        TimerRunning = true;
        updateFields();
    }

    private void pauseTimer() {
        CountDownTimer.cancel();
        TimerRunning = false;
        updateFields();
    }

    private void abandonTimer() {
        TimeLeftMs = StartTimeMs;
        updateCountDown();
        updateFields();
    }

    private void updateCountDown() {
        int hrs = (int) (TimeLeftMs / 1000) / 3600;
        int min = (int) ((TimeLeftMs / 1000) % 3600) / 60;
        int sec = (int) (TimeLeftMs / 1000) % 60;

        String formattedTimeLeft;

        if (hrs > 0) {
            formattedTimeLeft = String.format(Locale.getDefault(), "%d:%02d:%02d", hrs, min, sec);

        } else {
            formattedTimeLeft = String.format(Locale.getDefault(), "%02d:%02d", min, sec);
        }

        CountDown.setText(formattedTimeLeft);
    }

    private void updateFields() {
        if (TimerRunning) {
            EditTextIn.setVisibility(View.INVISIBLE);
            BtnSet.setVisibility(View.INVISIBLE);
            BtnAbandon.setVisibility(View.INVISIBLE);

            BtnBeginPause.setText("Pause");
        } else {
            EditTextIn.setVisibility(View.VISIBLE);
            BtnSet.setVisibility(View.VISIBLE);
            BtnBeginPause.setText("Begin!");

            if (TimeLeftMs < 1000) {
                BtnBeginPause.setVisibility(View.INVISIBLE);
            } else {
                BtnBeginPause.setVisibility(View.VISIBLE);
            }

            if (TimeLeftMs < StartTimeMs) {
                BtnAbandon.setVisibility(View.VISIBLE);
            } else {
                BtnAbandon.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void minKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inMgrMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inMgrMgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putLong("startTimeMs", StartTimeMs);
        editor.putLong("msLeft", TimeLeftMs);
        editor.putBoolean("timerRunning", TimerRunning);
        editor.putLong("endTime", EndTime);

        editor.apply();

        if (CountDownTimer != null) {
            CountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);

        StartTimeMs = pref.getLong("startTimeMs", 600000);
        TimeLeftMs = pref.getLong("msLeft", StartTimeMs);
        TimerRunning = pref.getBoolean("timerRunning", false);

        updateCountDown();
        updateFields();

        if (TimerRunning) {
            EndTime = pref.getLong("endTime", 0);
            TimeLeftMs = EndTime - System.currentTimeMillis();

            if (TimeLeftMs < 0) {
                TimeLeftMs = 0;
                TimerRunning = false;
                updateCountDown();
                updateFields();
            } else {
                beginTimer();
            }
        }
    }
}
