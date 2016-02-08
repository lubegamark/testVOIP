package com.peppermint.peppermint.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peppermint.peppermint.R;
import com.peppermint.peppermint.net.callback.BuddyCallback;
import com.peppermint.peppermint.net.handler.BuddyHandler;

import java.util.ArrayList;
import java.util.List;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;

public class UserListFragment extends Fragment implements BuddyCallback{
    private static final String TAG = makeLogTag(UserListActivity.class);
    Context mContext = getActivity();
    private RecyclerView mRecyclerView;
    private AvailableListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<String> peerList;
    private BuddyHandler buddyHandler;
    public UserListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String local_api = "http://"+getActivity().getIntent().getExtras().getString("local_api");
        buddyHandler = new BuddyHandler(getActivity(), local_api);
        buddyHandler.setBuddyCallback(this);
        buddyHandler.geBuddies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.available_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(false);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        peerList = new ArrayList<String>();

        // specify an adapter (see also next example)
        mAdapter = new AvailableListAdapter(mContext, peerList);
        mRecyclerView.setAdapter(mAdapter);

        registerForContextMenu(mRecyclerView);

        return rootView;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.BLOCK_USER:
                LOGD(TAG, "User blocked");
                return true;
            case R.id.MUTE_USER:
                LOGD(TAG, "User muted");
            case R.id.CLEAR_CHAT:
                LOGD(TAG, "Chat cleared");
                return true;
            case R.id.CLEAR_FROM_LOG:
                LOGD(TAG, "Conversations cleared");
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void getBudddiesResponseReceived(List<String> myBuddies) {
//        peerList.addAll(myBuddies);
        mAdapter.addItemsToList(myBuddies);
        LOGD(TAG, "Bongo" + myBuddies);
    }

    public class AvailableListAdapter extends RecyclerView.Adapter<AvailableListAdapter.ViewHolder>
     {
        Context mContext;
        private List<String> mDataset;
         private int selectedPos = 0;

        // Provide a suitable constructor (depends on the kind of dataset)
        public AvailableListAdapter(Context context,List<String> myDataset) {
            mDataset = myDataset;
            mContext = context;
        }
         public void addItemsToList(List<String> items){
             mDataset.addAll(items);
             notifyDataSetChanged();
             LOGD(TAG, "data added");
         }
        // Create new views (invoked by the layout manager)
        @Override
        public AvailableListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.available_list_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder((LinearLayout)v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.itemView.setSelected(true);
            holder.mTextView.setText(mDataset.get(position));
            holder.callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AccountManager am = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
                    Account[] accounts = am.getAccountsByType("com.peppermint.peppermint");
                    Account account = null;
                    if (accounts.length > 0) {
                        account = accounts[0];
                        Log.i("Account Name", account.name);
                    }
                    Intent i;
                    i = new Intent(
                            getActivity(),
                            CallActivity.class);

                    //Bundle extras= getActivity().getIntent().getExtras();
                    //extras.get("sipUsername");
                    //extras.get("sipDomain");
                    //extras.get("sipPassword");
                    i.putExtras(getActivity().getIntent().getExtras());

                    //Log.i("jid", jid);
                    startActivity(i);
                }
            });
            holder.chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AccountManager am = (AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE);
                    Account[] accounts = am.getAccountsByType("com.peppermint.peppermint");
                    Account account = null;
                    if (accounts.length > 0) {
                        account = accounts[0];
                        Log.i("Account Name", account.name);
                    }
                    Intent i;
                    i = new Intent(
                            getActivity(),
                            ChatActivity.class);
                    i.putExtra("sipUsername", "mark");
                    i.putExtra("sipDomain", "192.168.1.20");
                    i.putExtra("sipPassword", "bongo");
                    i.putExtras(getActivity().getIntent().getExtras());

                    startActivity(i);
                }
            });
            holder.itemView.setSelected(true);

        }


        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder implements OnCreateContextMenuListener {
            // each data item is just a string in this case
            public TextView mTextView;
            public ImageButton callButton;
            public ImageButton chatButton;


            public ViewHolder(LinearLayout v) {
                super(v);
                mTextView = (TextView)v.findViewById(R.id.available_name);
                callButton = (ImageButton)v.findViewById(R.id.call_button);
                chatButton = (ImageButton)v.findViewById(R.id.chat_button);
                v.setOnCreateContextMenuListener(this);
                v.setClickable(true);
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.menu_user, menu);
            }
        }
    }
}
