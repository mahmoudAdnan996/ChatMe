package chatme.apps.madnan.chatme.ui.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.model.Convs;
import chatme.apps.madnan.chatme.ui.ChatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import static chatme.apps.madnan.chatme.utils.Constants.IMAGE;
import static chatme.apps.madnan.chatme.utils.Constants.USER_ID;
import static chatme.apps.madnan.chatme.utils.Constants.USER_NAME;


public class ChatsFragment extends Fragment {

    @BindView(R.id.chatsRV)
     RecyclerView chatsRV;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDataase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    public ChatsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        ButterKnife.bind(this, view);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
        mConvDatabase.keepSynced(true);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDataase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrentUserId);
        mMessageDataase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chatsRV.setLayoutManager(linearLayoutManager);
        chatsRV.setHasFixedSize(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");
        FirebaseRecyclerAdapter<Convs, ConvHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Convs, ConvHolder>(
                Convs.class,
                R.layout.users_rec_item,
                ConvHolder.class,
                conversationQuery
        ) {
            @Override
            protected void populateViewHolder(final ConvHolder viewHolder, final Convs model, int position) {

                final String userId = getRef(position).getKey();
                Query lastMessageQuery = mMessageDataase.child(userId).limitToLast(1);
                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        String type = dataSnapshot.child("type").getValue().toString();
                        boolean seen = (boolean) dataSnapshot.child("seen").getValue();
                        if (type.equals("text")){
                            viewHolder.setMessage(data, seen);
                        }else {
                            viewHolder.setMessage("Image", seen);
                        }

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
                mUserDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String user_name = dataSnapshot.child("username").getValue().toString();
                        final String user_thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }
                        viewHolder.setName(user_name);
                        viewHolder.setImage(user_thumb_image, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                intent.putExtra(USER_ID, userId);
                                intent.putExtra(USER_NAME, user_name);
                                intent.putExtra(IMAGE, user_thumb_image);
                                startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        chatsRV.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ConvHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.users_nameTV)
        TextView username;
        @BindView(R.id.users_statusTV)
        TextView userMessage;
        @BindView(R.id.users_profileIMG)
        CircleImageView userImage;
        @BindView(R.id.onlineIV)
        ImageView onlineImage;

        View mView;

        public ConvHolder(View itemView) {
            super(itemView);
            mView = itemView;

            ButterKnife.bind(this, mView);
        }

        public void setMessage(String message, boolean isSeen){
            userMessage.setText(message);

            if (!isSeen){
                userMessage.setTypeface(userMessage.getTypeface(), Typeface.BOLD);
                userMessage.setTextColor(Color.BLACK);
            }else {
                userMessage.setTypeface(userMessage.getTypeface(), Typeface.NORMAL);
            }
        }
        public void setName(String name){
            username.setText(name);
        }
        public void setImage(String thumbImage, Context context){
            Picasso.with(context).load(thumbImage).placeholder(R.drawable.profile).into(userImage);
        }
        public void setUserOnline(String onlineStatus){

            if (onlineStatus.equals("true")){
                onlineImage.setVisibility(View.VISIBLE);
            }else {
                onlineImage.setVisibility(View.INVISIBLE);
            }
        }
    }
}
