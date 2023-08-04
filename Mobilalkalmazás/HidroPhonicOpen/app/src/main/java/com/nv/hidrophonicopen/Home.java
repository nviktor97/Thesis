package com.nv.hidrophonicopen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
 * Ez az activity felelős a főmenüért, ide irányít a bejelentkezési activity,
 * gombok találhatóak benne, amik töbii activityhez vezetnek,
 * továbbá itt építi fel az értesítéseket az alacsony vízszintről és a túl magas vagy alacsony hőmérsékletről
 */
public class Home extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference waterRef,actualTempRef,lowTempRef,highTempRef;
    private Button btnPick,btnSignOut,btnActual, btnChange, btnLimit;
    ValueEventListener waterListener,actualTempListener,lowTempListener,highTempListener;
    public static int flag,flag2; //azért public static a két flag, hogy megmaradjanak a futás alatt és ne ismétlődjenek ugyanazon értesítések
    String low= "-100", high = "200";
    public static int toastFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        btnPick = (Button) findViewById(R.id.datePicker);
        btnSignOut = (Button) findViewById(R.id.email_sign_out_button);
        btnActual = (Button) findViewById(R.id.ActualButton);
        btnChange = (Button) findViewById(R.id.passChanger);
        btnLimit = (Button) findViewById(R.id.LimitButton);

        SimpleDateFormat isoFormat = new SimpleDateFormat("H");
        isoFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        String hour2 = isoFormat.format(new Date());

        Date mTime = new Date();
        String text = new SimpleDateFormat("yyyy-M-d").format(mTime);

        lowTempRef = FirebaseDatabase.getInstance().getReference().child("Low_Temp");
        lowTempListener = lowTempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                low = snapshot.getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        highTempRef = FirebaseDatabase.getInstance().getReference().child("High_Temp");
        highTempListener = highTempRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                high = snapshot.getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        //Notification csatornák és managerek megadása
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel waterChannel = new NotificationChannel("Water Notification", "Water Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationChannel highChannel = new NotificationChannel("High Notification", "High Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationChannel lowChannel = new NotificationChannel("Low Notification", "Low Notification", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(waterChannel);
            manager.createNotificationChannel(highChannel);
            manager.createNotificationChannel(lowChannel);
        }



        mAuth = FirebaseAuth.getInstance();




        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && toastFlag == 0) {

                    toastMessage("Signed in with: " + user.getEmail());
                    toastFlag = 1;
                }

            }
        };

        //vízszittel kapcsolatos adatbázis figyelés és értesítés alacsony vízszint esetén
        waterRef = FirebaseDatabase.getInstance().getReference().child("Water_Level");

        waterListener = waterRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {

                String WaterData = dataSnapshot1.getValue().toString();

                if(WaterData.equals("1")) {

                    flag = 0;
                }




                if(WaterData.equals("0")){

                    if(flag == 0) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(Home.this, "Water Notification");
                        builder.setContentTitle("Water Level Notification");
                        builder.setContentText("The Water Level Is Low");
                        builder.setSmallIcon(R.drawable.hidro_icon);
                        builder.setAutoCancel(true);

                        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(Home.this);
                        managerCompat.notify(1, builder.build());
                        flag = 1;
                    }
                }



            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value


            }
        });

        //Az aktuális hőmérséklet figyelése, szint átlépés esetén értesítés küldése
        actualTempRef = FirebaseDatabase.getInstance().getReference().child("Date").child(text).child(hour2);

        actualTempListener = actualTempRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot1) {





                int lowInt = Integer.parseInt(low);
                int highInt = Integer.parseInt(high);

                if (dataSnapshot1.getValue() != null) {

                    int act = ((Long) dataSnapshot1.getValue()).intValue();

                    if (act > highInt) {
                        if (flag2 == 0) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(Home.this, "High Notification");
                            builder.setContentTitle("Temperature Notification");
                            builder.setContentText("The Temperature is Too High");
                            builder.setSmallIcon(R.drawable.hidro_icon);
                            builder.setAutoCancel(true);

                            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(Home.this);
                            managerCompat.notify(2, builder.build());
                            flag2 = 1;
                        }


                    }

                    if (act < lowInt) {

                        if (flag2 == 0) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(Home.this, "Low Notification");
                            builder.setContentTitle("Temperature Notification");
                            builder.setContentText("The Temperature is Too Low");
                            builder.setSmallIcon(R.drawable.hidro_icon);
                            builder.setAutoCancel(true);

                            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(Home.this);
                            managerCompat.notify(3, builder.build());
                            flag2 = 1;
                        }
                    }

                    if (act > lowInt && act < highInt)
                        flag2 = 0;

                }




            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });




        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), Pick.class);
                startActivity(intent);

            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(waterListener!=null) {
                    waterRef.removeEventListener(waterListener);
                    waterListener = null;
                }
                if(actualTempListener!=null) {
                    actualTempRef.removeEventListener(actualTempListener);
                    actualTempListener = null;
                }
                mAuth.signOut();
                toastMessage("Signing Out...");
                toastFlag = 0;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnActual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), Actual.class);
                startActivity(intent);

            }
        });

        btnLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LimitPicker.class);
                startActivity(intent);

            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PasswChange.class);
                startActivity(intent);

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }



    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}