package com.example.sensors.ui.walking;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sensors.R;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class StepCounterFragment extends Fragment implements SensorEventListener{

    private static final String TAG = "StepCounterFragment";
    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 100;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private Sensor stepDetectorSensor; // Ajout d'un capteur alternatif
    private TextView stepCountTextView;
    private boolean isSensorPresent = false;

    // Variable pour stocker le nombre de pas depuis le dernier redémarrage
    private int stepCount = 0;
    // Variable pour stocker la valeur initiale du capteur
    private int initialStepCount = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_counter, container, false);
        stepCountTextView = view.findViewById(R.id.step_count_text_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "Fragment créé");

        // Demander la permission si nécessaire (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Demande de permission ACTIVITY_RECOGNITION");
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_RECOGNITION_REQUEST_CODE);
        } else {
            // La permission est déjà accordée, initialiser le capteur
            Log.d(TAG, "Permission déjà accordée ou non nécessaire, initialisation du capteur");
            initializeSensor();
        }
    }

    private void initializeSensor() {
        // Initialisation du SensorManager
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            // Vérifier et afficher les capteurs disponibles (pour le débogage)
            listAvailableSensors();

            // Utiliser le capteur TYPE_STEP_COUNTER qui compte les pas depuis le dernier redémarrage
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            // Capteur alternatif qui peut être utilisé si le premier n'est pas disponible
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

            if (stepSensor != null) {
                Log.d(TAG, "Capteur TYPE_STEP_COUNTER trouvé");
                isSensorPresent = true;
                stepCountTextView.setText("Prêt à compter les pas");
            } else if (stepDetectorSensor != null) {
                Log.d(TAG, "Capteur TYPE_STEP_COUNTER non disponible, utilisation de TYPE_STEP_DETECTOR");
                isSensorPresent = true;
                stepCountTextView.setText("Prêt à compter les pas (mode détecteur)");
            } else {
                Log.e(TAG, "Aucun capteur de pas n'est disponible");
                stepCountTextView.setText("Capteur de pas non disponible");
            }
        } else {
            Log.e(TAG, "SensorManager non disponible");
        }
    }

    private void listAvailableSensors() {
        Log.d(TAG, "Liste des capteurs disponibles:");
        for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            Log.d(TAG, "Capteur: " + sensor.getName() + " - Type: " + sensor.getType());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission ACTIVITY_RECOGNITION accordée");
                // Permission accordée, initialiser le capteur
                initializeSensor();
            } else {
                Log.e(TAG, "Permission ACTIVITY_RECOGNITION refusée");
                // Permission refusée
                Toast.makeText(requireContext(),
                        "Permission de reconnaissance d'activité refusée. Le compteur de pas ne fonctionnera pas.",
                        Toast.LENGTH_LONG).show();
                stepCountTextView.setText("Permission refusée");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume appelé");

        // Vérifier si le capteur est présent et la permission accordée
        if (isSensorPresent && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED)) {

            // Enregistrer le listener pour le capteur principal si disponible
            if (stepSensor != null) {
                Log.d(TAG, "Enregistrement du listener pour TYPE_STEP_COUNTER");
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            // Enregistrer le listener pour le capteur alternatif si disponible
            if (stepDetectorSensor != null) {
                Log.d(TAG, "Enregistrement du listener pour TYPE_STEP_DETECTOR");
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } else {
            Log.e(TAG, "Impossible d'enregistrer le listener: capteur absent ou permission non accordée");
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause appelé, désactivation des capteurs");

        // Désactivation du capteur lorsque le fragment n'est pas visible pour économiser la batterie
        if (isSensorPresent && sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Traiter les données du capteur selon le type
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];
            Log.d(TAG, "Événement TYPE_STEP_COUNTER reçu: " + totalSteps);

            // Initialiser le compteur de pas au premier événement
            if (initialStepCount == -1) {
                initialStepCount = totalSteps;
                Log.d(TAG, "Initialisation du compteur: " + initialStepCount);
            }

            // Calculer les pas depuis le redémarrage (ou depuis la première mesure)
            stepCount = totalSteps - initialStepCount;

            // Mise à jour de l'interface utilisateur
            updateStepCountDisplay();
        }
        else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // Pour le STEP_DETECTOR, chaque événement représente un pas
            Log.d(TAG, "Événement TYPE_STEP_DETECTOR reçu");
            stepCount++;

            // Mise à jour de l'interface utilisateur
            updateStepCountDisplay();
        }
    }

    private void updateStepCountDisplay() {
        Log.d(TAG, "Mise à jour de l'affichage: " + stepCount + " pas");
        stepCountTextView.setText("Pas: " + stepCount);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Précision du capteur modifiée: " + accuracy);
    }

    // Méthode pour réinitialiser le compteur
    public void resetCounter() {
        Log.d(TAG, "Réinitialisation du compteur");
        initialStepCount = -1;
        stepCount = 0;
        updateStepCountDisplay();
    }
}