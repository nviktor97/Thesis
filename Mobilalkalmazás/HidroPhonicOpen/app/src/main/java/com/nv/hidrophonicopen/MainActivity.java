package com.nv.hidrophonicopen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/*
 * Készítette: Némethy Viktor
 * A projekt MainActivity-e, ahol a bejelentkezés történik
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth myAuth;
    private FirebaseAuth.AuthStateListener myAuthListener;

    private EditText myEmail, myPassword;
    private Button btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); // az action bar eltüntetése az oldal tetejéről

        myEmail = (EditText) findViewById(R.id.email);
        myPassword = (EditText) findViewById(R.id.password);
        btnSignIn = (Button) findViewById(R.id.email_sign_in_button);

        myAuth = FirebaseAuth.getInstance();

        //AuthListener a bejelentkezéshez, log és toast üzenet a sikeres bejelentkezésnél és kijelentkezésnél
        myAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    toastMessage("Signed in with: " + user.getEmail());
                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    startActivity(intent);
                } else {

                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Signed out.");
                }

            }
        };

        /*OnClickListener a bejelentkező gomhoz,
        kiolvassa az email és jelszó mezőből a beírtakat,
        megvizsgálja, hogy üres-e valamelyik mező,
        ha minden rendben van továbbb adja a beírtakat az autentikációs objektumnak,
        hogy elinduljon a bejelentkeztetés
         */
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = myEmail.getText().toString();
                String pass = myPassword.getText().toString();
                if(!email.equals("") && !pass.equals("")){
                    myAuth.signInWithEmailAndPassword(email,pass);
                }else{
                    toastMessage("The fields weren't filled in.");
                }
            }
        });

    }

    /*
     * onStart() az indításkor végrehajtandó utasítások helye,
     * az autentikációs objektumhoz hozzáadja az authlistenert
     *
     */
    @Override
    public void onStart() {
        super.onStart();
        myAuth.addAuthStateListener(myAuthListener);
    }

    /*
     * onStop() leálláskor végrehajtandó utasítások helye,
     * az autentikációs objektumból eltávolítja az authlistenert
     */
    @Override
    public void onStop() {
        super.onStop();
        if (myAuthListener != null) {
            myAuth.removeAuthStateListener(myAuthListener);
        }
    }

    /*
     * az oldal alján felbukkanó toast üzenet megjelenítése megadott stringgel
     */
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

}