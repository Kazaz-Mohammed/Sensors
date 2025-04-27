package com.example.sensors.ui.boussole;

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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BoussFragment extends Fragment implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private ImageView compassImage;
    private TextView bearingText;
    private TextView directionText;

    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];

    private boolean isAccelerometerSet = false;
    private boolean isMagnetometerSet = false;

    private float currentDegree = 0f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bouss, container, false);

        compassImage = view.findViewById(R.id.compass_image);
        bearingText = view.findViewById(R.id.bearing_text);
        directionText = view.findViewById(R.id.direction_text);

        // Initialiser le gestionnaire de capteurs
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Enregistrer les écouteurs de capteurs
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Désinscrire les écouteurs de capteurs pour économiser la batterie
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            isAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            isMagnetometerSet = true;
        }

        if (isAccelerometerSet && isMagnetometerSet) {
            SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rotationMatrix, orientation);

            // Convertir les radians en degrés
            float azimuthInRadians = orientation[0];
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

            // Corriger l'angle pour qu'il soit toujours positif
            if (azimuthInDegrees < 0) {
                azimuthInDegrees += 360;
            }

            // Mettre à jour l'angle de rotation de l'image
            RotateAnimation rotateAnimation = new RotateAnimation(
                    currentDegree,
                    -azimuthInDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            rotateAnimation.setDuration(250);
            rotateAnimation.setFillAfter(true);

            compassImage.startAnimation(rotateAnimation);
            currentDegree = -azimuthInDegrees;

            // Afficher l'angle
            int bearing = Math.round(azimuthInDegrees);
            bearingText.setText(bearing + "°");

            // Afficher la direction cardinale
            directionText.setText(getDirectionFromDegrees(bearing));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Non utilisé, mais nécessaire pour implémenter SensorEventListener
    }

    private String getDirectionFromDegrees(int degrees) {
        if (degrees >= 337.5 || degrees < 22.5) {
            return "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            return "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            return "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            return "W";
        } else {
            return "NW";
        }
    }
}