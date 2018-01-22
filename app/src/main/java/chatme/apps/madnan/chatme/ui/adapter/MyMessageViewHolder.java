package chatme.apps.madnan.chatme.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.model.Messages;

/**
 * Created by mahmoud.adnan on 1/10/2018.
 */

public class MyMessageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.myMessageTV)
    TextView messageText;
    @BindView(R.id.myMessageImg)
    ImageView messageImage;

    public MyMessageViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    public void setMyMessage(Messages message){
        messageText.setText(message.getMessage());
    }

    public void setMyMessageImage(Messages messages){
        messageImage.setVisibility(View.GONE);
        Picasso.with(messageImage.getContext()).load(messages.getMessage()).into(messageImage, new Callback() {
            @Override
            public void onSuccess() {
                messageImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {

            }
        });
    }
}
