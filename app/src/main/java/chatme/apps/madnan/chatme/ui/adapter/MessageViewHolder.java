package chatme.apps.madnan.chatme.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.model.Messages;
import chatme.apps.madnan.chatme.ui.ChatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by mahmoud.adnan on 1/10/2018.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {

    CircleImageView userImg;
    TextView userMessage;
    ImageView userMessageImage;

    public MessageViewHolder(View itemView) {
        super(itemView);

        userImg = (CircleImageView)itemView.findViewById(R.id.messageUserIV);
        userMessage = (TextView)itemView.findViewById(R.id.messageUserTV);
        userMessageImage = (ImageView)itemView.findViewById(R.id.messageUserImg);
    }
    public void setUserMessageView(Messages messages, Context context){

        userMessage.setText(messages.getMessage());
    }

    public void setUserMessageImage(Messages messages){
        userMessageImage.setVisibility(View.GONE);
        Picasso.with(userMessageImage.getContext()).load(messages.getMessage()).into(userMessageImage, new Callback() {
            @Override
            public void onSuccess() {
                userMessageImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {

            }
        });
    }
}
