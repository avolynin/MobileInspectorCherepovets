package com.mic.MobileInsperctorCherepovets;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "TAG";

    EditText et_email, et_password;
    Button btn_registration;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        firebaseAuth = FirebaseAuth.getInstance();

        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);
        btn_registration = (Button) findViewById(R.id.btn_registration);
        btn_registration.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser !=null){
            reload();
        }
    }

    private void reload() {
        // Check if user is signed in (non-null) and update UI accordingly.
    }

    private void updateUI(FirebaseUser user){
        // Sign in success, update UI with the signed-in user's information
    }
    
    private void registration(){
        firebaseAuth.createUserWithEmailAndPassword(et_email.getText().toString().trim(), et_password.getText().toString().trim()).
                addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            updateUI(firebaseUser);
                        }else{
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "При регистрации возникли ошибки!", Toast.LENGTH_LONG);
                            updateUI(null);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_registration:
                registration();
                break;
        }
    }
}