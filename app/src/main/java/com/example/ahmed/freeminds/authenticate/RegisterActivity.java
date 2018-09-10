package com.example.ahmed.freeminds.authenticate;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ahmed.freeminds.R;
import com.example.ahmed.freeminds.SetupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import static com.example.ahmed.freeminds.model.Check.doStringsMatch;
import static com.example.ahmed.freeminds.model.Check.isEmpty;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    EditText mEmail, mPassword, mConfirmPassword;
    Button buttonRegistered;
    FirebaseAuth mAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        initViews();
        hideSoftKeyboard();

    }

    private void initViews() {
        mProgressBar = findViewById(R.id.progress_bar);
        mEmail = findViewById(R.id.et_email);
        mPassword = findViewById(R.id.et_password);
        mConfirmPassword = findViewById(R.id.et_confirm_password);
        buttonRegistered = findViewById(R.id.btn_register);
        buttonRegistered.setOnClickListener(this);
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register:
                if (!isEmpty(mEmail.getText().toString()) &&
                        !isEmpty(mPassword.getText().toString()) &&
                        !isEmpty(mConfirmPassword.getText().toString())) {
                    if (doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())) {
                        startRegister(mEmail.getText().toString(), mPassword.getText().toString());
                    } else {
                        Toast.makeText(this, "Passwords do not Match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();

                }
        }
    }

    private void startRegister(final String email, final String password) {

        mProgressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "User has been Registered", Toast.LENGTH_SHORT).show();
                    Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                    startActivity(setupIntent);
                    finish();
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidUserException
                            || task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(RegisterActivity.this, "Failed to login. Invalid user", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error_in_connection", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}
