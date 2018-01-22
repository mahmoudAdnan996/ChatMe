package chatme.apps.madnan.chatme.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import chatme.apps.madnan.chatme.R;

import static chatme.apps.madnan.chatme.utils.Constants.ADDRESS;
import static chatme.apps.madnan.chatme.utils.Constants.DEVICE_TOKEN;
import static chatme.apps.madnan.chatme.utils.Constants.EMAIL;
import static chatme.apps.madnan.chatme.utils.Constants.IMAGE;
import static chatme.apps.madnan.chatme.utils.Constants.MOBILE;
import static chatme.apps.madnan.chatme.utils.Constants.STATUS;
import static chatme.apps.madnan.chatme.utils.Constants.THUMP_IMAGE;
import static chatme.apps.madnan.chatme.utils.Constants.USERS_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.USER_NAME;

public class SignUp extends AppCompatActivity {

    TextInputLayout userName, email, password, retypePassword;
    Button signUp_btn;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    Toolbar mToolbar;

    MaterialDialog.Builder progressDialog;
    MaterialDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.Signup));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

                    Toast.makeText(getBaseContext(), R.string.fill_all_fields, Toast.LENGTH_LONG).show();
                }
                else {
                    if (retpedPass.equals(passwordText)) {
                        progressDialog.title(R.string.signning_up);
                        progressDialog.content(R.string.please_wait);
                        progressDialog.canceledOnTouchOutside(false);
                        progressDialog.progress(true, 0);
                        progressDialog.widgetColorRes(R.color.colorPrimary);
                        progressDialog.show();

                        registerUser(nameText,emailText, passwordText);
                        //code
                    }
                    else if (retpedPass != passwordText) {
                        Toast.makeText(getBaseContext(), R.string.password_doesnt_match, Toast.LENGTH_LONG).show();
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
                            final String userId = currentUser.getUid();

                            final String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            // add cild
                            mDatabase = FirebaseDatabase.getInstance().getReference().child(USERS_TABLE).child(userId);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put(USER_NAME, username);
                            userMap.put(EMAIL, email);
                            userMap.put(STATUS, "Status");
                            userMap.put(IMAGE, "default");
                            userMap.put(MOBILE, "000-000-000");
                            userMap.put(ADDRESS, "Country-City");
                            userMap.put(THUMP_IMAGE, "default");
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        dialog.dismiss();

                                        mDatabase.child(userId).child(DEVICE_TOKEN).setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = new Intent(SignUp.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                    }
                                }
                            });

                        }else {
                            dialog.hide();
                            Log.e("ERROR", String.valueOf(task.getException()));
                            Toast.makeText(getBaseContext(), R.string.cant_signup, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
