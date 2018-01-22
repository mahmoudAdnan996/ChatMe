package chatme.apps.madnan.chatme.ui;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Map;

import chatme.apps.madnan.chatme.R;

import static chatme.apps.madnan.chatme.utils.Constants.FRIENDS_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.FRIEND_REQUEST_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.IMAGE;
import static chatme.apps.madnan.chatme.utils.Constants.STATUS;
import static chatme.apps.madnan.chatme.utils.Constants.USERS_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.USER_ID;
import static chatme.apps.madnan.chatme.utils.Constants.USER_NAME;

public class UserProfile extends AppCompatActivity {

    ImageView userProfileImg;
    TextView username, status, friendsCount;
    Button sendRequest, declinRequest;

    DatabaseReference mDatabaseReference;
    DatabaseReference mFriendReqDatabase;
    DatabaseReference mFriendDatabase;
    DatabaseReference mRootRef;

    DatabaseReference newNotificationRef;

    ProgressDialog progressDialog;

    FirebaseUser mCurrentUser;

    String newNotificationId;

    private String currentState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final String userId = getIntent().getStringExtra(USER_ID);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(USERS_TABLE).child(userId);
        mDatabaseReference.keepSynced(true);

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child(FRIEND_REQUEST_TABLE);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child(FRIENDS_TABLE);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loading_user_data));
        progressDialog.setMessage(getString(R.string.please_wait));
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

                String name = dataSnapshot.child(USER_NAME).getValue().toString();
                String userStatus = dataSnapshot.child(STATUS).getValue().toString();
                final String image = dataSnapshot.child(IMAGE).getValue().toString();
                mFriendDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String count = String.valueOf(dataSnapshot.getChildrenCount());
                        friendsCount.setText(count + " " + getString(R.string.friend));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

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
                                sendRequest.setText(R.string.accept_request);

                                declinRequest.setVisibility(View.VISIBLE);
                                declinRequest.setEnabled(true);

                            }else if (req_type.equals("sent")){
                                currentState = "req_sent";
                                sendRequest.setText(R.string.cancel_request);

                                declinRequest.setVisibility(View.INVISIBLE);
                                declinRequest.setEnabled(false);

                            }
                        }else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)){
                                        currentState = "friends";
                                        sendRequest.setText(R.string.unfriend);

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

                    newNotificationRef = mRootRef.child("notifications").child(userId).push();
                    newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId + "/request_type", "sent");
                    requestMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + userId + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null){
                                Toast.makeText(UserProfile.this, "There is some errors", Toast.LENGTH_LONG).show();
                            }
                            sendRequest.setEnabled(true);
                            currentState = "req_sent";
                            sendRequest.setText(R.string.cancel_request);


                        }
                    });
                }
                //==================== Cancel Friend Request =================
                if (currentState.equals("req_sent")){

                    Map cancelMap = new HashMap();
                    cancelMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId, null);
                    cancelMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid(), null);
                    cancelMap.put("notifications/" + userId + "/" + newNotificationId, null);

                    mRootRef.updateChildren(cancelMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){
                                currentState = "not_friends";
                                sendRequest.setText(R.string.send_request);

                                declinRequest.setVisibility(View.INVISIBLE);
                                declinRequest.setEnabled(false);
                            }
                            else {
                                String error = databaseError.getMessage();
                                Toast.makeText(UserProfile.this, error, Toast.LENGTH_LONG).show();
                            }
                            sendRequest.setEnabled(true);
                        }
                    });
                }

                //===================== Req received =================
                if (currentState.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId + "/date", currentDate);
                    friendsMap.put("Friends/" + userId + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId, null);
                    friendsMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){
                                sendRequest.setEnabled(true);
                                currentState = "friends";
                                sendRequest.setText(R.string.unfriend);

                                declinRequest.setVisibility(View.INVISIBLE);
                                declinRequest.setEnabled(false);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(UserProfile.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });


                }
                // ======================== unfriend =========================
                if (currentState.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + userId, null);
                    unfriendMap.put("Friends/" + userId + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){

                                currentState = "not_friends";
                                sendRequest.setText(R.string.send_request);
                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(UserProfile.this, error, Toast.LENGTH_LONG).show();
                            }
                            sendRequest.setEnabled(true);
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

                    Map declineMap = new HashMap();
                    declineMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + userId, null);
                    declineMap.put("Friend_req/" + userId + "/" + mCurrentUser.getUid(), null);
                    declineMap.put("notifications/" + mCurrentUser.getUid() + "/" + newNotificationId, null);


                    mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){
                                currentState = "not_friends";
                                sendRequest.setText(R.string.send_request);

                                declinRequest.setVisibility(View.INVISIBLE);
                                declinRequest.setEnabled(false);
                            }
                            else {
                                String error = databaseError.getMessage();
                                Toast.makeText(UserProfile.this, error, Toast.LENGTH_LONG).show();
                            }
                            sendRequest.setEnabled(true);
                        }
                    });

                }
            }
        });

    }
}
