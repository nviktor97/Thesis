package com.nv.hidrophonicopen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/*
 * Készítette: Némethy Viktor
 * Ebben az activityben a felhasználó megváltoztathatja a jelszavát,
 * A jelszónak minimum 9 karakter hosszúnak kell lennie, ez a Firebase adatbázis szabálya,
 * én továbbá hozzáadtam megszorításokat, hogy nagybetű és számjegy is szerepeljen benne,
 * megerősítő mező is szerepel, ha sikeres, akkor egy textview megjelenik és kiírja, hogy megváltozott  jelszó,
 * ha nem felel meg a szabályoknak vagy nincs kitöltve valamelyik mező, akkor helytelen jelszót fog kiírni,
 * ha pedig az adatbázisba írás volt sikertelen már benne szereplő jelszó vagy egyéb probléma miatt, akkor fail-t fog kiírni
 */
public class PasswChange extends AppCompatActivity {

    private Button btnSave,btnHome;
    private TextView statText;
    private EditText newPassw,passAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passw_change);
        getSupportActionBar().hide();

        btnSave = (Button) findViewById(R.id.SaveButton);
        btnHome = (Button) findViewById(R.id.HomeButton);
        statText = (TextView) findViewById(R.id.status);
        newPassw = (EditText) findViewById(R.id.newPassField);
        passAgain = (EditText) findViewById(R.id.PassCheck);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Home.class);
                startActivity(intent);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String PassNew = newPassw.getText().toString();
                String PassAg = passAgain.getText().toString();
                char ch;
                int flag = 0, flag2 = 0;



                if(PassNew.length() >= 9) {
                    if(PassNew.equals(PassAg))
                    {
                        for(int i = 0; i< PassNew.length(); i++)
                        {
                            ch = PassNew.charAt(i);

                            if(Character.isDigit(ch))
                                flag=1;
                            if(Character.isUpperCase(ch))
                                flag2=1;

                        }


                    }

                }


                if(flag == 1 && flag2 == 1)
                {

                    user.updatePassword(PassNew)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        statText.setBackgroundResource(R.drawable.customframe);
                                        statText.setText("Password Changed");

                                        newPassw.setText("");
                                        passAgain.setText("");
                                    }
                                    else {
                                        statText.setBackgroundResource(R.drawable.customframe);
                                        statText.setText("Fail");
                                    }
                                }
                            });

                }

                else
                {
                    statText.setBackgroundResource(R.drawable.customframe);
                    statText.setText("Incorrect");
                }


            }
        });

    }
}