package ru.lisdevs.vkmp.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.lisdevs.vkmp.R;
import ru.lisdevs.vkmp.model.Search;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnMenuClickListener {
        void onMenuClick(Search search);
    }

    private List<Search> searchList;
    private OnItemClickListener itemClickListener;
    private OnMenuClickListener menuClickListener;
    private int currentlyPlayingPosition = -1;

    public SearchAdapter(List<Search> searchList) {
        this.searchList = searchList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.menuClickListener = listener;
    }

    public void setCurrentlyPlayingPosition(int position) {
        int oldPosition = currentlyPlayingPosition;
        currentlyPlayingPosition = position;
        if (oldPosition != -1) notifyItemChanged(oldPosition);
        if (position != -1) notifyItemChanged(position);
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audio, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Search search = searchList.get(position);
        holder.artistText.setText(search.getArtist());
        holder.titleText.setText(search.getTitle());

        // Подсветка текущего трека
        if (position == currentlyPlayingPosition) {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.circle_video));
        } else {
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });

        holder.menuButton.setOnClickListener(v -> {
            if (menuClickListener != null) {
                menuClickListener.onMenuClick(search);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView artistText;
        TextView titleText;
        ImageView menuButton;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            artistText = itemView.findViewById(R.id.artistText);
            titleText = itemView.findViewById(R.id.titleText);
            menuButton = itemView.findViewById(R.id.downloadButton);
        }
    }
}