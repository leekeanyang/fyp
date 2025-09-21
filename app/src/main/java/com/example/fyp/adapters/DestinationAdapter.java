package com.example.fyp.adapters;

import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fyp.R;
import com.example.fyp.activities.DestinationDetailActivity;
import com.example.fyp.models.Destination;

import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {
    private List<Destination> destinations;

    public DestinationAdapter(List<Destination> destinations) {
        this.destinations = destinations;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_destination, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Destination destination = destinations.get(position);
        holder.name.setText(destination.getName());
        holder.description.setText(destination.getDescription());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), DestinationDetailActivity.class);
            intent.putExtra("destination", (Parcelable) destination);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, description;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.destination_name);
            description = itemView.findViewById(R.id.destination_description);
        }
    }
}