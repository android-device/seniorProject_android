package com.mooo.samcat.temperaturemonitor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

//import com.mooo.samcat.temperaturemonitor.dummy.DummyContent.DummyItem;

/**
 *
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ThresholdItemRecyclerViewAdapter extends RecyclerView.Adapter<ThresholdItemRecyclerViewAdapter.ViewHolder> {

    private final List<Integer> mValues;
    private final OnThresholdListFragmentInteraction mListener;

    public ThresholdItemRecyclerViewAdapter(List<Integer> items, OnThresholdListFragmentInteraction listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_thresholditem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mValueView.setText(mValues.get(position).toString());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.OnThresholdListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mValueView;

        public Integer mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mValueView = (TextView) view.findViewById(R.id.notification_value);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mValueView.getText() + "'";
        }
    }
}
