package chatme.apps.madnan.chatme.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.model.Messages;

import static chatme.apps.madnan.chatme.utils.Constants.MY_MESSAGE_TYPE;
import static chatme.apps.madnan.chatme.utils.Constants.THUMP_IMAGE;
import static chatme.apps.madnan.chatme.utils.Constants.USERS_TABLE;
import static chatme.apps.madnan.chatme.utils.Constants.USER_MESSAGE_TYPE;

/**
 * Created by mahmoud.adnan on 1/10/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Messages> messagesList;
    FirebaseAuth mAuth;
    DatabaseReference mUserDatabaseRef;

    String fromId;
    String currentUserId;

    Context context;

    public MessageAdapter(List<Messages> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 123){
           return new MyMessageViewHolder(LayoutInflater.from(parent.getContext())
                   .inflate(R.layout.my_single_message, parent, false));
        }else {
            return new MessageViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_single_message_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        int viewType = getItemViewType(position);
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child(USERS_TABLE).child(fromId);

        if (viewType == MY_MESSAGE_TYPE){
            MyMessageViewHolder myMessageViewHolder = (MyMessageViewHolder)holder;
            myMessageViewHolder.setMyMessage(messagesList.get(position));

        }else {
            final MessageViewHolder messageViewHolder = (MessageViewHolder)holder;
            mUserDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String image = dataSnapshot.child(THUMP_IMAGE).getValue().toString();
                    messageViewHolder.setUserMessageView(messagesList.get(position), context);
                    Picasso.with(messageViewHolder.userImg.getContext()).load(image)
                            .placeholder(R.drawable.profile).into(messageViewHolder.userImg);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        Messages messages = messagesList.get(position);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        fromId = messages.getFrom();
        if (fromId.equals(currentUserId)){
            return MY_MESSAGE_TYPE;
        }
        else {
            return USER_MESSAGE_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }
}
