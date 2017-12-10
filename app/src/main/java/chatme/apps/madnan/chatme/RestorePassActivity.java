package chatme.apps.madnan.chatme;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RestorePassActivity extends AppCompatActivity {

    TextInputLayout emailET;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_pass);

        emailET = (TextInputLayout)findViewById(R.id.restorePass_email);
    }

    public void restoreBtn(View view) {

        String email = emailET.getEditText().getText().toString().trim();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(RestorePassActivity.this,"Message Sent, Check your email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RestorePassActivity.this, Login.class);
                    startActivity(intent);
                }
            }
        });
    }
}
