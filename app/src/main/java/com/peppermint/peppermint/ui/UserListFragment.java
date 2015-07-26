package com.peppermint.peppermint.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peppermint.peppermint.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class UserListFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    Context mContext = getActivity();

    public UserListFragment() {
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

        String [] myDataset = new String[]{"John", "James Muvabulaya N'ekitooke", "Mary"};

        // specify an adapter (see also next example)
        mAdapter = new AvailableListAdapter(mContext, myDataset);
        mRecyclerView.setAdapter(mAdapter);


        //return inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    public class AvailableListAdapter extends RecyclerView.Adapter<AvailableListAdapter.ViewHolder> {
        private String[] mDataset;
        Context mContext;
        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextView;
            public ImageButton callButton;
            public ImageButton chatButton;
            public ImageButton moreButton;

            public ViewHolder(LinearLayout v) {
                super(v);
                mTextView = (TextView)v.findViewById(R.id.available_name);
                callButton = (ImageButton)v.findViewById(R.id.call_button);
                chatButton = (ImageButton)v.findViewById(R.id.chat_button);
                moreButton = (ImageButton)v.findViewById(R.id.more_button);

            }
        }


        // Provide a suitable constructor (depends on the kind of dataset)
        public AvailableListAdapter(Context context,String[] myDataset) {
            mDataset = myDataset;
            mContext = context;
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
            holder.mTextView.setText(mDataset[position]);
            holder.callButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    Intent i;
                    i = new Intent(
                            getActivity(),
                            CallActivity.class);
                    //i.putExtra("jid", jid);
                    //Log.i("jid", jid);
                    startActivity(i);
                }
            });

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }

}
