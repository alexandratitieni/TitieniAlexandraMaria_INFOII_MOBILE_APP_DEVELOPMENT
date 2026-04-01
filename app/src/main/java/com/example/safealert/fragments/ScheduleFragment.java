package com.example.safealert.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.safealert.R;
import com.example.safealert.activities.MainActivity;
public class ScheduleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        EditText etMessage = view.findViewById(R.id.etScheduledMessage);
        EditText etTime = view.findViewById(R.id.etScheduleTime);

        view.findViewById(R.id.btnStartSchedule).setOnClickListener(v -> {
            String msg = etMessage.getText().toString();
            String timeStr = etTime.getText().toString();

            if (!msg.isEmpty() && !timeStr.isEmpty()) {
                try {
                    long minutes = Long.parseLong(timeStr);

                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).startScheduledAlert(msg, minutes);
                } catch (NumberFormatException e) {
                    etTime.setError("Invalid time format!");
                }
            }
        });

        return view;
    }
}