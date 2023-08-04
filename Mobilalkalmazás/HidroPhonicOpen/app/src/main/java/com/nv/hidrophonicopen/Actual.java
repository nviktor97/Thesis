package com.nv.hidrophonicopen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/*
 * Készítette: Némethy Viktor
 * Az aktuális adatokat mehjelenítő activity
 * itt leolvasható az aktuális hőmérséklet, a vízszint és a víz pump állapota,
 * továbbá beállítható az automatikus üzemmód és bekapcsolható manuálisan a pumpa.
 */
public class Actual extends AppCompatActivity {

    TextView textTmp,textWL, textCon;
    Button btnPump,btnH;
    Switch autSwitch;
    DatabaseReference tempRef,waterRef,pumpRef,pushRef,autoRef;
    ValueEventListener tempListener,waterListener,pumpListener, autoListener;
    String PumpData, WaterData, AutoData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual);
        getSupportActionBar().hide();

        textTmp = (TextView) findViewById(R.id.textTemp);
        textWL = (TextView) findViewById(R.id.textWater);
        textCon = (TextView) findViewById(R.id.textPump);
        btnPump = (Button) findViewById(R.id.button);
        btnH = (Button) findViewById(R.id.HomeBtn);
        autSwitch = (Switch) findViewById(R.id.autoSwitch);

        SimpleDateFormat isoFormat = new SimpleDateFormat("H");
        isoFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        String hour2 = isoFormat.format(new Date());

        Date mTime = new Date();
        String text = new SimpleDateFormat("yyyy-M-d").format(mTime);


        /*
        Az aktuális hőmérséklet lekérdezéséért felelős adatbázis referencia és listener,
        a listner írja be a textviewba az aktuális hőmérsékletet
         */
        tempRef = FirebaseDatabase.getInstance().getReference().child("Date").child(text).child(hour2);

        tempListener = tempRef.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null) {
                    String TempData = dataSnapshot.getValue().toString();

                    textTmp.setText(TempData + " °C");
                }
                else
                    textTmp.setText("No data");

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });

        /*
        A vízszint lekérdezéséért felelős adatbázis referencia és listener,
        a listner írja be a textviewba a vízszintet
        és tőle függően aktiválja vagy deaktiválja a pumpa gombját
         */
        waterRef = FirebaseDatabase.getInstance().getReference().child("Water_Level");

        waterListener = waterRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {

                WaterData = dataSnapshot1.getValue().toString();

                if(WaterData.equals("1")) {
                    textWL.setText("High");
                    btnPump.setEnabled(false);
                    btnPump.setBackgroundColor(Color.parseColor("#808080"));

                }

                else if(WaterData.equals("0") && autSwitch.isChecked()) {
                    textWL.setText("Low");
                    btnPump.setEnabled(false);
                    btnPump.setBackgroundColor(Color.parseColor("#808080"));

                }

                else {
                    textWL.setText("Low");
                    btnPump.setEnabled(true);
                    btnPump.setBackgroundColor(Color.parseColor("#8BC34A"));

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                textWL.setText("No Data");

            }
        });

        /*
        A pumpa állapotáért felelős adatbázis referencia és listner,
        a listener küldi az adatbázisba a pumpa bekapcsolására az igényt és figyeli az állapotát
         */
        pumpRef = FirebaseDatabase.getInstance().getReference().child("Pump_Status");

        pumpListener = pumpRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {

                PumpData = dataSnapshot1.getValue().toString();

                if(PumpData.equals("1")) {
                    textCon.setText("On");
                    btnPump.setEnabled(false);
                    btnPump.setBackgroundColor(Color.parseColor("#808080"));
                }

                if(PumpData.equals("0"))
                    textCon.setText("Off");

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                textCon.setText("No Data");

            }
        });

        /*
        Az automatikus üzemmódért felelős adatbázis referencia és listener,
        a listener állítja be az adatbázis alapján az automata mód switch-jét,
        hogy mindig az aktuális állapotban jelenjen meg,
        ha be van kapcsolva az auto üzemmód, akkor deaktiválja a pumpa indító gombot
         */
        autoRef = FirebaseDatabase.getInstance().getReference().child("Auto_Mode");

        autoListener = autoRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot2) {

                AutoData = dataSnapshot2.getValue().toString();

                if(AutoData.equals("1")) {
                    autSwitch.setChecked(true);
                    btnPump.setEnabled(false);
                    btnPump.setBackgroundColor(Color.parseColor("#808080"));
                }

                if(AutoData.equals("0") && textWL.getText().equals("Low")) {
                    autSwitch.setChecked(false);
                    btnPump.setEnabled(true);
                    btnPump.setBackgroundColor(Color.parseColor("#8BC34A"));
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

        //a pumpa bekapcsoló gombjának onClick listenerje
        btnPump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushRef = FirebaseDatabase.getInstance().getReference().child("Pump_On");
                if(PumpData.equals("0") && WaterData.equals("0"))
                {
                    pushRef.setValue(1);
                    btnPump.setEnabled(false);
                    btnPump.setBackgroundColor(Color.parseColor("#808080"));
                }



            }
        });

        //A switch onCheckedChange listenerje, az automata üzemmódot állítja az adatbázisban
        autSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                    DatabaseReference myrefau = database1.getReference("Auto_Mode");
                    myrefau.setValue(1);

                } else {

                    FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                    DatabaseReference myrefau = database1.getReference("Auto_Mode");
                    myrefau.setValue(0);
                }
            }
        });

        //főoldalra visszatérést végző gomb listenerje
        btnH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                if(tempListener!=null)
                    tempRef.removeEventListener(tempListener);
                if(waterListener!=null)
                    waterRef.removeEventListener(waterListener);
                if(pumpListener!=null)
                    pumpRef.removeEventListener(pumpListener);


                finish();
            }
        });

    }

    //onDestroy esetén listenerek eltávolítása
    @Override
    protected void onDestroy () {
        super.onDestroy();
        if(tempListener!=null)
            tempRef.removeEventListener(tempListener);
        if(waterListener!=null)
            waterRef.removeEventListener(waterListener);
        if(pumpListener!=null)
            pumpRef.removeEventListener(pumpListener);


    }

    //a vissza gomb megnyomása esetén a listenerek eltávolítása
    @Override
    public void onBackPressed() {

        super.onBackPressed();
        if(tempListener!=null)
            tempRef.removeEventListener(tempListener);
        if(waterListener!=null)
            waterRef.removeEventListener(waterListener);
        if(pumpListener!=null)
            pumpRef.removeEventListener(pumpListener);

    }

}