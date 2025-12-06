package ru.lisdevs.vkmp.official.audios;

import android.os.Parcel;
import android.os.Parcelable;

public class Audio implements Parcelable {
    private String artist;
    private String title;
    private String url;
    private int duration;
    private int genreId;
    private long audioId;
    private long ownerId;

    public Audio(String artist, String title, String url) {
        this.artist = artist;
        this.title = title;
        this.url = url;
    }

    // Геттеры и сеттеры
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getGenreId() { return genreId; }
    public void setGenreId(int genreId) { this.genreId = genreId; }

    public long getAudioId() { return audioId; }
    public void setAudioId(long audioId) { this.audioId = audioId; }

    public long getOwnerId() { return ownerId; }
    public void setOwnerId(long ownerId) { this.ownerId = ownerId; }

    // Parcelable implementation
    protected Audio(Parcel in) {
        artist = in.readString();
        title = in.readString();
        url = in.readString();
        duration = in.readInt();
        genreId = in.readInt();
        audioId = in.readLong();
        ownerId = in.readLong();
    }

    public static final Creator<Audio> CREATOR = new Creator<Audio>() {
        @Override
        public Audio createFromParcel(Parcel in) {
            return new Audio(in);
        }

        @Override
        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeInt(duration);
        dest.writeInt(genreId);
        dest.writeLong(audioId);
        dest.writeLong(ownerId);
    }
}