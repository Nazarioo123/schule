package mp3player;

import java.util.List;
import java.util.Scanner;

/**
 * Hauptklasse MP3PlayerApp – enthält main()-Methode und Menüführung.
 * Verwaltet eine Playlist und einen CsvStorage.
 * Assoziation: nutzt Playlist, CsvStorage, Song.
 */
public class MP3PlayerApp {

    private static final String CSV_FILE = "songs.csv";
    private static Playlist    playlist;
    private static CsvStorage  storage;
    private static Scanner     scanner;

    public static void main(String[] args) {
        storage  = new CsvStorage(CSV_FILE);
        playlist = new Playlist("Meine Bibliothek");
        scanner  = new Scanner(System.in);

        // Songs laden
        List<Song> loaded = storage.loadSongs();
        for (Song s : loaded) playlist.addSong(s);

        if (playlist.size() == 0) {
            addSampleData();
            storage.saveSongs(playlist);
        }

        printBanner();

        boolean running = true;
        while (running) {           // Hauptschleife
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {       // Verzweigung
                case "1" -> showAllSongs();
                case "2" -> searchSong();
                case "3" -> addSong();
                case "4" -> editSong();
                case "5" -> deleteSong();
                case "6" -> showStatistics();
                case "0" -> running = false;
                default  -> System.out.println("  Ungültige Eingabe. Bitte 0-6 eingeben.");
            }
        }

        storage.saveSongs(playlist);
        System.out.println("\n  Auf Wiedersehen! Songs gespeichert in: " + CSV_FILE);
        scanner.close();
    }

    // ---------------------------------------------------------------
    // MENÜ
    // ---------------------------------------------------------------
    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║        🎵  KONSOLEN MP3-PLAYER  🎵        ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  Playlist: " + playlist.getName()
                         + "  |  Songs geladen: " + playlist.size());
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("\n──────────────── MENÜ ────────────────");
        System.out.println("  1  Alle Songs anzeigen");
        System.out.println("  2  Song suchen");
        System.out.println("  3  Song hinzufügen");
        System.out.println("  4  Song bearbeiten");
        System.out.println("  5  Song löschen");
        System.out.println("  6  Statistiken");
        System.out.println("  0  Beenden");
        System.out.println("──────────────────────────────────────");
        System.out.print("Auswahl: ");
    }

    // ---------------------------------------------------------------
    // 1 – ALLE SONGS ANZEIGEN
    // ---------------------------------------------------------------
    private static void showAllSongs() {
        System.out.println("\n─── Alle Songs (" + playlist.size() + ") ───");
        if (playlist.size() == 0) {
            System.out.println("  Keine Songs vorhanden.");
            return;
        }
        printTableHeader();
        for (Song s : playlist.getSongs()) {    // Schleife
            System.out.println("  " + s);
        }
        System.out.println("  Gesamtdauer: " + playlist.getTotalDurationFormatted());
    }

    // ---------------------------------------------------------------
    // 2 – SUCHEN
    // ---------------------------------------------------------------
    private static void searchSong() {
        System.out.print("\nSuchbegriff (Titel/Künstler/Album): ");
        String q = scanner.nextLine().trim();
        List<Song> results = playlist.search(q);

        if (results.isEmpty()) {
            System.out.println("  Keine Songs gefunden für: \"" + q + "\"");
        } else {
            System.out.println("  " + results.size() + " Ergebnis(se) für \"" + q + "\":");
            printTableHeader();
            for (Song s : results) System.out.println("  " + s);
        }
    }

    // ---------------------------------------------------------------
    // 3 – HINZUFÜGEN
    // ---------------------------------------------------------------
    private static void addSong() {
        System.out.println("\n─── Neuen Song hinzufügen ───");
        String title    = readNonEmpty("Titel:    ");
        String artist   = readNonEmpty("Künstler: ");
        String album    = readNonEmpty("Album:    ");
        int    duration = readPositiveInt("Dauer (Sekunden): ");
        String genre    = readNonEmpty("Genre:    ");
        int    year     = readPositiveInt("Jahr:     ");

        int id = playlist.nextId();
        Song newSong = new Song(id, title, artist, album, duration, genre, year);
        playlist.addSong(newSong);
        storage.saveSongs(playlist);

        System.out.println("  ✔ Song hinzugefügt: " + newSong);
    }

    // ---------------------------------------------------------------
    // 4 – BEARBEITEN
    // ---------------------------------------------------------------
    private static void editSong() {
        System.out.print("\nID des zu bearbeitenden Songs: ");
        int id = readIntSafe();
        Song song = playlist.findById(id);

        if (song == null) {                     // Verzweigung
            System.out.println("  Song mit ID " + id + " nicht gefunden.");
            return;
        }
        System.out.println("  Aktuell: " + song);
        System.out.println("  (Leere Eingabe = unverändert)");

        String title  = readOptional("Neuer Titel [" + song.getTitle() + "]: ");
        String artist = readOptional("Neuer Künstler [" + song.getArtist() + "]: ");
        String album  = readOptional("Neues Album [" + song.getAlbum() + "]: ");
        String genre  = readOptional("Neues Genre [" + song.getGenre() + "]: ");

        System.out.print("Neue Dauer in Sekunden [" + song.getDurationSeconds() + "]: ");
        String durStr = scanner.nextLine().trim();
        System.out.print("Neues Jahr [" + song.getYear() + "]: ");
        String yearStr = scanner.nextLine().trim();

        if (!title.isEmpty())  song.setTitle(title);
        if (!artist.isEmpty()) song.setArtist(artist);
        if (!album.isEmpty())  song.setAlbum(album);
        if (!genre.isEmpty())  song.setGenre(genre);
        if (!durStr.isEmpty()) {
            try { song.setDurationSeconds(Integer.parseInt(durStr)); }
            catch (NumberFormatException e) { System.out.println("  Ungültige Dauer – unverändert."); }
        }
        if (!yearStr.isEmpty()) {
            try { song.setYear(Integer.parseInt(yearStr)); }
            catch (NumberFormatException e) { System.out.println("  Ungültiges Jahr – unverändert."); }
        }

        storage.saveSongs(playlist);
        System.out.println("  ✔ Song aktualisiert: " + song);
    }

    // ---------------------------------------------------------------
    // 5 – LÖSCHEN
    // ---------------------------------------------------------------
    private static void deleteSong() {
        System.out.print("\nID des zu löschenden Songs: ");
        int id = readIntSafe();
        Song song = playlist.findById(id);

        if (song == null) {
            System.out.println("  Song mit ID " + id + " nicht gefunden.");
            return;
        }
        System.out.print("  Wirklich löschen? \"" + song.getTitle() + "\" (j/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("j")) {              // Verzweigung
            playlist.removeSongById(id);
            storage.saveSongs(playlist);
            System.out.println("  ✔ Song gelöscht.");
        } else {
            System.out.println("  Abgebrochen.");
        }
    }

    // ---------------------------------------------------------------
    // 6 – STATISTIKEN
    // ---------------------------------------------------------------
    private static void showStatistics() {
        System.out.println("\n─── Statistiken ───");
        System.out.println("  Anzahl Songs:          " + playlist.size());
        System.out.println("  Gesamtdauer:           " + playlist.getTotalDurationFormatted());
        System.out.printf ("  Ø Dauer pro Song:      %.1f Sekunden%n",
                            playlist.getAverageDurationSeconds());

        Song shortest = playlist.getShortestSong();
        Song longest  = playlist.getLongestSong();

        if (shortest != null) {
            System.out.println("  Kürzester Song:        "
                + shortest.getTitle() + " (" + shortest.getFormattedDuration() + ")");
        }
        if (longest != null) {
            System.out.println("  Längster Song:         "
                + longest.getTitle() + " (" + longest.getFormattedDuration() + ")");
        }
        System.out.println("  Datei:                 " + storage.getFilePath());
    }

    // ---------------------------------------------------------------
    // HILFSMETHODEN
    // ---------------------------------------------------------------
    private static void printTableHeader() {
        System.out.println("  " + String.format(
            "[%3s] %-30s | %-20s | %-20s | %s  | %-10s | %s",
            "ID", "Titel", "Künstler", "Album", "Zeit", "Genre", "Jahr"));
        System.out.println("  " + "─".repeat(105));
    }

    private static String readNonEmpty(String prompt) {
        String val = "";
        while (val.isEmpty()) {             // Schleife mit Validierung
            System.out.print(prompt);
            val = scanner.nextLine().trim();
            if (val.isEmpty()) System.out.println("  Darf nicht leer sein!");
        }
        return val;
    }

    private static String readOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readPositiveInt(String prompt) {
        while (true) {                      // Schleife mit Fehlerbehandlung
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(scanner.nextLine().trim());
                if (v > 0) return v;
                System.out.println("  Muss eine positive Zahl sein!");
            } catch (NumberFormatException e) {
                System.out.println("  Bitte eine ganze Zahl eingeben!");
            }
        }
    }

    private static int readIntSafe() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    /** Beispieldaten für den ersten Start. */
    private static void addSampleData() {
        playlist.addSong(new Song(1, "Bohemian Rhapsody",    "Queen",          "A Night at the Opera", 354, "Rock",     1975));
        playlist.addSong(new Song(2, "Blinding Lights",      "The Weeknd",     "After Hours",          200, "Pop",      2019));
        playlist.addSong(new Song(3, "Smells Like Teen Spirit","Nirvana",       "Nevermind",            301, "Grunge",   1991));
        playlist.addSong(new Song(4, "Hotel California",     "Eagles",         "Hotel California",     391, "Rock",     1977));
        playlist.addSong(new Song(5, "Shape of You",         "Ed Sheeran",     "Divide",               233, "Pop",      2017));
        System.out.println("  Beispieldaten geladen.");
    }
}
