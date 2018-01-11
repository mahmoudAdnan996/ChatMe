package chatme.apps.madnan.chatme.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.model.Messages;

/**
 * Created by mahmoud.adnan on 1/10/2018.
 */

public class MyMessageViewHolder extends RecyclerView.ViewHolder {

    TextView messageText;
    ImageView messageImage;

    public MyMessageViewHolder(View itemView) {
        super(itemView);

        messageText = (TextView)itemView.findViewById(R.id.myMessageTV);
        messageImage = (ImageView)itemView.findViewById(R.id.myMessageImg);
    }

    public void setMyMessage(Messages message){
        messageText.setText(message.getMessage());
    }

    public void setMyMessageImage(Messages messages){
        Picasso.with(messageImage.getContext()).load(messages.getMessage())
                .placeholder(R.drawable.profile).into(messageImage);
    }
}
