package chatme.apps.madnan.chatme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import chatme.apps.madnan.chatme.R;

public class UsersActivity extends AppCompatActivity {

    RecyclerView userRec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        userRec = (RecyclerView)findViewById(R.id.users_recView);
        userRec.setLayoutManager(new LinearLayoutManager(this));
        userRec.setHasFixedSize(true);
    }
}
