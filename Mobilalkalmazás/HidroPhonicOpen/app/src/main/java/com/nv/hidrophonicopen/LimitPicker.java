package com.nv.hidrophonicopen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*
 * Készítette: Némethy Viktor
 * Itt változtathatja meg a felhasználó a magas és alacsony hőmérséklet határokat,
 * az aktuálisakat felül mindig kiírja,
 * a kiválasztást két numberpickerrel értem el,
 * az activity megnyitásakor mindig az aktuális határokra lesznek beállítva a numberpickerek
 */
public class LimitPicker extends AppCompatActivity {

    NumberPicker pickerLow, pickerHigh;
    TextView lowT, highT;
    Button saveBtn, homeBtn;
    DatabaseReference lowRef, highRef;
    ValueEventListener lowListener, highListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limit_picker);
        getSupportActionBar().hide();

        pickerLow = (NumberPicker) findViewById(R.id.PickerLow);
        pickerHigh = (NumberPicker) findViewById(R.id.PickerHigh);
        lowT = (TextView) findViewById(R.id.LowTempCurrent);
        highT = (TextView) findViewById(R.id.HighTempCurrent);
        saveBtn = (Button) findViewById(R.id.SaveLow);
        homeBtn = (Button) findViewById(R.id.HomeButton);

        pickerLow.setMinValue(0);
        pickerLow.setMaxValue(20);

        pickerHigh.setMinValue(21);
        pickerHigh.setMaxValue(50);


        lowRef = FirebaseDatabase.getInstance().getReference().child("Low_Temp");

        lowListener = lowRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                String lowData = snapshot.getValue().toString();
                lowT.setText("   Low Temperature Limit: " + lowData + "   ");
                pickerLow.setValue(Integer.parseInt(lowData));

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        highRef = FirebaseDatabase.getInstance().getReference().child("High_Temp");

        highListener = highRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                String highData = snapshot.getValue().toString();
                highT.setText("   High Temperature Limit: " + highData + "   ");
                pickerHigh.setValue(Integer.parseInt(highData));

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                DatabaseReference myrefau = database1.getReference("Low_Temp");
                int lowInt = pickerLow.getValue();
                myrefau.setValue(lowInt);

                FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                DatabaseReference myrefau2 = database2.getReference("High_Temp");
                int highInt = pickerHigh.getValue();
                myrefau2.setValue(highInt);

            }
        });


        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                if(lowListener!=null)
                    lowRef.removeEventListener(lowListener);
                if(highListener!=null)
                    highRef.removeEventListener(highListener);

                finish();
            }
        });



    }
}