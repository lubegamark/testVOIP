package com.peppermint.peppermint.ui;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.peppermint.peppermint.R;
import com.peppermint.peppermint.adapter.MessageListAdapter;
import com.peppermint.peppermint.model.TextMessage;
import com.rockerhieu.emojicon.EmojiconsFragment.OnEmojiconBackspaceClickedListener;
import java.util.ArrayList;
import java.util.Random;


public class ChatActivity extends BaseActivity implements OnEmojiconBackspaceClickedListener
{
ImageButton sendButton;
EditText messageBox;
Boolean isOnline;
ListView chatListView;
Context c = this;
MessageListAdapter chatAdapter;
//	XMPPConnection xmppConnection;
static String jid= null;
BroadcastReceiver messageReceiver;
ArrayList<TextMessage> messageList = new ArrayList<TextMessage>();
BroadcastReceiver connectedReceiver;
public static boolean isActive = false;
public static final String MESSAGERECEIVER = "com.lucidace.wolokoso.MESSAGERECEIVER";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		final DatabaseHandler databaseHandler = new DatabaseHandler(c);
//		final SQLiteDatabase db = databaseHandler.getWritableDatabase();
//		xmppConnection = MyXmppConnection.getInstance().retrieve("1");
//
		setContentView(R.layout.activity_in_chat);

		sendButton = (ImageButton)findViewById(R.id.send_button);
		messageBox = (EditText)findViewById(R.id.message_box);
		chatListView = (ListView)findViewById(R.id.chat_list_view);
		//messageList = databaseHandler.listMessages(jid);
		chatAdapter = new MessageListAdapter(this, messageList);
		chatListView.setAdapter(chatAdapter);
		//isOnline = Utils.isDataConnected(c, xmppConnection);
		Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);



		sendButton.setOnClickListener(new View.OnClickListener()
		{	
			@Override
			public void onClick(View v) 
			{
				if(messageBox.getText().length()>0)
				{
					Boolean mine = false;
					//mine = !mine;
					Random random = new Random();
					mine = random.nextBoolean();
					TextMessage wm= new TextMessage(messageBox.getText().toString(), System.currentTimeMillis(), mine, "John");
					//Log.e("Is Online",Boolean.toString(isOnline));
					//databaseHandler.addMessage(db, wm, isOnline);
					//Message msg = new Message(jid, Message.Type.chat);
					//msg.setBody(wm.getMessageText());
					messageList.add(wm);
					messageBox.setText("");
					//sendMessage(msg);
					chatAdapter.notifyDataSetChanged();
				}
			}
		}); 
	
	}

	@Override
	protected void onStart() {
	    super.onStart();

	}

	@Override
	protected void onStop() {
	    super.onStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isActive = false;
		LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
		this.unregisterReceiver(connectedReceiver);
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isActive = true;
		LocalBroadcastManager.getInstance(this).registerReceiver((messageReceiver), new IntentFilter(MESSAGERECEIVER));
		this.registerReceiver((connectedReceiver), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}

	@Override
	public void onEmojiconBackspaceClicked(View view) {

	}
}
