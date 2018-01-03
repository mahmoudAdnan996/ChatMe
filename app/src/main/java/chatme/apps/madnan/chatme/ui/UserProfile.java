package chatme.apps.madnan.chatme.ui;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import chatme.apps.madnan.chatme.R;

public class UserProfile extends AppCompatActivity {

    ImageView userProfileImg;
    TextView username, status, friendsCount;
    Button sendRequest, declinRequest;

    DatabaseReference mDatabaseReference;
    DatabaseReference mFriendReqDatabase;
    DatabaseReference mFriendDatabase;
    DatabaseReference mNotificationDatabase;

    ProgressDialog progressDialog;

    FirebaseUser mCurrentUser;

    private String currentState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        final String userId = getIntent().getStringExtra("userId");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mDatabaseReference.keepSynced(true);

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        userProfileImg = (ImageView)findViewById(R.id.userProfileIMG);
        username = (TextView)findViewById(R.id.UserProfileName);
        status = (TextView)findViewById(R.id.UserProfileStatus);
        friendsCount = (TextView)findViewById(R.id.UserProfileFriendsCount);
        sendRequest = (Button)findViewById(R.id.UserProfileSendRequest);
        declinRequest = (Button)findViewById(R.id.DeclineRequest);

        currentState = "not_friends";

        declinRequest.setVisibility(View.INVISIBLE);
        declinRequest.setEnabled(false);



        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("username").getValue().toString();
                String userStatus = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                username.setText(name);
                status.setText(userStatus);

                Picasso.with(UserProfile.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.profile_avatar).into(userProfileImg, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(UserProfile.this).load(image).placeholder(R.drawable.profile_avatar).into(userProfileImg);
                    }
                });


                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)){
                            String req_type = dataSnapshot.child(userId).child("request_type").getValue().toString();
                            if (req_type.equals("received")){
                                currentState = "req_received";
                                sendRequest.setText("Accept Friend Request");

                                declinRequest.setVisibility(View.VISIBLE);
                                declinRequest.setEnabled(true);

                            }else if (req_type.equals("sent")){
                                currentState = "req_sent";
                                sendRequest.setText("Cancel Friend Request");

                                declinRequest.setVisibility(View.INVISIBLE);
                                declinRequest.setEnabled(false);

                            }
                        }else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)){
                                        currentState = "friends";
                                        sendRequest.setText("Unfriend");

                                        declinRequest.setVisibility(View.INVISIBLE);
                                        declinRequest.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest.setEnabled(false);
                //================== Not Friend State ====================
                if (currentState.equals("not_friends")){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendReqDatabase.child(userId).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", mCurrentUser.getUid());
                                        notificationData.put("type", "request");
                                        mNotificationDatabase.child(userId).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
89
                                                currentState = "req_sent";
                                                sendRequest.setText("Cancel Friend Request");

                                                declinRequest.setVisibility(View.INVISIBLE);
                                                declinRequest.setEnabled(false);
                                            }
                                        });
                                    }
                                });
                            }else {
                                Toast.makeText(UserProfile.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();
                            }
                            sendRequest.setEnabled(true);
                        }
                    });
                }
                //==================== Cancel Friend Request =================
                if (currentState.equals("req_sent")){
                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    sendRequest.setEnabled(true);
                                    currentState = "not_friends";
                                    sendRequest.setText("Send friend request");

                                    declinRequest.setVisibility(View.INVISIBLE);
                                    declinRequest.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                //===================== Req recieved =================
                if (currentState.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(userId).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendDatabase.child(userId).child(mCurrentUser.getUid()).setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendReqDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    sendRequest.setEnabled(true);
                                                                    currentState = "friends";
                                                                    sendRequest.setText("Unfriend");

                                                                    declinRequest.setVisibility(View.INVISIBLE);
                                                                    declinRequest.setEnabled(false);
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                }
                // ======================== unfriend =========================
                if (currentState.equals("friends")){
                    mFriendDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    sendRequest.setEnabled(true);
                                    currentState = "not_friends";
                                    sendRequest.setText("Send friend request");
                                }
                            });
                        }
                    });
                }

            }
        });

        // ================= decline request =============================
        declinRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentState.equals("req_received")){

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    sendRequest.setEnabled(true);
                                    currentState = "not_friends";
                                    sendRequest.setText("Send friend request");

                                    declinRequest.setVisibility(View.INVISIBLE);
                                    declinRequest.setEnabled(false);
                                }
                            });
                        }
                    });
                }
            }
        });

    }
}
