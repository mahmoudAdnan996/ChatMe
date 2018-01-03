package chatme.apps.madnan.chatme.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import chatme.apps.madnan.chatme.R;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Profile extends AppCompatActivity {

    public static final int IMAGE_GALLARY_REQUEST = 2;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 30;

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;

    CircleImageView userProfileIV;
    TextView usernameTV, statusTV, phoneTV, emailTV, addressTV;
    ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(chatme.apps.madnan.chatme.R.layout.profile_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();


        userProfileIV = (CircleImageView)findViewById(R.id.profile_userIV);
        usernameTV = (TextView)findViewById(R.id.profile_usernameTV);
        statusTV = (TextView)findViewById(R.id.profile_statusTV);
        phoneTV = (TextView)findViewById(R.id.profile_phoneTV);
        emailTV = (TextView)findViewById(R.id.profile_emailTV);
        addressTV = (TextView)findViewById(R.id.profile_AddressTV);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        mUserDatabase.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username = dataSnapshot.child("username").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String mobile = dataSnapshot.child("mobile").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String address = dataSnapshot.child("address").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                usernameTV.setText(username);
                statusTV.setText(status);
                emailTV.setText(email);
                phoneTV.setText(mobile);
                addressTV.setText(address);
                if (!image.equals("default")){
//                    Picasso.with(Profile.this).load(image).placeholder(R.drawable.profile).into(userProfileIV);
                    Picasso.with(Profile.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profile).into(userProfileIV, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(Profile.this).load(image).placeholder(R.drawable.profile).into(userProfileIV);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void choosePhoto(View view) {

        new MaterialDialog.Builder(Profile.this)
                .title("choose image")
                .positiveText("Gallary")
                .positiveColorRes(R.color.colorPrimary)
                .negativeText("Camera")
                .negativeColorRes(R.color.colorPrimary)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        chooseImageGallary();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        takeImageIntent();
                    }
                })
                .show();

    }
    // capture an image
    private void takeImageIntent(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
        }else {
            Intent takeImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takeImage.resolveActivity(getPackageManager()) != null){
                startActivityForResult(takeImage, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }

        }
    }

    // pick image from gallery
    private void chooseImageGallary(){
        Intent imageGallaryIntent = new Intent(Intent.ACTION_PICK);
        imageGallaryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(imageGallaryIntent,"SELECT IMAGE"),IMAGE_GALLARY_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takeImageIntent();
        }else {
            Toast.makeText(this, "App need Camera Permission", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String currentUserId = mCurrentUser.getUid();
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap)extras.get("data");
                Uri uri = getImageUri(getApplicationContext(), imageBitmap);
                CropImage.activity(uri)
                        .setAspectRatio(1, 1)
                        .start(this);
//                userProfileIV.setImageBitmap(imageBitmap);

                mProgressDialog = new ProgressDialog(Profile.this, R.style.AlertDialogTheme);
                mProgressDialog.setTitle("Uploading image");
                mProgressDialog.setMessage("please wait");
                mProgressDialog.setProgressStyle(R.style.AlertDialogTheme);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                userProfileIV.setImageURI(uri);

                StorageReference filePath = mStorageRef.child("profile_images").child(currentUserId + ".jpg");
                filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            String downloadUrl = task.getResult().getDownloadUrl().toString();
                            mUserDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mProgressDialog.dismiss();
                                        Toast.makeText(Profile.this, "Uploading Success", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else {
                            Log.e("ERROR IS :" , String.valueOf(task.getException()));
                            Toast.makeText(Profile.this, "Error while uploading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "you cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        if(requestCode == IMAGE_GALLARY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                CropImage.activity(uri)
                        .setAspectRatio(1, 1)
                        .start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    mProgressDialog = new ProgressDialog(Profile.this, R.style.AlertDialogTheme);
                    mProgressDialog.setTitle("Uploading image");
                    mProgressDialog.setMessage("please wait");
                    mProgressDialog.setProgressStyle(R.style.AlertDialogTheme);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();

                    Uri resultUri = result.getUri();

                    File thumb_filePath = new File(resultUri.getPath());
                    Bitmap thumb_bitmap = null;
                    try {
                        thumb_bitmap = new Compressor(this)
                                    .setMaxHeight(200)
                                    .setMaxWidth(200)
                                    .setQuality(75)
                                    .compressToBitmap(thumb_filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();


                    userProfileIV.setImageURI(resultUri);

                    StorageReference filePath = mStorageRef.child("profile_images").child(currentUserId + ".jpg");
                    final StorageReference thumb_filepath = mStorageRef.child("profile_images").child("thumbs").child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    String thumb_downloadUrl = task.getResult().getDownloadUrl().toString();

                                    if (task.isSuccessful()){
                                        Map updatedInfo = new HashMap();
                                        updatedInfo.put("image", downloadUrl);
                                        updatedInfo.put("thumb_image", thumb_downloadUrl);

                                        mUserDatabase.updateChildren(updatedInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(Profile.this, "Uploading Success", Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        });
                                    }else {
                                        Log.e("ERROR IS :" , String.valueOf(task.getException()));
                                        mProgressDialog.dismiss();
                                        Toast.makeText(Profile.this, "Error while uploading image", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }



    }
    public void EditStatus(View view) {

        new MaterialDialog.Builder(Profile.this)
                .title("Edit Your Status")
                .positiveText("Edit")
                .positiveColorRes(R.color.colorPrimary)
                .negativeText("Cancel")
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Status", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String editedStatus = input.toString();
                        statusTV.setText(editedStatus);
                        mUserDatabase.child("status").setValue(editedStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(Profile.this, "Status updated successfully", Toast.LENGTH_LONG).show();
                                }else {
                                    Log.e("ERROR Is: ", String.valueOf(task.getException()));
                                    Toast.makeText(Profile.this, "Somthing is wrong, try again!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        return;
                    }
                })
                .show();
    }

    public void EditPhone(View view) {
        new MaterialDialog.Builder(Profile.this)
                .title("Edit Your Phone")
                .positiveText("Edit")
                .positiveColorRes(R.color.colorPrimary)
                .negativeText("Cancel")
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Phone number", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String editednumber = input.toString();
                        phoneTV.setText(editednumber);
                        mUserDatabase.child("mobile").setValue(editednumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(Profile.this, "Mobile number updated successfully", Toast.LENGTH_LONG).show();
                                }else {
                                    Log.e("ERROR Is: ", String.valueOf(task.getException()));
                                    Toast.makeText(Profile.this, "Somthing is wrong, try again!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        return;
                    }
                })
                .show();
    }

    public void EditEmail(View view) {

        new MaterialDialog.Builder(Profile.this)
                .title("Edit Your Email")
                .positiveText("Edit")
                .positiveColorRes(R.color.colorPrimary)
                .negativeText("Cancel")
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Email", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String editedEmail = input.toString().trim();
                        emailTV.setText(editedEmail);
                        mCurrentUser.updateEmail(editedEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(Profile.this, "Email updated successfully", Toast.LENGTH_LONG).show();
                                }else {
                                    Log.e("ERROR Is: ", String.valueOf(task.getException()));
                                    Toast.makeText(Profile.this, "Somthing is wrong, try again!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        mUserDatabase.child("email").setValue(editedEmail);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        return;
                    }
                })
                .show();

        /*
        mCurrentUser.updateEmail(editedEmail);
         */
    }

    public void EditAddress(View view) {

        new MaterialDialog.Builder(Profile.this)
                .title("Edit Your Address")
                .positiveText("Edit")
                .positiveColorRes(R.color.colorPrimary)
                .negativeText("Cancel")
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Address", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        String editedAddress = input.toString();
                        addressTV.setText(editedAddress);
                        mUserDatabase.child("address").setValue(editedAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(Profile.this, "Address updated successfully", Toast.LENGTH_LONG).show();
                                }else {
                                    Log.e("ERROR Is: ", String.valueOf(task.getException()));
                                    Toast.makeText(Profile.this, "Somthing is wrong, try again!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        return;
                    }
                })
                .show();
    }
    // convert bitmap to uri
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
