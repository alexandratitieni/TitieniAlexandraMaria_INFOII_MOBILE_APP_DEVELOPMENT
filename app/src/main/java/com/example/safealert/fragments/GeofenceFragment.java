package com.example.safealert.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.safealert.R;
import com.example.safealert.activities.MainActivity;

public class GeofenceFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geofence, container, false);

        EditText etCoords = view.findViewById(R.id.etGeofenceCoords);

        view.findViewById(R.id.btnSetGeofence).setOnClickListener(v -> {
            String coordsRaw = etCoords.getText().toString().trim();

            try {
                String[] parts = coordsRaw.split(",");
                if (parts.length == 2) {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lon = Double.parseDouble(parts[1].trim());

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).addSafeZone("USER_CUSTOM_ZONE", lat, lon, 200);
                        Toast.makeText(getContext(), "Safe zone set", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Invalid coordinates format (lat, lon)!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}