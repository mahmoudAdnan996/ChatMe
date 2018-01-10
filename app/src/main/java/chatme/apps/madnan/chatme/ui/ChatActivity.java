package chatme.apps.madnan.chatme.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.model.Messages;
import chatme.apps.madnan.chatme.ui.adapter.MessageAdapter;
import chatme.apps.madnan.chatme.utils.GetTimeAgo;
import de.hdodenhof.circleimageview.CircleImageView;

import static chatme.apps.madnan.chatme.utils.Constants.CHAT_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.IMAGE;
import static chatme.apps.madnan.chatme.utils.Constants.MESSAGES_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.ONLINE;
import static chatme.apps.madnan.chatme.utils.Constants.USERS_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.USER_ID;
import static chatme.apps.madnan.chatme.utils.Constants.USER_NAME;

public class ChatActivity extends AppCompatActivity {

    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 30;
    public static final int IMAGE_GALLERY_REQUEST = 2;

    Toolbar mToolbar;

    DatabaseReference mUserDatabase;
    DatabaseReference mRootRef;
    FirebaseAuth mAuth;

    TextView nameTV, lastSeenTV;
    CircleImageView chatUserImage;
    ImageButton sendImg, sendBtn;
    EditText messageET;

    RecyclerView messagesRV;

    Uri uri;
    private String chatUserId, username, imageUrl, mCurrentUserId;

    private List<Messages> messagesList;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        messagesList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messagesList, ChatActivity.this);

        mUserDatabase = FirebaseDatabase.getInstance().getReference();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        sendImg = (ImageButton)findViewById(R.id.sendImageBTN);
        sendBtn = (ImageButton)findViewById(R.id.sendBTN);
        messageET = (EditText)findViewById(R.id.sendMessageET);
        nameTV = (TextView)action_bar_view.findViewById(R.id.chatUserName);
        lastSeenTV = (TextView)action_bar_view.findViewById(R.id.chatLastSeen);
        chatUserImage = (CircleImageView)action_bar_view.findViewById(R.id.custom_bar_image);

        messagesRV = (RecyclerView)findViewById(R.id.messagesRV);
        messagesRV.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        messagesRV.setHasFixedSize(true);
        messagesRV.setAdapter(messageAdapter);

        chatUserId = getIntent().getStringExtra(USER_ID);
        username = getIntent().getStringExtra(USER_NAME);
        imageUrl = getIntent().getStringExtra(IMAGE);
        mCurrentUserId = mAuth.getCurrentUser().getUid();


        Picasso.with(ChatActivity.this).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.profile).into(chatUserImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(ChatActivity.this).load(imageUrl).placeholder(R.drawable.profile).into(chatUserImage);
            }
        });
        nameTV.setText(username);

        mUserDatabase.child(USERS_TABLE).child(chatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child(ONLINE).getValue().toString();
                if (online.equals("true")){
                    lastSeenTV.setText("online");
                }
                else {
                    GetTimeAgo get_time_ago = new GetTimeAgo();
                    try{
                        long lastTime = Long.valueOf(online).longValue();
                        String lastSeenTime = get_time_ago.getTimeAgo(lastTime, getApplicationContext());
                        lastSeenTV.setText(lastSeenTime);

                    }catch (NumberFormatException e){
                        lastSeenTV.setText("offline");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child(CHAT_TABLE).child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(chatUserId)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + chatUserId, chatAddMap);
                    chatUserMap.put("Chat/" + chatUserId + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){
                                Log.e("CHAT_LOG", databaseError.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        loadMessages();
    }

    private void loadMessages() {

        mRootRef.child(MESSAGES_TABLE).child(mCurrentUserId).child(chatUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void sendImage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this, R.style.AlertDialogTheme);
        builder.setMessage("Choose image")
                .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        takeImageIntent();
                    }
                })
                .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        chooseImageGallary();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void takeImageIntent(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},1);
        }else {
            Intent takeImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takeImage.resolveActivity(getPackageManager()) != null){
                startActivityForResult(takeImage, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }
        }
    }

    private void chooseImageGallary(){
        Intent imageGallaryIntent = new Intent(Intent.ACTION_PICK);
        imageGallaryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(imageGallaryIntent,"SELECT IMAGE"),IMAGE_GALLERY_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takeImageIntent();
        }else {
            Toast.makeText(this, "App need Camera Permission", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap)extras.get("data");
                uri= getImageUri(getApplicationContext(), imageBitmap);
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(ChatActivity.this, "Capture iamge cancelled", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                uri = data.getData();
                Log.e("Uri is :", String.valueOf(uri));
            }
        }
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void sendMessage() {
        String messageText = messageET.getText().toString();

        if (!TextUtils.isEmpty(messageText)){

            String currentUserRef = "messages/" + mCurrentUserId + "/" + chatUserId;
            String chatUserRef = "messages/" + chatUserId + "/" + mCurrentUserId;

            DatabaseReference userMessagePush = mRootRef.child("messages").child(mCurrentUserId)
                    .child(chatUserId).push();

            String push_id = userMessagePush.getKey();

            Map messagesMap = new HashMap();
            messagesMap.put("message", messageText);
            messagesMap.put("seen", false);
            messagesMap.put( "type", "text");
            messagesMap.put("time", ServerValue.TIMESTAMP);
            messagesMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + push_id, messagesMap);
            messageUserMap.put(chatUserRef + "/" + push_id, messagesMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null){
                        Log.e("CHAT_ERROR", databaseError.getMessage());
                    }
                    messageET.setText("");
                }
            });
        }
    }
}
