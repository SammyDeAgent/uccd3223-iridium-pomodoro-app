package cyto.iridium.iridium.ui.clock;

import android.content.Context;
import android.content.Intent;
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

    private TextView showpoint;
    //private long totaltime;
    private long difftime;

    private ClockViewModel clockViewModel;
    private FragmentClockBinding binding;

    private SharedPreferences sharepoint;
    SharedPreferences.Editor prefEditor;

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

        showpoint = binding.points;

        sharepoint = getActivity().getSharedPreferences("MySharePoint",0);
        prefEditor = sharepoint.edit();

        showpoint.setText(String.valueOf(sharepoint.getLong("point",0)));

        BtnSet.setOnClickListener(new View.OnClickListener() {
            /* To check if the user input is Empty or Zero(s) */
            @Override
            public void onClick(View v) {
                String input = EditTextIn.getText().toString();
                if (input.length() == 0) {
                    /* An Error Message if Empty Input to notify user */
                    Toast.makeText(getContext(), "Input can't be Empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                long msInput = Long.parseLong(input) * 60000;
                if (msInput == 0) {
                    /* An Error Message if input is Zero(s) to notify user */
                    Toast.makeText(getContext(), "Input Must Be Positive!", Toast.LENGTH_SHORT).show();
                    return;
                }
                prefEditor.putLong("totaltime", msInput);
                setTime(msInput);
                EditTextIn.setText("");
            }
        });

        /* To Begin or Pause the Timer */
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

        /* To Abandon the Timer @Reset */
        BtnAbandon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abandonTimer();
            }
        });

        prefEditor.putLong("point", 0);
        prefEditor.commit();
    }

    /* Time in Ms we want to Set Timer as and Minimise Keyboard after Set-ing Time */
    private void setTime(long ms) {
        StartTimeMs = ms;
        abandonTimer();
        minKeyboard();
    }

    /* To  Begin/Start Timer */
    private void beginTimer() {
        /* When Rotating phone, the timer slows down, this way we know the Exact time the Timer needs to end */
        EndTime = System.currentTimeMillis() + TimeLeftMs;

        /* After how many Ms will the onTick() be called */
        CountDownTimer = new CountDownTimer(TimeLeftMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TimeLeftMs = millisUntilFinished;
                updateCountDown();
                Long savedTotalTime = sharepoint.getLong("totaltime", 0);

                difftime = (savedTotalTime / 1000) - (TimeLeftMs / 1000);
                prefEditor.putLong("point", difftime);
                prefEditor.commit();

                Long totalPoints = sharepoint.getLong("totalpoint", 0)+1;
                prefEditor.putLong("totalpoint", totalPoints);
                prefEditor.commit();

                showpoint.setText(String.valueOf(difftime));
            }

            @Override
            public void onFinish() {
                TimerRunning = false;
                updateFields();
            }
        }.start();

        /* When Timer is Running, change the Abandon Btn's Visibility and Begin!/Pause words */
        TimerRunning = true;
        updateFields();
    }

    /* Pause the timer and change the Abandon Btn's Visibility and Begin!/Pause words */
    private void pauseTimer() {
        CountDownTimer.cancel();
        TimerRunning = false;
        updateFields();
    }

    /* Abandon/Reset the timer and change the Abandon Btn's Visibility */
    private void abandonTimer() {
        TimeLeftMs = StartTimeMs;
        updateCountDown();
        updateFields();
    }

    /* Change the Time Remaining shown */
    private void updateCountDown() {

        /* Change the time from Ms to Hrs, Min, Sec and Format it to be displayed to user */
        int hrs = (int) (TimeLeftMs / 1000) / 3600;
        int min = (int) ((TimeLeftMs / 1000) % 3600) / 60;
        int sec = (int) (TimeLeftMs / 1000) % 60;

        String formattedTimeLeft;

        /* Check if need to Show Hrs too or not */
        if (hrs > 0) {
            formattedTimeLeft = String.format(Locale.getDefault(), "%d:%02d:%02d", hrs, min, sec);

        } else {
            formattedTimeLeft = String.format(Locale.getDefault(), "%02d:%02d", min, sec);
        }

        CountDown.setText(formattedTimeLeft);
    }

    /* To change Abandon Btn's Visibility and or Begin/Start and Pause Btns */
    private void updateFields(){

        /* When Timer is Running, make the Input/Set/Abandon Btns Invisible */
        if (TimerRunning) {
            EditTextIn.setVisibility(View.INVISIBLE);
            BtnSet.setVisibility(View.INVISIBLE);
            BtnAbandon.setVisibility(View.INVISIBLE);
            BtnBeginPause.setText("Pause");
        } else {
            /* When Timer is Not Running, make the Input/Set/Abandon Btns Visible and change Pause/Begin */
            EditTextIn.setVisibility(View.VISIBLE);
            BtnSet.setVisibility(View.VISIBLE);
            BtnBeginPause.setText("Begin!");

            /* When Timer hits Zero, only can Abandon/Reset the Timer (Cause I don't want to Begin again until we have Re-set it) */
            if (TimeLeftMs < 1000) {
                BtnBeginPause.setVisibility(View.INVISIBLE);
            } else {
                /* When Timer isn't Zero, can still Pause/Begin */
                BtnBeginPause.setVisibility(View.VISIBLE);
            }

            /* When Timer is Paused and the Time Remaining is Less than the Initial Time, change Abandon Btn's Visibility */
            if (TimeLeftMs < StartTimeMs) {
                BtnAbandon.setVisibility(View.VISIBLE);
            } else {
                BtnAbandon.setVisibility(View.INVISIBLE);
            }
        }
    }

    /* To Minimise Keyboard */
    private void minKeyboard() {
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inMgrMgr = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inMgrMgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    /* Save Preferences if App is Closed/Stopped/Rotated */
    public void onStop() {
        super.onStop();


        /* Save Values/Data into Shared Preferences */
        SharedPreferences pref = this.getActivity().getSharedPreferences("Iridium_Clock", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();

        /* To Save current Timer related Values/Data */
        editor.putLong("startTimeMs", StartTimeMs);
        editor.putLong("msLeft", TimeLeftMs);
        editor.putBoolean("timerRunning", TimerRunning);
        editor.putLong("endTime", EndTime);

        editor.apply();

        /* Cancelling the Timer cause We are gonna Start it onStart anyway */
        if (CountDownTimer != null) {
            CountDownTimer.cancel();
        }
    }

    @Override
    /* Load Preferences when App is Opened from Background/Restarted/Rotated */
    public void onStart() {
        super.onStart();


        /* Get Values/Data from Shared Preferences */
        SharedPreferences pref = this.getActivity().getSharedPreferences("Iridium_Clock", Context.MODE_PRIVATE);

        /* To Load current Timer related Values/Data so that when After Rotate/Close App/App in Background, the Timer can continue */
        StartTimeMs = pref.getLong("startTimeMs", 600000);
        TimeLeftMs = pref.getLong("msLeft", StartTimeMs);
        TimerRunning = pref.getBoolean("timerRunning", false);

        updateCountDown();
        updateFields();

        /* To Restore Remaining Time after Rotate more accurately by using Initially Calculated End Time of Timer */
        if (TimerRunning) {
            EndTime = pref.getLong("endTime", 0);
            TimeLeftMs = EndTime - System.currentTimeMillis();

            /* To check if Timer has already Ended */
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