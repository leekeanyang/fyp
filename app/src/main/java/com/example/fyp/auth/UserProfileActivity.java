package com.example.fyp.auth;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.fyp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private TextInputEditText edtDisplayName;
    private Button btnChangePhoto, btnSaveProfile;

    private FirebaseUser user;
    private Uri selectedImageUri;

    private StorageReference storageRef;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(imgProfile);
                }
            });

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_VIDEO_PICK = 1002;
    private static final int REQUEST_AUDIO_PICK = 1003;
    private static final int PERMISSION_REQUEST_CODE = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        imgProfile = findViewById(R.id.imgProfile);
        Button btnUploadPhoto = findViewById(R.id.btnUploadPhoto);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnUploadPhoto.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                showImageSourceDialog();
            }
        });
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".provider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();

            if (requestCode == REQUEST_IMAGE_PICK) {
                // ✅ Handle image
                Log.d("MEDIA_PICK", "Image URI: " + selectedUri);
            } else if (requestCode == REQUEST_VIDEO_PICK) {
                // ✅ Handle video
                Log.d("MEDIA_PICK", "Video URI: " + selectedUri);
            } else if (requestCode == REQUEST_AUDIO_PICK) {
                // ✅ Handle audio
                Log.d("MEDIA_PICK", "Audio URI: " + selectedUri);
            }
        }
    }
    
    private void loadUserProfile() {
        if (user != null) {
            edtDisplayName.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(imgProfile);
            }
        }
    }

    private void saveUserProfile() {
        String displayName = edtDisplayName.getText().toString().trim();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            // Upload photo first
            StorageReference photoRef = storageRef.child("profile_photos/" + user.getUid() + ".jpg");
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> photoRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                updateUserProfile(displayName, uri);
                            }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload photo", Toast.LENGTH_SHORT).show());
        } else {
            // No new photo selected
            updateUserProfile(displayName, user.getPhotoUrl());
        }
    }

    private void updateUserProfile(String displayName, Uri photoUri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(photoUri)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private boolean checkAndRequestPermissions() {
        String[] permissions = getRequiredPermissions();
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    listPermissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        } else {
            // All permissions granted → continue
            showImageSourceDialog();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            Map<String, Integer> perms = new HashMap<>();
            for (int i = 0; i < permissions.length; i++) {
                perms.put(permissions[i], grantResults[i]);
            }

            boolean allGranted = true;
            for (String permission : getRequiredPermissions()) {
                if (perms.getOrDefault(permission, PackageManager.PERMISSION_DENIED) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // ✅ Permissions granted
                showImageSourceDialog();
            } else {
                // ❌ At least one denied
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("You must allow permissions from Settings to continue.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs Camera and Storage access. Please enable them in App Settings.");
        builder.setPositiveButton("Go to Settings", (dialog, which) -> {
            dialog.dismiss();
            openAppSettings();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    @androidx.annotation.NonNull
    private static Map<String, Integer> getStringIntegerMap(@NonNull String[] permissions, @NonNull int[] grantResults) {
        Map<String, Integer> perms = new HashMap<>();
        perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.put(Manifest.permission.READ_MEDIA_IMAGES, PackageManager.PERMISSION_GRANTED);
        } else {
            perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
        }

        for (int i = 0; i < permissions.length; i++) {
            perms.put(permissions[i], grantResults[i]);
        }
        return perms;
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openFileChooser();
                    }
                })
                .show();
    }
    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
            };
        } else {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }

    private void requestImagePermissions() {
        String[] permissions = getRequiredPermissions();
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            boolean shouldShowRationale = false;

            for (String perm : listPermissionsNeeded) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                    shouldShowRationale = true;
                    break;
                }
            }

            if (shouldShowRationale) {
                // Show rationale dialog
                new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("This app needs access to your camera and photos to upload an image.")
                        .setPositiveButton("Grant", (dialog, which) -> ActivityCompat.requestPermissions(
                                this,
                                listPermissionsNeeded.toArray(new String[0]),
                                PERMISSION_REQUEST_CODE
                        ))
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // Directly request (first time OR "Don't ask again" selected)
                ActivityCompat.requestPermissions(
                        this,
                        listPermissionsNeeded.toArray(new String[0]),
                        PERMISSION_REQUEST_CODE
                );
            }
        } else {
            // ✅ All permissions already granted
            showImageSourceDialog();
        }
    }
    private String[] getRequiredMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (scoped permissions for different media types)
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };
        } else {
            // Older versions (single storage permission covers all media)
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }
    private void showMediaSourceDialog() {
        String[] options = {"Image", "Video", "Audio"};

        new AlertDialog.Builder(this)
                .setTitle("Select Media Type")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Image
                            pickImage();
                            break;
                        case 1: // Video
                            pickVideo();
                            break;
                        case 2: // Audio
                            pickAudio();
                            break;
                    }
                })
                .show();
    }
    // Pick an image
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // Pick a video
    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }

    // Pick audio
    private void pickAudio() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_AUDIO_PICK);
    }

}
