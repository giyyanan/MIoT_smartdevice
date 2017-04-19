package cmu.miot.smartdevice;

import android.hardware.Sensor;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference messageRef = database.getReference("message");
    DatabaseReference sensorRef = database.getReference("sensors");
    private SensorManager mSensorManager;
    private Sensor currentSensor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testFirebase();

        //fetch sensor manager service
        mSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        //fetch all the available sensors
        List<Sensor> mSensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);


        for (int i=0;i<mSensorList.size();i++){
            currentSensor= mSensorList.get(i);

            sensorRef.child(currentSensor.getName()).child("value").setValue(0);
        }




    }

    private void testFirebase(){
        final TextView welcome_text = (TextView) findViewById(R.id.welcome_text) ;
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
}
