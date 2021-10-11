package com.mic.MobileInsperctorCherepovets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "TAG";

    Button btn_enter;

    EditText et_email, et_password;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);

        btn_enter = (Button) findViewById(R.id.btn_enter);
        btn_enter.setOnClickListener(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null){
            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_createAccount:
                Intent intent = new Intent(this, RegistrationActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_enter:
                signIn();
                break;
        }
    }

    private void signIn(){
        firebaseAuth.signInWithEmailAndPassword(et_email.getText().toString().trim(), et_password.getText().toString().trim()).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "signInWithEmailAndPassword:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                            startActivity(intent);

                            updateUI(user);
                        }else{
                            Log.w(TAG, "signInWithEmailAndPassword:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "При авторизации возникла ошибка",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user){

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }
}