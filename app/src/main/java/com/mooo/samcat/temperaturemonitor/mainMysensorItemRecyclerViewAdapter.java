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
public class mainMysensorItemRecyclerViewAdapter extends RecyclerView.Adapter<mainMysensorItemRecyclerViewAdapter.ViewHolder> {

    private final List<sensor> mValues;
    private final OnListFragmentInteractionListener mListener;

    public mainMysensorItemRecyclerViewAdapter(List<sensor> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_sensoritem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).getName());
        holder.mTempView.setText(Integer.toString(mValues.get(position).getTemperature()));
        holder.mBattView.setText(Float.toString(mValues.get(position).getBattery()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.OnListFragmentInteraction(holder.mItem);
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
        public final TextView mNameView;
        public final TextView mTempView;
        public final TextView mBattView;
        public sensor mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.sensor_name);
            mTempView = (TextView) view.findViewById(R.id.sensor_temperature);
            mBattView = (TextView) view.findViewById(R.id.sensor_battery);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTempView.getText() + "'";
        }
    }
}
