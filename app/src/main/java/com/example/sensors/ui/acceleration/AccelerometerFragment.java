package com.example.sensors.ui.acceleration;

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
public class AccelerometerFragment extends Fragment implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView xAxisText, yAxisText, zAxisText;
    private boolean isSensorRegistered = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accel, container, false);

        // Initialiser les TextView pour afficher les valeurs
        xAxisText = view.findViewById(R.id.x_axis_value);
        yAxisText = view.findViewById(R.id.y_axis_value);
        zAxisText = view.findViewById(R.id.z_axis_value);

        // Obtenir le SensorManager et le capteur d'accélération
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer == null) {
            // L'appareil ne possède pas de capteur d'accélération
            xAxisText.setText("Accéléromètre non disponible");
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null && !isSensorRegistered) {
            // Enregistrer le listener quand le fragment devient visible
            // Le deuxième paramètre est la fréquence d'échantillonnage (SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI,
            // SENSOR_DELAY_GAME, ou SENSOR_DELAY_FASTEST)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            isSensorRegistered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isSensorRegistered) {
            // Désinscrire le listener quand le fragment n'est pas visible
            sensorManager.unregisterListener(this);
            isSensorRegistered = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Les valeurs des axes sont en m/s²
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Mettre à jour l'interface utilisateur
            xAxisText.setText(String.format("X: %.2f m/s²", x));
            yAxisText.setText(String.format("Y: %.2f m/s²", y));
            zAxisText.setText(String.format("Z: %.2f m/s²", z));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Peut être utilisé pour gérer les changements de précision du capteur
    }

}