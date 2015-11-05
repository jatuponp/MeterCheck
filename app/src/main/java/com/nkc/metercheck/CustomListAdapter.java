package com.nkc.metercheck;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nkc.metercheck.model.Room;

import java.util.List;

/**
 * Created by Jumpon-pc on 4/10/2558.
 */
public class CustomListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Room> roomItems;
    //ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public CustomListAdapter(Activity activity, List<Room> roomItems) {
        this.activity = activity;
        this.roomItems = roomItems;
    }

    @Override
    public int getCount() {
        return roomItems.size();
    }

    @Override
    public Object getItem(int location) {
        return roomItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        TextView room_id = (TextView) convertView.findViewById(R.id.room_id);
        TextView meter_start = (TextView) convertView.findViewById(R.id.meter_start);
        TextView meter_end = (TextView) convertView.findViewById(R.id.meter_end);

        // getting movie data for the row
        Room r = roomItems.get(position);

        // room_id
        room_id.setText(r.getRoomId());

        // meter_start
        meter_start.setText(r.getMeterStart());

        // meter_end
        meter_end.setText(r.getMeterEnd());

        return convertView;
    }
}
