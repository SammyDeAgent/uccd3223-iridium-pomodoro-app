package cyto.iridium.iridium.ui.clock;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

import cyto.iridium.iridium.R;
import cyto.iridium.iridium.databinding.FragmentClockBinding;

public class ClockFragment extends Fragment {

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

    private ClockViewModel clockViewModel;
    private FragmentClockBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        clockViewModel =
                new ViewModelProvider(this).get(ClockViewModel.class);

        binding = FragmentClockBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textGallery;
//        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        return root;
    }

    @Override
    public void onViewCreated(View view, @NonNull Bundle savedInstanceState){

        EditTextIn = (EditText) getView().findViewById(R.id.text_input);
        CountDown = (TextView) getView().findViewById(R.id.text_countdown);

        BtnSet = (Button) getView().findViewById(R.id.btn_set);
        BtnBeginPause = (Button) getView().findViewById(R.id.btn_begin_pause);
        BtnAbandon = (Button) getView().findViewById(R.id.btn_abandon);

        BtnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = EditTextIn.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(getContext(), "Input can't be Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long msInput = Long.parseLong(input) * 60000;
                if (msInput == 0) {
                    Toast.makeText(getContext(), "Input Must Be Positive!", Toast.LENGTH_SHORT).show();
                    return;
                }

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

    private void updateFields(){
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
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inMgrMgr = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inMgrMgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences pref = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
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
    public void onStart() {
        super.onStart();

        SharedPreferences pref = this.getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}