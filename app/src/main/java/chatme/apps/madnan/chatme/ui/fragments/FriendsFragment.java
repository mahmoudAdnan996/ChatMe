package chatme.apps.madnan.chatme.ui.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.model.Friends;
import chatme.apps.madnan.chatme.ui.ChatActivity;
import chatme.apps.madnan.chatme.ui.UserProfile;
import chatme.apps.madnan.chatme.ui.UsersActivity;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    RecyclerView friendsRv;
    DatabaseReference mFriendsDatabase;
    DatabaseReference mUserDatabase;
    FirebaseAuth mAuth;

    String currentUserId;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsRv = (RecyclerView)view.findViewById(R.id.friendsRV);
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        friendsRv.setHasFixedSize(true);
        friendsRv.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_rec_item,
                FriendsViewHolder.class,
                mFriendsDatabase)
        {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());

                final String userId = getRef(position).getKey();
                mUserDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String username = dataSnapshot.child("username").getValue().toString();
                        final String imageUri = dataSnapshot.child("thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);

                        }

                        viewHolder.setName(username);
                        viewHolder.setThumbImage(imageUri, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            final CharSequence[] items = {
                                    "Open Profile","Start Chat"};

                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                                dialog.setTitle("Select action");
                                dialog.setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (items[which].equals("Open Profile")){
                                            Intent intent = new Intent(getActivity(), UserProfile.class);
                                            intent.putExtra("userId", userId);
                                            startActivity(intent);

                                        }else if (items[which].equals("Start Chat")){
                                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                                            intent.putExtra("userId", userId);
                                            intent.putExtra("username", username);
                                            intent.putExtra("image", imageUri);
                                            startActivity(intent);
                                        }
                                    }
                                });
                                AlertDialog alert = dialog.create();
                                alert.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        friendsRv.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date){
            TextView usernameView = (TextView)mView.findViewById(R.id.users_statusTV);
            usernameView.setText(date);
        }
        public void setName(String name){
            TextView username = (TextView) mView.findViewById(R.id.users_nameTV);
            username.setText(name);
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

        public void setUserOnline(String online_status){
            ImageView onlineImage = (ImageView)mView.findViewById(R.id.onlineIV);

            if (online_status.equals("true")){
                onlineImage.setVisibility(View.VISIBLE);
            }else {
                onlineImage.setVisibility(View.INVISIBLE);
            }
        }
    }
}
