package com.remitty.caronz.messages.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import com.remitty.caronz.R;
import com.remitty.caronz.helper.SendReciveONClickListner;
import com.remitty.caronz.models.ReceivedMessageModel;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemSendRecMesageAdapter extends RecyclerView.Adapter<ItemSendRecMesageAdapter.CustomViewHolder> {
    private List<ReceivedMessageModel> feedItemList;
    private Context mContext;
    private SendReciveONClickListner oNItemClickListener;


    public ItemSendRecMesageAdapter(Context context, List<ReceivedMessageModel> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_sent_message, null);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        final ReceivedMessageModel feedItem = feedItemList.get(i);

        customViewHolder.tvLast.setText(feedItemList.get(i).getLastSenderName());
        customViewHolder.name.setText(feedItemList.get(i).getTopic());
        customViewHolder.tvLastMsg.setText(feedItemList.get(i).getLastMessage());
        customViewHolder.loginTime.setText(feedItemList.get(i).getChatTime());
        if (!feedItem.isMessageRead()) {
            customViewHolder.card_view.setCardBackgroundColor(Color.parseColor("#fffcf5"));
//            customViewHolder.notification_icon.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(feedItem.getTumbnail())) {
            Picasso.with(mContext).load(feedItem.getTumbnail())
                    .error(R.drawable.placeholder)
                    .placeholder(R.drawable.placeholder)
                    .into(customViewHolder.imageView);
        }

        View.OnClickListener listener = v -> oNItemClickListener.onItemClick(feedItem);

        customViewHolder.linearLayout.setOnClickListener(listener);
        customViewHolder.delete_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oNItemClickListener.onItemDelete(feedItem, i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    public SendReciveONClickListner getOnItemClickListener() {
        return oNItemClickListener;
    }

    public void setOnItemClickListener(SendReciveONClickListner onItemClickListener) {
        this.oNItemClickListener = onItemClickListener;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        TextView name, loginTime,tvLastMsg, chatTime, tvLast;
        LinearLayout linearLayout;
        CardView card_view;
        ImageView notification_icon, delete_icon;

        CustomViewHolder(View view) {
            super(view);
//            this.is_block=view.findViewById(R.id.Blocktext_view);
            this.linearLayout = view.findViewById(R.id.linear_layout_card_view);
            this.imageView = view.findViewById(R.id.image_view);
            this.name = view.findViewById(R.id.text_viewName);
            this.notification_icon = view.findViewById(R.id.notification_icon);
            this.delete_icon = view.findViewById(R.id.delete_icon);
            this.loginTime = view.findViewById(R.id.loginTime);
            this.card_view = view.findViewById(R.id.card_view);
            this.tvLastMsg = view.findViewById(R.id.tv_last_msg);
            this.tvLast = view.findViewById(R.id.tv_last);
        }
    }
}