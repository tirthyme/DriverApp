package com.project.driverapp.Utilities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.driverapp.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private Context context;
    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<String> sr_no;
    private ArrayList<String> cust_name;
    private ArrayList<String> mp_name;

    public RecyclerViewAdapter(Context context, ArrayList<String> sr_no, ArrayList<String> cust_name, ArrayList<String> mp_name) {
        this.context = context;
        this.sr_no = sr_no;
        this.cust_name = cust_name;
        this.mp_name = mp_name;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_listitem, parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return  viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.no.setText(sr_no.get(position));
        holder.mtpointname.setText(mp_name.get(position));
        holder.name.setText(cust_name.get(position));
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, cust_name.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cust_name.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView no, name, mtpointname;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            no = itemView.findViewById(R.id.sr_no);
            name = itemView.findViewById(R.id.customer_name);
            mtpointname = itemView.findViewById(R.id.meetingpoint_name);
            linearLayout = itemView.findViewById(R.id.parent_layout);

        }
    }
}
