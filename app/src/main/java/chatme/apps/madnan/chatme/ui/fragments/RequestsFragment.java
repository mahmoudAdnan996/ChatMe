package chatme.apps.madnan.chatme.ui.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import chatme.apps.madnan.chatme.model.Users;
import chatme.apps.madnan.chatme.ui.UserProfile;
import de.hdodenhof.circleimageview.CircleImageView;

import static chatme.apps.madnan.chatme.utils.Constants.FRIEND_REQUEST_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.THUMP_IMAGE;
import static chatme.apps.madnan.chatme.utils.Constants.USERS_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.USER_ID;
import static chatme.apps.madnan.chatme.utils.Constants.USER_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    RecyclerView requestsRV;
    DatabaseReference mRequestsDatabase;
    DatabaseReference mUserDatabase;
    FirebaseAuth mAuth;

    String currentUserId;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        requestsRV = (RecyclerView)view.findViewById(R.id.requestsRV);
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();
        mRequestsDatabase = FirebaseDatabase.getInstance().getReference().child(FRIEND_REQUEST_TABLE).child(currentUserId);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(USERS_TABLE);
        mUserDatabase.keepSynced(true);

        requestsRV.setHasFixedSize(true);
        requestsRV.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, RequestsFragment.RequestsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, RequestsFragment.RequestsViewHolder>(
                Users.class,
                R.layout.users_rec_item,
                RequestsFragment.RequestsViewHolder.class,
                mRequestsDatabase) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, Users model, int position) {

                final String userId = getRef(position).getKey();
                mUserDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String username = dataSnapshot.child(USER_NAME).getValue().toString();
                        String imageUri = dataSnapshot.child(THUMP_IMAGE).getValue().toString();

                        viewHolder.setName(username);
                        viewHolder.setThumbImage(imageUri, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(), UserProfile.class);
                                intent.putExtra(USER_ID, userId);
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

        requestsRV.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setStatus(String status){
            TextView usernameView = (TextView)mView.findViewById(R.id.users_statusTV);
            usernameView.setText(status);
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

    }
}
