package com.example.admin.simpleaudiorecoder;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import static android.R.attr.data;


/**
 * Created by Admin on 07.03.2017.
 */

public class VoiceAdapter extends RecyclerView.Adapter<VoiceAdapter.ViewHolder> {

    final private ListItemClickListener mOnClickListener;
    List<Voice> mVoiceList;


    public interface ListItemClickListener {
        void onListItemClick(String fileName);
    }

    public VoiceAdapter(List<Voice> voiceList,ListItemClickListener listener){
        mVoiceList=voiceList;
        mOnClickListener=listener;
    }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.voice_item,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {
         Voice currentVoice = mVoiceList.get(position);

        holder.filename=currentVoice.getmVoiceTitle();
        holder.titleTextView.setText(currentVoice.getmVoiceTitle());
        holder.dateTextView.setText(currentVoice.getmVoiceDate());
        holder.durationTextView.setText(currentVoice.getmVoiceDuration());

        holder.itemView.setTag(currentVoice.getmVoiceTitle());


    }

    @Override
    public int getItemCount() {
        return mVoiceList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView titleTextView;
        TextView dateTextView;
        TextView durationTextView;
        String filename;
        View mItemView;
        public ViewHolder(View itemView) {
            super(itemView);

            mItemView=itemView;
            titleTextView=(TextView)itemView.findViewById(R.id.voice_title_text_view);
            dateTextView = (TextView) itemView.findViewById(R.id.voice_date_text_view);
            durationTextView = (TextView) itemView.findViewById(R.id.voice_duration_text_view);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            mOnClickListener.onListItemClick(filename);


        }


    }


}
