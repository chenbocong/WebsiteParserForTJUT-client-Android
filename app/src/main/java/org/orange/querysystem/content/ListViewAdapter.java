package org.orange.querysystem.content;

import org.orange.querysystem.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ListViewAdapter extends BaseAdapter {

    private List<String> items;

    private LayoutInflater inflater;

    public ListViewAdapter(Context context, List<String> items) {
        this.items = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_item, null);
        }
        TextView text = (TextView) view.findViewById(R.id.list_item);
        text.setText(items.get(position));
        return view;
    }

}
