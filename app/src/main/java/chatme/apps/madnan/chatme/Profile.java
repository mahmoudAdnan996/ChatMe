package chatme.apps.madnan.chatme;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.IOException;
import java.io.InputStream;

public class Profile extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;


    public static final int IMAGE_GALLARY_REQUEST = 2;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 30;

    ImageView userProfileIV;
    TextView usernameTV, statusTV, phoneTV, emailTV, addressTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();


        userProfileIV = (ImageView)findViewById(R.id.profile_userIV);
        usernameTV = (TextView)findViewById(R.id.profile_usernameTV);
        statusTV = (TextView)findViewById(R.id.profile_statusTV);
        phoneTV = (TextView)findViewById(R.id.profile_phoneTV);
        emailTV = (TextView)findViewById(R.id.profile_emailTV);
        addressTV = (TextView)findViewById(R.id.profile_AddressTV);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username = dataSnapshot.child("username").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String mobile = dataSnapshot.child("mobile").getValue().toString();
                String email = dataSnapshot.child("email").getValue().toString();
                String address = dataSnapshot.child("address").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();

                usernameTV.setText(username);
                statusTV.setText(status);
                emailTV.setText(email);
                phoneTV.setText(mobile);
                addressTV.setText(address);
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
        Intent takeImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takeImage.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takeImage, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    // pick image from gallery
    private void chooseImageGallary(){
        Intent imageGallaryIntent = new Intent(Intent.ACTION_PICK);
        imageGallaryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(imageGallaryIntent,"SELECT IMAGE"),IMAGE_GALLARY_REQUEST);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap)extras.get("data");
                userProfileIV.setImageBitmap(imageBitmap);

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
        if(requestCode == IMAGE_GALLARY_REQUEST){
            if(resultCode == RESULT_OK){
                Uri uri = data.getData();
                InputStream inputStream;
                try{
                    inputStream = getContentResolver().openInputStream(uri);
                    Bitmap imageFromGallary = BitmapFactory.decodeStream(inputStream);
                    userProfileIV.setImageBitmap(imageFromGallary);

                }catch(IOException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_SHORT).show();
                }
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
                        String editedEmail = input.toString();
                        emailTV.setText(editedEmail);
                        mUserDatabase.child("email").setValue(editedEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
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
}
