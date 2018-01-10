package chatme.apps.madnan.chatme.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.model.Messages;

/**
 * Created by mahmoud.adnan on 1/10/2018.
 */

public class MyMessageViewHolder extends RecyclerView.ViewHolder {

    TextView messageText;
    public MyMessageViewHolder(View itemView) {
        super(itemView);

        messageText = (TextView)itemView.findViewById(R.id.myMessageTV);
    }

    public void setMyMessage(Messages message){
        messageText.setText(message.getMessage());
    }
}
