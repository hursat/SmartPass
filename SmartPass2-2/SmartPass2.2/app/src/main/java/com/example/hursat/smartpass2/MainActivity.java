package com.example.hursat.smartpass2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final String TAG = "MainAct";
    private static final String spName = "SmartPassSharedPreferences";

    private FirebaseDatabase database;
    private SharedPreferences sp;

    private EditText txtEmail;
    private EditText txtPass;
    private TextView linkRegister;
    private Button btnLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences(spName, Context.MODE_PRIVATE);

        txtEmail = (EditText) findViewById(R.id.main_email);
        txtPass = (EditText) findViewById(R.id.main_pass);
        btnLogIn = (Button) findViewById(R.id.main_log_in);
        linkRegister = (TextView) findViewById(R.id.main_link_register);

        final Intent toRegisterAct = new Intent(this, RegisterActivity.class);

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn(txtEmail.getText().toString(), txtPass.getText().toString());
            }
        });

        linkRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(toRegisterAct);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
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

    public void logIn(String email, String pass){

        final ProgressDialog progDialog = new ProgressDialog(MainActivity.this);
        progDialog.setIndeterminate(true);
        progDialog.setMessage("Logging In...");
        progDialog.show();

        final Intent toUserAct = new Intent(this, UserActivity.class);

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        if(task.isSuccessful()){

                            progDialog.dismiss();

                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("UID", task.getResult().getUser().getUid());
                            editor.commit();

                            startActivity(toUserAct);
                        }

                        if (!task.isSuccessful()) {

                            progDialog.dismiss();

                            Log.w(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(MainActivity.this, "Log In Failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}
