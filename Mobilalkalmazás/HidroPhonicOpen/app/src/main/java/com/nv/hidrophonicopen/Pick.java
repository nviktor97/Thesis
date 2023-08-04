package com.nv.hidrophonicopen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*
 * Készítette: Némethy Viktor
 * Itt választhatja ki a felhasználó, hogy melyik nap mért hőmérsékleteit akarja ábrázolva látni,
 * ezt egy datepicker segítségével valósítottam meg,
 * a lekérdezés gombra kattintva kiírja, hogy szerepel-e mért adat a napra az adatbázisban mielőtt továbblépne,
 * ha szerepel adat a dátumot üzenetként továbbítjuk a Diagram activitynek
 */
public class Pick extends AppCompatActivity {

    DatePicker picker;
    Button btnGet;
    Button btnHome;
    TextView tvw;
    DatabaseReference myref;
    ValueEventListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick);
        getSupportActionBar().hide();

        tvw=(TextView)findViewById(R.id.textView1);
        picker=(DatePicker)findViewById(R.id.datePicker1);
        btnGet=(Button)findViewById(R.id.button1);
        btnHome=(Button)findViewById(R.id.homeP);

        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                myref = database.getReference();

                String str = picker.getYear()+"-"+(picker.getMonth() + 1)+"-"+picker.getDayOfMonth();

                listener = myref.addValueEventListener(new ValueEventListener(){
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(!dataSnapshot.child("Date").hasChild(str)){
                            tvw.setText("No Data");
                        }
                        else{
                            tvw.setText("Data Found");
                            myref.removeEventListener(listener);
                            String str2 = picker.getYear()+"-"+(picker.getMonth() + 1)+"-"+picker.getDayOfMonth() + "/";
                            Intent intent = new Intent(getApplicationContext(), Diagram.class);
                            intent.putExtra("message_key", str2);
                            startActivity(intent);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });

            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                if(listener!=null)
                    myref.removeEventListener(listener);
                finish();
            }
        });


    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        if(listener!=null)
            myref.removeEventListener(listener);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        if(listener!=null)
            myref.removeEventListener(listener);
    }
}