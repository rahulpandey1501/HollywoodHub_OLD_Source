package com.rahul.hollywoodhub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rahul on 8/3/16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder> {

    List<Information> list;
    List<DownloadLinkInfo> dlist;
    Context context;
    boolean fromMovieInfo;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;

    public RecyclerViewAdapter(Context context, List<Information> list, boolean fromMovieInfo) {
        this.list = list;
        this.context = context;
        this.fromMovieInfo = fromMovieInfo;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return TYPE_HEADER;
            default:
                return TYPE_CELL;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = null;
        View view;
        if (fromMovieInfo)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card_small, parent, false);
        else view = LayoutInflater.from(parent.getContext()).inflate(R.layout.download_list_list, parent, false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
//        switch (viewType) {
//            case TYPE_HEADER: {
//                view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.list_item_card_big, parent, false);
//                return new CustomViewHolder(view) {
//                };
//            }
//            case TYPE_CELL: {
//                view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.list_item_card_small, parent, false);
//                return new CustomViewHolder(view) {
//                };
//            }
//        }
//        return null;
    }


    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        final Information information = list.get(position);
        if(fromMovieInfo) {
            Picasso.with(context).load(information.image).placeholder(R.drawable.placeholder).into(holder.image);
            holder.title.setText(information.title);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MovieInfo.class);
                    intent.putExtra("link", information.link);
                    intent.putExtra("image", information.image);
                    intent.putExtra("title", information.title);
                    context.startActivity(intent);
                }
            });
        }
        else{
            holder.dTitle.setText(information.downloadLinkInfo.quality+"   "+information.downloadLinkInfo.title);
            holder.dLink.setText(information.downloadLinkInfo.link);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(information.downloadLinkInfo.link));
                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(browserIntent);
                }
            });
        }
//        switch (getItemViewType(position)) {
//            case TYPE_HEADER:
//                break;
//            case TYPE_CELL:
//                break;
//        }
    }

    class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView title, dTitle, dLink;
        ImageView image;
        public CustomViewHolder(View itemView) {
            super(itemView);
            if (fromMovieInfo) {
                image = (ImageView) itemView.findViewById(R.id.movie_IV);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                title = (TextView) itemView.findViewById(R.id.movie_title_TV);
            }
            else {
                dTitle = (TextView) itemView.findViewById(R.id.dTitle);
                dLink = (TextView) itemView.findViewById(R.id.dLink);
            }
        }
    }
}
