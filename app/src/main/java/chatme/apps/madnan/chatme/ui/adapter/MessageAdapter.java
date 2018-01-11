package chatme.apps.madnan.chatme.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import static chatme.apps.madnan.chatme.utils.Constants.MESSAGE_IMAGE;
import static chatme.apps.madnan.chatme.utils.Constants.MESSAGE_TEXT;
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

    Messages messages;

    public MessageAdapter(List<Messages> messagesList, Context context) {
        this.messagesList = messagesList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == MY_MESSAGE_TYPE){
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
            if (messagesList.get(position).getType().equals(MESSAGE_TEXT)){
                myMessageViewHolder.setMyMessage(messagesList.get(position));
                myMessageViewHolder.messageImage.setVisibility(View.GONE);
            }
            if (messagesList.get(position).getType().equals(MESSAGE_IMAGE)){
                myMessageViewHolder.messageText.setVisibility(View.GONE);
                myMessageViewHolder.setMyMessageImage(messagesList.get(position));
            }
            else {
                Log.e("Error is", "error occurred");
            }


        }
        else {
            final MessageViewHolder messageViewHolder = (MessageViewHolder)holder;
            mUserDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String image = dataSnapshot.child(THUMP_IMAGE).getValue().toString();
                    if (messagesList.get(position).getType().equals(MESSAGE_TEXT)){
                        messageViewHolder.setUserMessageView(messagesList.get(position), context);
                        messageViewHolder.userMessageImage.setVisibility(View.GONE);
                    }
                    if (messagesList.get(position).getType().equals(MESSAGE_IMAGE)){
                        messageViewHolder.userMessage.setVisibility(View.GONE);
                        messageViewHolder.setUserMessageImage(messagesList.get(position));
                    }
                    else {
                        Log.e("Error is", "error occurred");
                    }
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
        messages = messagesList.get(position);
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
