package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SoftwareRecyclerViewAdapter extends RecyclerView.Adapter<SoftwareRecyclerViewAdapter.ViewHolder>  {
    private ArrayList<MainActivity.Soft> localDataSet;

    private ItemClickListener mClickListener;

    private Activity activity;
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView p_name_textView, p_lic_textView, p_mauf_textView, p_description_textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            p_name_textView = view.findViewById(R.id.lable_name);
            p_lic_textView = view.findViewById(R.id.lable_license);
            p_mauf_textView = view.findViewById(R.id.lable_manufacturer);
            p_description_textView = view.findViewById(R.id.lable_description);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
                }
            });

            view.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (mClickListener != null)
                        return mClickListener.onLongItemClick(view, getAdapterPosition());
                    return false;
                }

            });
        }

    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public SoftwareRecyclerViewAdapter(Activity activity, ArrayList<MainActivity.Soft> dataSet) {
        localDataSet = dataSet;
        this.activity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.my_row, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        //название прогрммного обеспечения
        viewHolder.p_name_textView.setText(String.valueOf(localDataSet.get(position).name));
        //лицензия распространения
        viewHolder.p_lic_textView.setText(activity.getString(R.string.p_license)+ System.lineSeparator() + localDataSet.get(position).license);
        //производитель программного обеспечения:
        viewHolder.p_mauf_textView.setText(activity.getString(R.string.p_manufacturer)+ System.lineSeparator()+localDataSet.get(position).manufacturer);
        //описание программного обеспечения
        viewHolder.p_description_textView.setText(activity.getString(R.string.p_description)+ System.lineSeparator()+ localDataSet.get(position).description);

    }

    // возвращает размер коллекции
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
        boolean onLongItemClick(View view, int position);
    }
}

