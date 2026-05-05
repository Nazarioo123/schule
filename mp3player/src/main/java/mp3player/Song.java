package mp3player;

/**
 * Fachklasse Song – repräsentiert einen einzelnen Song im MP3-Player.
 * Assoziation: wird von Playlist verwaltet.
 */
public class Song {

    // --- Attribute ---
    private int    id;
    private String title;
    private String artist;
    private String album;
    private int    durationSeconds; // Länge in Sekunden
    private String genre;
    private int    year;

    // --- Konstruktoren ---
    public Song(int id, String title, String artist, String album,
                int durationSeconds, String genre, int year) {
        this.id              = id;
        this.title           = title;
        this.artist          = artist;
        this.album           = album;
        this.durationSeconds = durationSeconds;
        this.genre           = genre;
        this.year            = year;
    }

    // --- Getter & Setter ---
    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public String getTitle()           { return title; }
    public void   setTitle(String t)   { this.title = t; }

    public String getArtist()          { return artist; }
    public void   setArtist(String a)  { this.artist = a; }

    public String getAlbum()           { return album; }
    public void   setAlbum(String a)   { this.album = a; }

    public int    getDurationSeconds()        { return durationSeconds; }
    public void   setDurationSeconds(int d)   { this.durationSeconds = d; }

    public String getGenre()           { return genre; }
    public void   setGenre(String g)   { this.genre = g; }

    public int    getYear()            { return year; }
    public void   setYear(int y)       { this.year = y; }

    /**
     * Gibt die Dauer im Format mm:ss zurück.
     * Methode mit Rückgabewert (1).
     */
    public String getFormattedDuration() {
        int min = durationSeconds / 60;
        int sec = durationSeconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    /**
     * Prüft, ob der Song einem Suchbegriff entspricht (Titel oder Künstler).
     * Methode mit Rückgabewert (2) – boolean.
     */
    public boolean matches(String query) {
        if (query == null || query.isBlank()) return false;
        String q = query.toLowerCase();
        return title.toLowerCase().contains(q)
            || artist.toLowerCase().contains(q)
            || album.toLowerCase().contains(q);
    }

    /** CSV-Zeile: id;title;artist;album;durationSeconds;genre;year */
    public String toCsv() {
        return id + ";" + escapeCsv(title) + ";" + escapeCsv(artist) + ";"
             + escapeCsv(album) + ";" + durationSeconds + ";"
             + escapeCsv(genre) + ";" + year;
    }

    /** Erstellt Song-Objekt aus einer CSV-Zeile. */
    public static Song fromCsv(String line) {
        String[] p = line.split(";", -1);
        if (p.length < 7) throw new IllegalArgumentException("Ungültige CSV-Zeile: " + line);
        return new Song(
            Integer.parseInt(p[0].trim()),
            p[1], p[2], p[3],
            Integer.parseInt(p[4].trim()),
            p[5],
            Integer.parseInt(p[6].trim())
        );
    }

    private String escapeCsv(String s) {
        return s == null ? "" : s.replace(";", ",");
    }

    @Override
    public String toString() {
        return String.format("[%3d] %-30s | %-20s | %-20s | %s | %-10s | %d",
            id, title, artist, album, getFormattedDuration(), genre, year);
    }
}
