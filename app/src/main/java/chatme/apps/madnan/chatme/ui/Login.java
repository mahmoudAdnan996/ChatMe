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

import butterknife.BindView;
import butterknife.ButterKnife;
import chatme.apps.madnan.chatme.R;

import static chatme.apps.madnan.chatme.utils.Constants.DEVICE_TOKEN;
import static chatme.apps.madnan.chatme.utils.Constants.USERS_TABLE;

public class Login extends AppCompatActivity {

    @BindView(R.id.signIn_username)
    TextInputLayout userName;
    @BindView(R.id.signIn_password)
    TextInputLayout password;
    @BindView(R.id.signIn_button)
    Button signIn_btn;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    String mUsername, mPassword;


    MaterialDialog.Builder progressDialog;
    MaterialDialog dialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle(getString(R.string.Signin));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(USERS_TABLE);

        progressDialog = new MaterialDialog.Builder(this);

        Animation an = AnimationUtils.loadAnimation(getBaseContext(),R.anim.appear);
        signIn_btn.startAnimation(an);

        signIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUsername = userName.getEditText().getText().toString().trim();
                mPassword = password.getEditText().getText().toString().trim();

                if (mUsername.isEmpty() || mPassword.isEmpty()){
                    Toast.makeText(getBaseContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                }
                else {
                    progressDialog.title(R.string.signning_in);
                    progressDialog.content(R.string.please_wait);
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

                            mDatabaseReference.child(currentUserId).child(DEVICE_TOKEN).setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                            Toast.makeText(Login.this, R.string.cant_sign_in, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void forgetPass(View view) {
        Intent intent = new Intent(this, RestorePassActivity.class);
        startActivity(intent);
    }
}
