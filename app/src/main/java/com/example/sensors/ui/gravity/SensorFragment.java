package com.example.sensors.ui.gravity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sensors.R;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SensorFragment extends Fragment implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private Sensor gyroscopeSensor;

    private TextView gravityXTextView;
    private TextView gravityYTextView;
    private TextView gravityZTextView;
    private TextView rotationXTextView;
    private TextView rotationYTextView;
    private TextView rotationZTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);

        // Initialiser les TextView
        gravityXTextView = view.findViewById(R.id.gravity_x_value);
        gravityYTextView = view.findViewById(R.id.gravity_y_value);
        gravityZTextView = view.findViewById(R.id.gravity_z_value);
        rotationXTextView = view.findViewById(R.id.rotation_x_value);
        rotationYTextView = view.findViewById(R.id.rotation_y_value);
        rotationZTextView = view.findViewById(R.id.rotation_z_value);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Obtenir le SensorManager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        // Initialiser les capteurs
        if (sensorManager != null) {
            gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Enregistrer les listeners des capteurs
        if (sensorManager != null) {
            if (gravitySensor != null) {
                sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                gravityXTextView.setText("Capteur de gravité non disponible");
            }

            if (gyroscopeSensor != null) {
                sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                rotationXTextView.setText("Gyroscope non disponible");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Désactiver les capteurs pour économiser la batterie
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Récupérer les valeurs du capteur et les afficher
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            // Mesure de la gravité sans accélération (m/s²)
            float gravityX = event.values[0];
            float gravityY = event.values[1];
            float gravityZ = event.values[2];

            gravityXTextView.setText(String.format("X: %.2f m/s²", gravityX));
            gravityYTextView.setText(String.format("Y: %.2f m/s²", gravityY));
            gravityZTextView.setText(String.format("Z: %.2f m/s²", gravityZ));
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Mesure du taux de rotation (rad/s)
            float rotationX = event.values[0];
            float rotationY = event.values[1];
            float rotationZ = event.values[2];

            rotationXTextView.setText(String.format("X: %.2f rad/s", rotationX));
            rotationYTextView.setText(String.format("Y: %.2f rad/s", rotationY));
            rotationZTextView.setText(String.format("Z: %.2f rad/s", rotationZ));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Gérer les changements de précision du capteur si nécessaire
    }
}