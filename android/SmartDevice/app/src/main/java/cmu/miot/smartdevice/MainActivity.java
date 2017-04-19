package cmu.miot.smartdevice;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "MainActivity";
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference messageRef = database.getReference("message");
    DatabaseReference sensorRef = database.getReference("sensors");
    private SensorManager mSensorManager;
    private Sensor currentSensor;
    List<Sensor> mSensorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testFirebase();

        //fetch sensor manager service
        this.mSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        //fetch all the available sensors
        this.mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        this.sensorRef.setValue("");
        for (int i=0;i<mSensorList.size();i++){
            this.currentSensor= mSensorList.get(i);
            this.sensorRef.child(Integer.toString(currentSensor.getType())).child("name").setValue(currentSensor.getName());
            this.sensorRef.child(Integer.toString(currentSensor.getType())).child("value").setValue(0);
            //Registerlistener for each sensor and fetch values at one second interval
           // this.mSensorManager.registerListener(this,currentSensor,1000000);

        }




    }

    private void testFirebase(){
        final TextView welcome_text = (TextView) findViewById(R.id.welcome_text) ;
        try {
            this.messageRef.setValue("Hello, World!");
            this.messageRef = database.getReference("device");

            this.messageRef.setValue(Build.MODEL);


            this.messageRef.addValueEventListener(new ValueEventListener() {
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

        DatabaseReference sensorValueRef  = this.sensorRef.child(Integer.toString(this.currentSensor.getType())).child("value");
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
        //mSensorManager.registerListener(this, currentSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mSensorManager.unregisterListener(this);
    }
}
