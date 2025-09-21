package com.example.fyp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fyp.R;

import org.jspecify.annotations.NonNull;

public class IconSpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] items;
    private final int[] icons;

    public IconSpinnerAdapter(Context context, String[] items, int[] icons) {
        super(context, R.layout.spinner_item_with_icon, items);
        this.context = context;
        this.items = items;
        this.icons = icons;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }


    private View createItemView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.spinner_item_with_icon, parent, false);
        }

        ImageView icon = view.findViewById(R.id.icon);
        TextView text = view.findViewById(R.id.text);

        icon.setImageResource(icons[position]);
        text.setText(items[position]);

        return view;
    }
}

