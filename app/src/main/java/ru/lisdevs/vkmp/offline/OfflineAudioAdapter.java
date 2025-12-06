package ru.lisdevs.vkmp.offline;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.lisdevs.vkmp.R;

public class OfflineAudioAdapter extends RecyclerView.Adapter<OfflineAudioAdapter.AudioViewHolder> {
    private List<OfflineMusicFragment.LocalAudio> audioList;
    private OnItemClickListener listener;
    private OnMenuClickListener menuClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnMenuClickListener {
        void onMenuClick(OfflineMusicFragment.LocalAudio audio);
    }

    public OfflineAudioAdapter(List<OfflineMusicFragment.LocalAudio> audioList) {
        this.audioList = audioList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.menuClickListener = listener;
    }

    public void updateList(List<OfflineMusicFragment.LocalAudio> newList) {
        this.audioList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        OfflineMusicFragment.LocalAudio audio = audioList.get(position);
        holder.artistText.setText(audio.getArtist());
        holder.titleText.setText(audio.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });

        holder.menuButton.setOnClickListener(v -> {
            if (menuClickListener != null) {
                menuClickListener.onMenuClick(audio);
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView artistText;
        TextView titleText;
        ImageView menuButton;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            artistText = itemView.findViewById(R.id.artistText);
            titleText = itemView.findViewById(R.id.titleText);
            menuButton = itemView.findViewById(R.id.downloadButton);
        }
    }
}
