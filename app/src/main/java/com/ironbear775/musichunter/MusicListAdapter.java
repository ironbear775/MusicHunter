package com.ironbear775.musichunter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

/**
 * Created by ironbear775 on 2017/12/30.
 */

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicListViewHolder> {
    private ArrayList<Music> mList;
    private LayoutInflater mInflater;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    MusicListAdapter(Context context, ArrayList<Music> list) {
        mList = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    public interface OnItemClickListener {
        void OnItemClick(View view, int position);

    }

    void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }


    @Override
    public MusicListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MusicListViewHolder(
                mInflater.inflate(R.layout.musiclist_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final MusicListViewHolder holder, int position) {
        if (mOnItemClickListener != null) {
            final int layoutPosition = holder.getLayoutPosition();
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnItemClick(holder.itemView, layoutPosition);
                }
            });
        }

        Glide.with(mContext)
                .load(mList.get(position).getAlbumArtUrl())
                .apply(RequestOptions.centerCropTransform()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder))
                .into(holder.imageView);
        holder.itemTitle.setText(mList.get(position).getTitle());
        String detail = mList.get(position).getArtist() + "-" + mList.get(position).getAlbum();
        holder.itemDetail.setText(detail);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MusicListViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle, itemDetail;
        SquareImageView imageView;

        MusicListViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_album_art);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemDetail = itemView.findViewById(R.id.item_itemDetail);
        }
    }
}
