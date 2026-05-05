package mp3player;

import java.util.ArrayList;
import java.util.List;

/**
 * Fachklasse Playlist – enthält eine geordnete Liste von Songs.
 * Assoziation: Playlist hat eine ArrayList von Song-Objekten (1 zu *).
 */
public class Playlist {

    private String        name;
    private ArrayList<Song> songs;   // ArrayList als geforderter Container

    public Playlist(String name) {
        this.name  = name;
        this.songs = new ArrayList<>();
    }

    public String getName()               { return name; }
    public void   setName(String name)    { this.name = name; }

    public ArrayList<Song> getSongs()     { return songs; }

    /** Fügt einen Song hinzu. */
    public void addSong(Song s) {
        songs.add(s);
    }

    /** Entfernt Song anhand der ID. Gibt true zurück wenn erfolgreich. */
    public boolean removeSongById(int id) {
        return songs.removeIf(s -> s.getId() == id);
    }

    /** Sucht Song anhand der ID. Methode mit Rückgabewert (3). */
    public Song findById(int id) {
        for (Song s : songs) {          // Schleife
            if (s.getId() == id) return s;
        }
        return null;
    }

    /** Sucht Songs anhand eines Suchbegriffs. Methode mit Rückgabewert (4). */
    public List<Song> search(String query) {
        List<Song> result = new ArrayList<>();
        for (Song s : songs) {          // Schleife + Verzweigung
            if (s.matches(query)) result.add(s);
        }
        return result;
    }

    // --- Statistische Methoden mit Rückgabewert ---

    /** Gesamtdauer der Playlist in Sekunden. */
    public int getTotalDurationSeconds() {
        int total = 0;
        for (Song s : songs) total += s.getDurationSeconds();
        return total;
    }

    /** Gesamtdauer formatiert. */
    public String getTotalDurationFormatted() {
        int total = getTotalDurationSeconds();
        int h   = total / 3600;
        int min = (total % 3600) / 60;
        int sec = total % 60;
        return String.format("%02d:%02d:%02d", h, min, sec);
    }

    /** Durchschnittliche Song-Dauer in Sekunden. */
    public double getAverageDurationSeconds() {
        if (songs.isEmpty()) return 0.0;
        return (double) getTotalDurationSeconds() / songs.size();
    }

    /** Kürzester Song. */
    public Song getShortestSong() {
        if (songs.isEmpty()) return null;
        Song min = songs.get(0);
        for (Song s : songs) {
            if (s.getDurationSeconds() < min.getDurationSeconds()) min = s;
        }
        return min;
    }

    /** Längster Song. */
    public Song getLongestSong() {
        if (songs.isEmpty()) return null;
        Song max = songs.get(0);
        for (Song s : songs) {
            if (s.getDurationSeconds() > max.getDurationSeconds()) max = s;
        }
        return max;
    }

    /** Nächste freie ID (max + 1). */
    public int nextId() {
        int max = 0;
        for (Song s : songs) {
            if (s.getId() > max) max = s.getId();
        }
        return max + 1;
    }

    /** Anzahl Songs. */
    public int size() { return songs.size(); }
}
