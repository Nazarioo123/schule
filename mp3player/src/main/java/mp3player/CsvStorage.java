package mp3player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse für persistente Datenspeicherung in einer CSV-Datei.
 * Assoziation: arbeitet mit Song-Objekten und Playlist.
 */
public class CsvStorage {

    private final String filePath;
    private static final String HEADER = "id;title;artist;album;durationSeconds;genre;year";

    public CsvStorage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Lädt alle Songs aus der CSV-Datei. Gibt ArrayList zurück (Rückgabewert).
     */
    public ArrayList<Song> loadSongs() {
        ArrayList<Song> list = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) return list;  // Verzweigung: Datei noch nicht vorhanden

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {  // Schleife
                if (firstLine) { firstLine = false; continue; } // Header überspringen
                line = line.trim();
                if (!line.isEmpty()) {
                    try {
                        list.add(Song.fromCsv(line));
                    } catch (Exception e) {
                        System.err.println("Zeile übersprungen (Fehler): " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen: " + e.getMessage());
        }
        return list;
    }

    /**
     * Speichert alle Songs der Playlist in die CSV-Datei.
     */
    public boolean saveSongs(Playlist playlist) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println(HEADER);
            for (Song s : playlist.getSongs()) {  // Schleife
                writer.println(s.toCsv());
            }
            return true;
        } catch (IOException e) {
            System.err.println("Fehler beim Schreiben: " + e.getMessage());
            return false;
        }
    }

    public String getFilePath() { return filePath; }
}
