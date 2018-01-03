package chatme.apps.madnan.chatme.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.ui.Login;
import chatme.apps.madnan.chatme.ui.SignUp;

public class Welcom extends AppCompatActivity {

    TextView signIn, signUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcom_activity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        signIn = (TextView)findViewById(R.id.Welcome_signin);
        signUp = (TextView)findViewById(R.id.Welcome_signup);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), Login.class);
                startActivity(intent);
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SignUp.class);
                startActivity(intent);
            }
        });
    }
}
