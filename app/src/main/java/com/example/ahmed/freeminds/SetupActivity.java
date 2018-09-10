package com.example.ahmed.freeminds;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ahmed.freeminds.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {

    private EditText setupName;
    private Button setupButton;
    private ImageButton setUpImage;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    String user_id;
    Uri mainImageUri = null;
    Boolean isChanged = false;
    Bitmap compressedProfileImageFile;
    boolean username_exists = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user_id = mAuth.getCurrentUser().getUid();

        initViews();
        setFirestoreNameAndPicuter();
        onClicks();
    }


    private void setFirestoreNameAndPicuter() {
        mProgressBar.setVisibility(View.VISIBLE);
        firebaseFirestore.collection(Constants.USER_COLLECTION).document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString(Constants.USER_NAME);
                        String image = task.getResult().getString(Constants.USER_IMAGE);
                        setupName.setText(name);
                        mainImageUri = Uri.parse(image);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.mipmap.ic_account);

                        Glide.with(SetupActivity.this)
                                .setDefaultRequestOptions(placeholderRequest).load(image).into(setUpImage);

                    } else {
                        Toast.makeText(SetupActivity.this, "Enter your Account Details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SetupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void initViews() {
        setUpImage = findViewById(R.id.userImg);
        setupName = findViewById(R.id.et_username);
        setupButton = findViewById(R.id.setup_btn);
        mProgressBar = findViewById(R.id.progress_bar);
    }

    private void onClicks() {
        setUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            + ContextCompat.checkSelfPermission(SetupActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        Toast.makeText(SetupActivity.this, "Grant Storage Read & Write Permission", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                    } else {

                        imagePicker();

                    }

                } else {
                    imagePicker();
                }
            }
        });

        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSave();
            }
        });
    }

    private void startSave() {
        final String name = setupName.getText().toString();
        if (!TextUtils.isEmpty(name) && mainImageUri != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            if (isChanged) {
                user_id = mAuth.getCurrentUser().getUid();
                File newImageFile = new File(mainImageUri.getPath());
                try {
                    compressedProfileImageFile = new Compressor(SetupActivity.this)
                            .setMaxHeight(100)
                            .setMaxWidth(100)
                            .setQuality(2)
                            .compressToBitmap(newImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedProfileImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] profile_image_data = baos.toByteArray();
                Toast.makeText(SetupActivity.this, "Uploading image", Toast.LENGTH_SHORT).show();

                UploadTask uploadTask = storageReference
                        .child(Constants.USER_PROFILE_IMAGE).child(user_id + ".jpg").putBytes(profile_image_data);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storeFireStore(taskSnapshot, name);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetupActivity.this, "Image Upload error", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);

                    }
                });


            } else {
                storeFireStore(null, name);

            }
        } else {
            Toast.makeText(this, "Select profile image , enter name", Toast.LENGTH_SHORT).show();
        }
    }

    private void storeFireStore(final UploadTask.TaskSnapshot taskSnapshot, final String name) {
        final Uri download_uri;
        if (taskSnapshot != null) {
            download_uri = taskSnapshot.getDownloadUrl();
        } else {
            download_uri = mainImageUri;
        }
        final Map<String, String> userMap = new HashMap<>();
        userMap.put(Constants.USER_NAME, name);
        userMap.put(Constants.USER_IMAGE, download_uri.toString());

        CollectionReference allUsersRef = firebaseFirestore.collection("Users");
        allUsersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
//                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
//                        String userName;
//                        userName = documentSnapshot.getString(Constants.USER_NAME);
//                        if (!documentSnapshot.getId().equals(mAuth.getCurrentUser().getUid())) {
//                            if (!TextUtils.isEmpty(userName)) {
//                                if (userName.equals(name)) {
//                                    Toast.makeText(SetupActivity.this, "Username Already Exists ", Toast.LENGTH_SHORT).show();
//                                    mProgressBar.setVisibility(View.INVISIBLE);
//                                    username_exists = true;
//                                    return;
//
//                                } else {
//                                    username_exists = false;
//                                }
//                            }
//                        }
//                    }
                    if (!username_exists) {
                        firebaseFirestore.collection(Constants.USER_COLLECTION).document(user_id).set(userMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SetupActivity.this, "The user settings are updated", Toast.LENGTH_SHORT).show();
                                            Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                            startActivity(mainIntent);
                                            finish();
                                        } else {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                });
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        firebaseFirestore.collection(Constants.USER_COLLECTION).document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!name.equals(task.getResult().getString(Constants.USER_NAME)) && taskSnapshot != null) {

                                        final Map<String, Object> userMapWithoutUsername = new HashMap<>();
                                        userMapWithoutUsername.put(Constants.USER_NAME, name);
                                        userMapWithoutUsername.put(Constants.USER_IMAGE, download_uri.toString());

                                        firebaseFirestore.collection(Constants.USER_COLLECTION).document(user_id)
                                                .set(userMapWithoutUsername).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    mProgressBar.setVisibility(View.INVISIBLE);

                                                    Toast.makeText(SetupActivity.this, "The user settings are updated ", Toast.LENGTH_SHORT).show();
                                                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                } else {

                                                    mProgressBar.setVisibility(View.INVISIBLE);
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(SetupActivity.this, error, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SetupActivity.this, "Change Fields and save settings", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    mProgressBar.setVisibility(View.INVISIBLE);

                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    Toast.makeText(SetupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void imagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                setUpImage.setImageURI(mainImageUri);
                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
