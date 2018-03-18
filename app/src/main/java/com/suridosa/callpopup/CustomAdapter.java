package com.suridosa.callpopup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by suridosa on 2018-02-13.
 */

public class CustomAdapter extends ArrayAdapter<CommDomain> {

    private ArrayList<CommDomain> items;
    private int textViewResourceId;
    private Context context;

    public CustomAdapter(Context context, int textViewResourceId, ArrayList<CommDomain> items) {
        super(context, textViewResourceId, items);
        Log.e("CustomAdapter","생성!!");
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Log.e("Adapter >>> position : ", position+"");
        if (v == null) {
            Log.e("Adapter >>>", " v is null ");
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(textViewResourceId, null);
        }
        CommDomain domain = items.get(position);
        if (domain != null) {
            TextView tvName = (TextView) v.findViewById(R.id.tvName);
            TextView tvNumber = (TextView) v.findViewById(R.id.tvNumber);
            TextView tvAddress = (TextView) v.findViewById(R.id.tvAddress);
            TextView tvRecvDt = (TextView) v.findViewById(R.id.tvRecvDt);
            final TextView tvLat = (TextView) v.findViewById(R.id.tvLat);
            TextView tvLng = (TextView) v.findViewById(R.id.tvLng);


            if (tvName != null){
                tvName.setText(domain.getName());
            }
            if (tvNumber != null){
                tvNumber.setText(domain.getNumber());
            }
            if (tvAddress != null){
                tvAddress.setText(domain.getAddress());
            }
            if (tvRecvDt != null){
                tvRecvDt.setText(domain.getRecvDt());
            }
            final String tmpLat = domain.getLat();
            final String tmpLng = domain.getLng();

            if (tvLat != null){
                tvLat.setText(domain.getLat());
            }
            if (tvLng != null){
                tvLng.setText(domain.getLng());
            }

            Button btnShowMap = (Button) v.findViewById(R.id.btnShowMap);
            btnShowMap.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.e("CustomAdapter","OnClick!!");
                            Intent intent = new Intent(context, MapActivity.class);
                            Log.e("CustomAdapter","tmpLat :: "+tmpLat);
                            Log.e("CustomAdapter","tmpLng :: "+tmpLng);

                            intent.putExtra("Lat",tmpLat);
                            intent.putExtra("Lng",tmpLng);
                            //Intent intent = new Intent(Intent.ACTION_VIEW);
                            //intent.setData(Uri.parse("daummaps://look?p="+tmpLat+","+tmpLng));
                            context.startActivity(intent);
                        }
                    }
            );
        }
        return v;
    }

    @Override
    public int getCount() {
        Log.e("CustomAdapter","getCount :: "+items.size());
        return items.size();
    }

}
