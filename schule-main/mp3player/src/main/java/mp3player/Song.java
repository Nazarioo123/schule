package mp3player;

import java.util.Objects;

public class Song {

    private int id;
    private String title;
    private String artist;
    private String album;
    private int durationSeconds;
    private String genre;
    private int year;

    public Song(int id, String title, String artist, String album,
                int durationSeconds, String genre, int year) {

        if (id <= 0) {
            throw new IllegalArgumentException("Die Song-ID muss größer als 0 sein.");
        }

        if (durationSeconds < 0) {
            throw new IllegalArgumentException("Die Dauer darf nicht negativ sein.");
        }

        this.id = id;
        this.title = normalize(title);
        this.artist = normalize(artist);
        this.album = normalize(album);
        this.durationSeconds = durationSeconds;
        this.genre = normalize(genre);
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Die Song-ID muss größer als 0 sein.");
        }
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = normalize(title);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = normalize(artist);
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = normalize(album);
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        if (durationSeconds < 0) {
            throw new IllegalArgumentException("Die Dauer darf nicht negativ sein.");
        }
        this.durationSeconds = durationSeconds;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = normalize(genre);
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean matches(String query) {
        if (query == null || query.isBlank()) {
            return false;
        }

        String q = query.trim().toLowerCase();

        return title.toLowerCase().contains(q)
                || artist.toLowerCase().contains(q)
                || album.toLowerCase().contains(q)
                || genre.toLowerCase().contains(q)
                || String.valueOf(year).contains(q);
    }

    public String toCsv() {
        return id + ";"
                + escape(title) + ";"
                + escape(artist) + ";"
                + escape(album) + ";"
                + durationSeconds + ";"
                + escape(genre) + ";"
                + year;
    }

    public static Song fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Leere CSV-Zeile.");
        }

        String[] parts = line.split(";", -1);

        if (parts.length != 7) {
            throw new IllegalArgumentException("Ungültige CSV-Zeile: " + line);
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String title = parts[1].trim();
            String artist = parts[2].trim();
            String album = parts[3].trim();
            int durationSeconds = Integer.parseInt(parts[4].trim());
            String genre = parts[5].trim();
            int year = Integer.parseInt(parts[6].trim());

            return new Song(id, title, artist, album, durationSeconds, genre, year);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ungültige Zahl in CSV-Zeile: " + line, e);
        }
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }
        return value.trim();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(";", ",").trim();
    }

    @Override
    public String toString() {
        return String.format(
                "[%3d] %-25s | %-20s | %-20s | %s | %-10s | %d",
                id,
                title,
                artist,
                album,
                getFormattedDuration(),
                genre,
                year
        );
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Song song)) {
            return false;
        }
        return id == song.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}