package chatme.apps.madnan.chatme;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    TextInputLayout userName, password;
    Button signIn_btn;
    String mUsername, mPassword;

    MaterialDialog.Builder progressDialog;
    MaterialDialog dialog;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new MaterialDialog.Builder(this);


        userName = (TextInputLayout)findViewById(R.id.signIn_username);
        password = (TextInputLayout)findViewById(R.id.signIn_password);

        Animation an = AnimationUtils.loadAnimation(getBaseContext(),R.anim.appear);
        signIn_btn = (Button)findViewById(R.id.signIn_button);
        signIn_btn.startAnimation(an);



        signIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUsername = userName.getEditText().getText().toString();
                mPassword = password.getEditText().getText().toString();

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
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            // add new task and remove previus tasks
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else {
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
