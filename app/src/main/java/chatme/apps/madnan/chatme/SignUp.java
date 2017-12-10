package chatme.apps.madnan.chatme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    TextInputLayout userName, email, password, retypePassword;
    Button signUp_btn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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

        // signUp button
        signUp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameText = userName.getEditText().getText().toString().trim();
                String emailText = email.getEditText().getText().toString().trim();
                String passwordText = password.getEditText().getText().toString().trim();
                String retpedPass = retypePassword.getEditText().getText().toString().trim();

                if (nameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty() || retpedPass.isEmpty()) {

                    Toast.makeText(getBaseContext(), "Plsese Fill All Fields!", Toast.LENGTH_LONG).show();
                }
                else {
                    if (retpedPass.equals(passwordText)) {
                        progressDialog.title("Registering User");
                        progressDialog.content("Please wait...");
                        progressDialog.canceledOnTouchOutside(false);
                        progressDialog.progress(true, 0);
                        progressDialog.widgetColorRes(R.color.colorPrimary);
                        progressDialog.show();

                        registerUser(nameText,emailText, passwordText);
                        //code
                    }
                    else if (retpedPass != passwordText) {
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
                            userMap.put("status", "Status");
                            userMap.put("image", "default");
                            userMap.put("mobile", "000-000-000");
                            userMap.put("address", "Country-City");
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

}
