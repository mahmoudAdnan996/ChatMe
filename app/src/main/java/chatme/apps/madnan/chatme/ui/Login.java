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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import chatme.apps.madnan.chatme.R;

public class Login extends AppCompatActivity {

    TextInputLayout userName, password;
    Button signIn_btn;
    String mUsername, mPassword;

    Toolbar mToolbar;

    MaterialDialog.Builder progressDialog;
    MaterialDialog dialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new MaterialDialog.Builder(this);

        userName = (TextInputLayout)findViewById(R.id.signIn_username);
        password = (TextInputLayout)findViewById(R.id.signIn_password);
        signIn_btn = (Button)findViewById(R.id.signIn_button);

        Animation an = AnimationUtils.loadAnimation(getBaseContext(),R.anim.appear);
        signIn_btn.startAnimation(an);

        signIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUsername = userName.getEditText().getText().toString().trim();
                mPassword = password.getEditText().getText().toString().trim();

                if (mUsername.isEmpty() || mPassword.isEmpty()){
                    Toast.makeText(getBaseContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.title("Signing In");
                    progressDialog.content("Please wait...");
                    progressDialog.canceledOnTouchOutside(false);
                    progressDialog.progress(true, 0);
                    progressDialog.widgetColorRes(R.color.colorPrimary);
                    progressDialog.show();

                    loginUser(mUsername, mPassword);
                }
            }
        });
    }

    private void loginUser(String username, String password){

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        dialog = progressDialog.build();
                        if (task.isSuccessful()){

                            dialog.dismiss();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            mDatabaseReference.child(currentUserId).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    // add new task and remove previous tasks
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                        else {
                            dialog.dismiss();
                            Log.e("ERROR", String.valueOf(task.getException()));
                            Toast.makeText(Login.this, "Can't sign in, something is wrong,please try again!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void forgetPass(View view) {
        Intent intent = new Intent(this, RestorePassActivity.class);
        startActivity(intent);
    }
}
