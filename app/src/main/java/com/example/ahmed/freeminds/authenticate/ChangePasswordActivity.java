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
import android.widget.Toast;

import com.example.ahmed.freeminds.MainActivity;
import com.example.ahmed.freeminds.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import static com.example.ahmed.freeminds.model.Check.doStringsMatch;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText current_password, new_password, new_password_confirm;
    private Button change_paassword_button;
    private ProgressBar mProgressBar;
    private FirebaseAuth mAuth;
    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        mAuth = FirebaseAuth.getInstance();
        user_email = mAuth.getCurrentUser().getEmail();
        initViews();
        onClicks();
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    private void initViews() {
        current_password = findViewById(R.id.et_current_password);
        new_password = findViewById(R.id.et_new_password);
        new_password_confirm = findViewById(R.id.et_confirm_password);
        change_paassword_button = findViewById(R.id.btn_change);
        mProgressBar = findViewById(R.id.progress_bar);
    }

    private void onClicks() {
        change_paassword_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String current_pass = current_password.getText().toString();
                String new_pass = new_password.getText().toString();
                final String confirm_new_pass = new_password_confirm.getText().toString();
                if (!TextUtils.isEmpty(current_pass) && !TextUtils.isEmpty(new_pass) &&
                        !TextUtils.isEmpty(confirm_new_pass)) {
                    if (doStringsMatch(new_pass, confirm_new_pass)) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        AuthCredential authCredential = EmailAuthProvider.getCredential(user_email, current_pass);
                        mAuth.getCurrentUser().reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mAuth.getCurrentUser().updatePassword(confirm_new_pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ChangePasswordActivity.this, "Password changed Successfully!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                                }
                                mProgressBar.setVisibility(View.INVISIBLE);

                            }
                        });

                    } else {
                        Toast.makeText(ChangePasswordActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(ChangePasswordActivity.this, "All Fields are Mandatory", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
