package com.example.hursat.smartpass2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.Settings;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "RegisterAct";
    private FirebaseDatabase database;
    private StorageReference fbStorageRef;

    private EditText txtEmail;
    private EditText txtName;
    private EditText txtSurname;
    private EditText txtPass;
    private EditText txtRepeatPass;
    private Button btnSignUp;
    private TextView linkLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtEmail = (EditText) findViewById(R.id.register_email);
        txtName = (EditText) findViewById(R.id.register_name);
        txtSurname = (EditText) findViewById(R.id.register_surname);
        txtPass = (EditText) findViewById(R.id.register_pass);
        txtRepeatPass = (EditText) findViewById(R.id.register_repeat_pass);
        btnSignUp = (Button) findViewById(R.id.register_sign_up);
        linkLogin = (TextView) findViewById(R.id.register_link_login);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newUserEmail = txtEmail.getText().toString();
                String newUserName = txtName.getText().toString();
                String newUserSurname = txtSurname.getText().toString();
                String newUserPass = txtPass.getText().toString();
                String newUserRepeatPass = txtRepeatPass.getText().toString();

                if(validateInfo(newUserEmail, newUserPass, newUserRepeatPass)){

                    User newUser = new User(newUserEmail, newUserName, newUserSurname);
                    signUp(newUser, newUserPass);

                };

            }
        });

        final Intent toLoginPage = new Intent(this,MainActivity.class);

        linkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(toLoginPage);
            }
        });

        mAuth = FirebaseAuth.getInstance();

    }

    public boolean validateInfo(String email, String pass, String repeatPass){

        boolean result = false;

        if(email.contains("@") && pass.length() >= 6 && pass.equals(repeatPass)){
            result = true;
        }

        return result;
    }

    public void signUp(final User user, String pass){

        final ProgressDialog progDialog = new ProgressDialog(RegisterActivity.this);
        progDialog.setIndeterminate(true);
        progDialog.setMessage("Registering");
        progDialog.show();

        final Intent toLoginPage = new Intent(this,MainActivity.class);

        mAuth.createUserWithEmailAndPassword(user.email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        if(task.isSuccessful()){

                            database = FirebaseDatabase.getInstance();
                            DatabaseReference registerRef = database.getReference();
                            String uid = task.getResult().getUser().getUid();

                            registerRef.child("users").child(uid).setValue(user);

                            progDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                            startActivity(toLoginPage);

                        }

                        if (!task.isSuccessful()) {
                            progDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Sign Up Failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}
