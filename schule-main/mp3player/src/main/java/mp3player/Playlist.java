package mp3player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Playlist {

    private String name;
    private final ArrayList<Song> songs;

    public Playlist(String name) {
        setName(name);
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Der Playlist-Name darf nicht leer sein.");
        }
        this.name = name.trim();
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public List<Song> getSongsReadOnly() {
        return Collections.unmodifiableList(songs);
    }

    public boolean addSong(Song song) {
        if (song == null) {
            return false;
        }

        if (findById(song.getId()) != null) {
            return false;
        }

        songs.add(song);
        return true;
    }

    public boolean removeSongById(int id) {
        return songs.removeIf(song -> song.getId() == id);
    }

    public Song findById(int id) {
        for (Song song : songs) {
            if (song.getId() == id) {
                return song;
            }
        }
        return null;
    }

    public List<Song> search(String query) {
        List<Song> result = new ArrayList<>();

        if (query == null || query.isBlank()) {
            return result;
        }

        for (Song song : songs) {
            if (song.matches(query)) {
                result.add(song);
            }
        }

        return result;
    }

    public int getTotalDurationSeconds() {
        int total = 0;

        for (Song song : songs) {
            total += song.getDurationSeconds();
        }

        return total;
    }

    public String getTotalDurationFormatted() {
        int total = getTotalDurationSeconds();
        int hours = total / 3600;
        int minutes = (total % 3600) / 60;
        int seconds = total % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public double getAverageDurationSeconds() {
        if (songs.isEmpty()) {
            return 0.0;
        }

        return (double) getTotalDurationSeconds() / songs.size();
    }

    public Song getShortestSong() {
        if (songs.isEmpty()) {
            return null;
        }

        Song shortest = songs.get(0);

        for (Song song : songs) {
            if (song.getDurationSeconds() < shortest.getDurationSeconds()) {
                shortest = song;
            }
        }

        return shortest;
    }

    public Song getLongestSong() {
        if (songs.isEmpty()) {
            return null;
        }

        Song longest = songs.get(0);

        for (Song song : songs) {
            if (song.getDurationSeconds() > longest.getDurationSeconds()) {
                longest = song;
            }
        }

        return longest;
    }

    public int nextId() {
        int max = 0;

        for (Song song : songs) {
            if (song.getId() > max) {
                max = song.getId();
            }
        }

        return max + 1;
    }

    public int size() {
        return songs.size();
    }

    public boolean isEmpty() {
        return songs.isEmpty();
    }
}
