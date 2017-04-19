package cmu.miot.smartdevice;


import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "MainActivity";
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference messageRef = database.getReference("message");
    DatabaseReference sensorRef = database.getReference("sensors");
    DatabaseReference actuatorRef = database.getReference("actuators");
    private SensorManager mSensorManager;
    private Sensor currentSensor;
    List<Sensor> mSensorList;

    private CameraManager mCameraManager;
    private TextToSpeech textSpeaker;
    private Vibrator phoneVibrator;
    private  TextView welcome_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testFirebase();

        //fetch sensor manager service
        mSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        welcome_text = (TextView) findViewById(R.id.welcome_text) ;

        //fetch all the available sensors

        //code to check if camera is available and add flash settings
        try{

            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics("0");
            boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            //DatabaseReference flashActuator = actuatorRef.
            if (flashAvailable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    actuatorRef.child("333").child("available").setValue("off");
                    actuatorRef.child("333").child("val").setValue(false);
                    actuatorRef.child("333").child("val").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String command = dataSnapshot.getValue().toString();

                                try {
                                    if (command.equals("on")) {
                                    mCameraManager.setTorchMode("0",true);}
                                    else {
                                        mCameraManager.setTorchMode("0",false);
                                    }

                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }

                            }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    actuatorRef.child("333").child("available").setValue(false);
                }

            }

        //setting up text to Speech and firbase listeners
        textSpeaker = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textSpeaker.setLanguage(Locale.US);
                    actuatorRef.child("222").child("available").setValue(true);
                }
                else {
                    actuatorRef.child("222").child("available").setValue(false);
                }
            }
        });

        actuatorRef.child("222").child("val").setValue("");
        actuatorRef.child("222").child("val").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String speakerText = dataSnapshot.getValue().toString();
                textSpeaker.speak(speakerText, TextToSpeech.QUEUE_FLUSH, null, null);
                if(!textSpeaker.isSpeaking()){
                actuatorRef.child("222").child("val").setValue("");}

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.toString());
            }
        });

            // setting up phone vibrator

        phoneVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if(phoneVibrator.hasVibrator()){
                actuatorRef.child("444").child("available").setValue(true);
                actuatorRef.child("444").child("val").setValue(0);
                actuatorRef.child("444").child("val").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String timeInterval = dataSnapshot.getValue().toString();
                        if (timeInterval.matches("[0-9]+") && timeInterval.length() > 3) {
                            Long timer = Long.parseLong(timeInterval);
                            phoneVibrator.vibrate(timer);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
            else{
                actuatorRef.child("444").child("available").setValue(false);
            }

        if(welcome_text.isCursorVisible()){
            actuatorRef.child("111").child("available").setValue(true);
            actuatorRef.child("111").child("val").setValue("white");
            actuatorRef.child("111").child("val").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String screenColor = dataSnapshot.getValue().toString();
                    int colorCode = -1;
                    try {
                        colorCode = Color.parseColor(screenColor);
                        welcome_text.setBackgroundColor(colorCode);
                    }
                    catch (Exception e){
                        welcome_text.setBackgroundColor(colorCode);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else{
            actuatorRef.child("111").child("available").setValue(false);
        }

            mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

            //sensorRef.setValue("");
            for (int i=0;i<mSensorList.size();i++){
                currentSensor= mSensorList.get(i);
                sensorRef.child(Integer.toString(currentSensor.getType())).child("name").setValue(currentSensor.getName());
                sensorRef.child(Integer.toString(currentSensor.getType())).child("val").setValue(0);
                //Registerlistener for each sensor and fetch values at one second interval
                mSensorManager.registerListener(this,mSensorList.get(i),1000000);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void testFirebase(){

        try {
            messageRef.setValue("Hello, World!");
            messageRef = database.getReference("device");

            messageRef.setValue(Build.MODEL);


            messageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class);
                    welcome_text.setText("Welcome ... this is a  " + value);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
        catch (Exception e){
            welcome_text.setText("Error connecting with firebase");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        DatabaseReference sensorValueRef  = sensorRef.child(Integer.toString(event.sensor.getType())).child("val");
        //if event holds only a single value update only value
        if(event.values.length ==1)
        {
            sensorValueRef.setValue(event.values[0]);
        }
        //if event holds more than one value update only value with x,y,(z)
        if(event.values.length ==2)
        {
            sensorValueRef.child("x").setValue(event.values[0]);
            sensorValueRef.child("y").setValue(event.values[1]);
        }
        if(event.values.length ==3)
        {
            if(event.values[1]==0 && event.values[2]==0){
                sensorValueRef.setValue(event.values[0]);
            }
            else {
                sensorValueRef.child("x").setValue(event.values[0]);
                sensorValueRef.child("y").setValue(event.values[1]);
                sensorValueRef.child("z").setValue(event.values[2]);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        sensorRef.setValue("");
        for (int i=0;i<mSensorList.size();i++){
            currentSensor= mSensorList.get(i);
            sensorRef.child(Integer.toString(currentSensor.getType())).child("name").setValue(currentSensor.getName());
            sensorRef.child(Integer.toString(currentSensor.getType())).child("val").setValue(0);
            //Registerlistener for each sensor and fetch values at one second interval
            mSensorManager.registerListener(this,mSensorList.get(i),1000000);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
