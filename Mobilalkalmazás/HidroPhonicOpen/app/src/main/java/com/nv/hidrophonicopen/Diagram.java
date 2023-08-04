package com.nv.hidrophonicopen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

/*
 * Készítette: Némethy Viktor
 * A kiválasztott nap óránkénti hőmérsékleteinek ábrázoltatásáért felelős activity,
 * jjoe64 által készített GraphView segítségével ábrázoljuk egy oszlopdiagrammon az adatokat
 */
public class Diagram extends AppCompatActivity {

    long[] arr = new long[24];
    ValueEventListener dateListener;
    DatabaseReference dateRef;
    Button btnHome;
    TextView dateText;
    int flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagram);
        getSupportActionBar().hide();

        btnHome=(Button)findViewById(R.id.homeB);
        dateText = (TextView)findViewById(R.id.date);

        //A Pick osztály által üzenetben átadott kiválasztott dátum lekérdezése és tárolása
        Intent intent = getIntent();
        String str = intent.getStringExtra("message_key");
        String strNew = str.replace("/", "");
        dateText.setText(strNew);

        /*
        Kiválasztott dátum adatainak lekérdezése és egy tömbben letárolása,
        ha valamelyik óránál nem szerepel adat, akkor automatikusan 0-nak vesszük,
        ezután konfiguráljuk a GraphView-t, megadjuk hogy nézzen ki a diagram és ezután beadjuk neki az adatokat
         */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dateRef = FirebaseDatabase.getInstance().getReference().child("Date");

        dateListener = dateRef.addValueEventListener(new ValueEventListener()

        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (int i = 0; i < 24; i++) {
                    if(dataSnapshot.child(str + Integer.toString(i)).getValue() != null)
                        arr[i] = (long) dataSnapshot.child(str + Integer.toString(i)).getValue();
                    else
                        arr[i] = 0;
                }

                if(flag == 1) {
                    GraphView graph = (GraphView) findViewById(R.id.graph);

                    BarGraphSeries<DataPoint> series = new BarGraphSeries<>(getDataPoint());
                    graph.addSeries(series);

                    series.setColor(Color.rgb(56,120,29));

                    series.setSpacing(50);
                    series.setAnimated(true);


                    // X tengely manuális megadása
                    graph.getViewport().setXAxisBoundsManual(true);
                    graph.getViewport().setMinX(-1);
                    graph.getViewport().setMaxX(24);

                    // Y tengely manuális megadása
                    graph.getViewport().setYAxisBoundsManual(true);
                    graph.getViewport().setMinY(0);
                    graph.getViewport().setMaxY(40);
                    flag = 0;

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }

        });

        //Visszatérés a főoldalra
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                if(dateListener!=null)
                    dateRef.child("Date").removeEventListener(dateListener);
                dateListener = null;

                flag = 0;

                startActivity(intent);
                finish();
            }
        });

    }

    //onDestroy esetén listener eltávolítása
    @Override
    protected void onDestroy () {
        super.onDestroy();
        if(dateListener!=null)
            dateRef.removeEventListener(dateListener);
    }

    //vissza gomb megnyomása esetén listener eltávolítása
    @Override
    public void onBackPressed() {

        super.onBackPressed();
        if(dateListener!=null)
            dateRef.removeEventListener(dateListener);
    }

    /*
     * A metódusba beadjuk az adattömbünket és átalakítja ábrázolható adatpontokká
     * Visszatér ábrázolható formába hozott adatokkal
     */
    private DataPoint[] getDataPoint() {
        DataPoint[] dp = new DataPoint[24];
        for(int i=0;i<24;i++){
            dp[i] = new DataPoint(i, arr[i]);
        }
        return dp;
    }

}