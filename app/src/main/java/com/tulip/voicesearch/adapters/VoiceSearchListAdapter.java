package com.tulip.voicesearch.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tulip.voicesearch.R;
import com.tulip.voicesearch.interfaces.OnClickListener;
import com.tulip.voicesearch.models.VoiceSearchModel;
import com.tulip.voicesearch.utils.Populate;

import java.util.List;

public class VoiceSearchListAdapter extends RecyclerView.Adapter<VoiceSearchListAdapter.SearchViewHolder> {

    private List<VoiceSearchModel> searchModels;
    private OnClickListener onClickListener;

    public VoiceSearchListAdapter(List<VoiceSearchModel> searchModels) {
        this.searchModels = searchModels;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.voice_search_item_list,parent,false
        );
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchViewHolder holder, int position) {
        holder.voiceSearchButton.setImageResource(searchModels.get(position).getImage());
        holder.name.setText(Populate.getAction(
                searchModels.get(position).getRequestCode()
        ));
        holder.voiceSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.setOnItemClickListener(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchModels.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageButton voiceSearchButton;
        TextView name;
        SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            voiceSearchButton = itemView.findViewById(R.id.voice_search_button);
            name = itemView.findViewById(R.id.name);
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
