package ru.lisdevs.vkmp.music;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.lisdevs.vkmp.R;
import ru.lisdevs.vkmp.model.Audio;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private List<Audio> audioList;
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private MusicListFragment.OnMenuClickListener menuClickListener;

    public void setOnMenuClickListener(MusicListFragment.OnMenuClickListener listener) {
        this.menuClickListener = listener;
    }

    public AudioAdapter(List<Audio> audioList) {
        this.audioList = audioList;
    }

    public void updateList(List<Audio> newList) {
        this.audioList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        Audio audio = audioList.get(position);

        // Форматируем длительность
        String formattedDuration = formatDuration(audio.getDuration());

        // Объединяем артиста и длительность в одну строку
        String artistWithDuration = String.format("%s • %s", audio.getArtist(), formattedDuration);

        // Устанавливаем текст
        holder.artistText.setText(artistWithDuration);
        holder.titleText.setText(audio.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });

        holder.downloadButton.setOnClickListener(v -> {
            if (menuClickListener != null) {
                menuClickListener.onMenuClick(audio);
            }
        });
    }

    // Метод для форматирования длительности (секунды -> минуты:секунды)
    private String formatDuration(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView artistText;
        TextView titleText;
        ImageView downloadButton;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            artistText = itemView.findViewById(R.id.artistText);
            titleText = itemView.findViewById(R.id.titleText);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }
    }
}