package chatme.apps.madnan.chatme;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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

        String email = emailET.getEditText().getText().toString();
        FirebaseAuth.getInstance().sendPasswordResetEmail(email);
    }
}
