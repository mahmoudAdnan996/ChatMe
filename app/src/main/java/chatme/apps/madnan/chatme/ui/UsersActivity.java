package chatme.apps.madnan.chatme.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.model.Users;
import de.hdodenhof.circleimageview.CircleImageView;

import static chatme.apps.madnan.chatme.utils.Constants.USERS_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.USER_ID;

public class UsersActivity extends AppCompatActivity {

    RecyclerView userRec;

    DatabaseReference mDatabaseReference;
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(USERS_TABLE);
        mDatabaseReference.keepSynced(true);

        userRec = (RecyclerView)findViewById(R.id.users_recView);
        userRec.setLayoutManager(new LinearLayoutManager(this));
        userRec.setHasFixedSize(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class, R.layout.users_rec_item, UsersViewHolder.class,mDatabaseReference
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setDisplayName(model.getUsername());
                viewHolder.setUserStatus(model.getStatus());
                viewHolder.setThumbImage(model.getThumb_image(), getApplicationContext());

                final String user_id = getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(UsersActivity.this, UserProfile.class);
                        intent.putExtra(USER_ID, user_id);
                        startActivity(intent);
                    }
                });
            }
        };

        userRec.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDisplayName(String name){
            TextView username = (TextView) mView.findViewById(R.id.users_nameTV);
            username.setText(name);
        }
        public void setUserStatus(String status){
            TextView statusTv = (TextView) mView.findViewById(R.id.users_statusTV);
            statusTv.setText(status);
        }
        public void setThumbImage(final String thumb_image, final Context context){
            final CircleImageView userImage = (CircleImageView)mView.findViewById(R.id.users_profileIMG);

            Picasso.with(context).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile).into(userImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(thumb_image).placeholder(R.drawable.profile).into(userImage);
                }
            });
        }
    }
}
