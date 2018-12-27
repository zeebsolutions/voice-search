package com.tulip.voicesearch.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tulip.voicesearch.R;
import com.tulip.voicesearch.interfaces.OnHistoryClickListener;
import com.tulip.voicesearch.models.VoiceSearchHistoryModel;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<VoiceSearchHistoryModel> models;
    private OnHistoryClickListener listener;

    public void setListener(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    public HistoryAdapter(List<VoiceSearchHistoryModel> models) {
        this.models = models;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_history,parent,false
        );
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int i) {
        String query = models.get(i).getQuery().substring(
                0,1
        ).toUpperCase()+models.get(i).getQuery().substring(1)+"  ";
        holder.query.setText(query);
        String action = models.get(i).getAction();
        holder.action.setText(action);
        if(listener!=null){
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClickListener(holder.getAdapterPosition());
                }
            });
            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onLongClickListener(holder.getAdapterPosition());
                    return true;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView action;
        TextView query;
        ViewHolder(@NonNull View view) {
            super(view);
            cardView = view.findViewById(R.id.card_view);
            action = view.findViewById(R.id.action);
            query = view.findViewById(R.id.query);

        }
    }
}
