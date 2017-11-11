package chatme.apps.madnan.chatme;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    TextInputLayout userName, email, password, retypePassword;
    ImageView user_img;
    Button signUp_btn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public static final int IMAGE_GALLARY_REQUEST = 2;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 30;

    MaterialDialog.Builder progressDialog;
    MaterialDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new MaterialDialog.Builder(this);
        dialog = progressDialog.build();

        userName = (TextInputLayout)findViewById(R.id.signUp_name);
        email = (TextInputLayout)findViewById(R.id.signUp_email);
        password = (TextInputLayout)findViewById(R.id.signUp_password);
        retypePassword = (TextInputLayout)findViewById(R.id.signUp_retypePass);


        signUp_btn = (Button)findViewById(R.id.signUp_button);

        Animation an = AnimationUtils.loadAnimation(getBaseContext(),R.anim.appear);
        signUp_btn.startAnimation(an);

        // choose an image
        user_img = (ImageView)findViewById(R.id.signUp_image);
        user_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(SignUp.this)
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
        });

        // signUp button
        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameText = userName.getEditText().getText().toString();
                String emailText = email.getEditText().getText().toString();
                String passwordText = password.getEditText().getText().toString();
                String retpedPass = retypePassword.getEditText().getText().toString();



                if (nameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || retpedPass.isEmpty()) {

                    Toast.makeText(getBaseContext(), "Plsese Fill All Fields!", Toast.LENGTH_LONG).show();

                }else {
                    if (retpedPass.equals(passwordText)) {
                        progressDialog.title("Registering User");
                        progressDialog.content("Please wait...");
                        progressDialog.canceledOnTouchOutside(false);
                        progressDialog.progress(true, 0);
                        progressDialog.widgetColorRes(R.color.colorPrimary);
                        progressDialog.show();

                        registerUser(nameText,emailText, passwordText);
                        //code
                    } else if (retpedPass != passwordText) {
                        Toast.makeText(getBaseContext(), "Password doesn't match, check it!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    // register new user using firebase
    private void registerUser(final String username, final String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String userId = currentUser.getUid();

                            // add cild
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("username", username);
                            userMap.put("email", email);
                            userMap.put("status", "Hi I'm the developer of that app!");
                            userMap.put("image", "default");
                            userMap.put("mobile", "01014032996");
                            userMap.put("address", "Egypt");
                            userMap.put("thumb_image", "default");
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        dialog.dismiss();
                                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }

                                }
                            });

                        }else {
                            dialog.hide();
                            Log.e("ERROR", String.valueOf(task.getException()));
                            Toast.makeText(getBaseContext(), "Can't register, something is wrong,please try again!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
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
                user_img.setImageBitmap(imageBitmap);

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
                    user_img.setImageBitmap(imageFromGallary);

                }catch(IOException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
