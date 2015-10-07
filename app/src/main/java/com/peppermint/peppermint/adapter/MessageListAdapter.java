package com.peppermint.peppermint.adapter;


import android.content.Context;
import android.text.format.Time;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.peppermint.peppermint.R;
import com.peppermint.peppermint.model.TextMessage;

import java.util.ArrayList;

public class MessageListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<TextMessage> messages;

    public MessageListAdapter(Context c) {
        mContext = c;
    }

    public MessageListAdapter(Context c, ArrayList<TextMessage> u) {
        mContext = c;
        messages = u;
    }

    @Override
    public int getCount() {
        if (messages != null)
            return messages.size();

        return 0;
    }

    public TextMessage getItem(int position) {
        if (messages != null)
            return messages.get(position);
        return null;
    }

    public long getItemId(int position) {
        if (messages != null)
            return messages.get(position).hashCode();
        return 0;
    }

    public ArrayList<TextMessage> getItemList() {
        return messages;
    }

    public void setItemList(ArrayList<TextMessage> itemList) {
        this.messages = itemList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        //if (convertView == null) {
        TextMessage message = messages.get(position);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (message.isMine()) {
            rowView = inflater.inflate(R.layout.message_list_sent, parent, false);
//            LinearLayout chat_bubble = (LinearLayout) rowView.findViewById(R.id.chat_bubble);
//            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
//            chat_bubble.setLayoutParams(layoutParams);
        }
        //If not mine then it is from sender align to left
        else {
            rowView = inflater.inflate(R.layout.message_list_received, parent, false);
//            LinearLayout chat_bubble = (LinearLayout) rowView.findViewById(R.id.chat_bubble);
//            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.LEFT);
//            chat_bubble.setLayoutParams(layoutParams);
        }


        TextView messageText = (TextView) rowView.findViewById(R.id.message_text);
        TextView messageTime = (TextView) rowView.findViewById(R.id.message_time);

        Time time = new Time();
        time.set(message.getTime());
        messageText.setText(message.getMessageText());
        messageTime.setText(time.format("%I:%M %p"));

        //} else {
        // rowView =  convertView;
        //}

        return rowView;
    }

    public ArrayList<TextMessage> getArticles() {
        return messages;
    }

    public void setArticles(ArrayList<TextMessage> mArticles) {
        this.messages = mArticles;
    }
}