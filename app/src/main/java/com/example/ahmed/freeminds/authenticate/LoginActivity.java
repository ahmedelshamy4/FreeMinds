package com.example.ahmed.freeminds.authenticate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ahmed.freeminds.MainActivity;
import com.example.ahmed.freeminds.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mEmail, mPassword;
    Button buttonRegister, buttonLogin;
    TextView textViewForget;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener listener;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
        };
        initViews();
        hideSoftKeyboard();

    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(listener);
    }


    private void initViews() {
        mEmail = findViewById(R.id.et_email);
        mPassword = findViewById(R.id.et_password);

        textViewForget = findViewById(R.id.tv_forget);
        textViewForget.setOnClickListener(this);

        mProgressBar = findViewById(R.id.progress_bar);

        buttonLogin = findViewById(R.id.btn_Login);
        buttonLogin.setOnClickListener(this);

        buttonRegister = findViewById(R.id.btn_Login_register);
        buttonRegister.setOnClickListener(this);

    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_Login: {
                checkLogin();
                break;
            }
            case R.id.tv_forget: {
                startActivity(new Intent(this, passwordForgetActivity.class));
                break;
            }
            case R.id.btn_Login_register: {
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            }

        }
    }

    private void checkLogin() {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "User has been login", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidUserException
                                || task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Wrong Email or Password", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);

                        } else {
                            Toast.makeText(LoginActivity.this, "Error_in_connection", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);

                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "You didn't fill in all the fields.", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);

        }
    }

}
