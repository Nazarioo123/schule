package mp3player;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MP3PlayerGUI extends JFrame {

    private final Color PRIMARY_COLOR = new Color(48, 204, 24);
    private final Color ACCENT_COLOR = new Color(76, 170, 53);
    private final Color BG_COLOR = new Color(248, 245, 252);
    private final Color TEXT_COLOR = new Color(33, 33, 33);
    private final Color DELETE_COLOR = new Color(231, 76, 60);

    private static final String SONG_FILE = "songs.csv";
    private static final String PLAYLIST_FILE = "playlist.csv";

    private CsvStorage storage;
    private Map<String, Playlist> playlists = new LinkedHashMap<>();
    private List<Song> allSongs = new ArrayList<>();
    private Playlist currentPlaylist;

    private JComboBox<String> playlistSelector;
    private JTable songTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;

    public MP3PlayerGUI() {
        storage = new CsvStorage(SONG_FILE, PLAYLIST_FILE);
        loadData();

        if (playlists.isEmpty()) {
            Playlist defaultPlaylist = new Playlist("Soundtrack des Lebens");
            playlists.put(defaultPlaylist.getName(), defaultPlaylist);
            addSampleData(defaultPlaylist);
        }

        setupMainFrame();
    }

    private void setupMainFrame() {
        setTitle("MP3 Library Manager v1.2 [Amethyst Edition]");
        setSize(1024, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(15, 15));

        add(createHeaderPanel(), BorderLayout.NORTH);

        add(createTablePanel(), BorderLayout.CENTER);

        add(createFooterPanel(), BorderLayout.SOUTH);

        updateTable();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                storage.saveSongs(allSongs);
                storage.savePlaylists(playlists);
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(0, 90));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("MUSIC STATION");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        controls.setOpaque(false);

        playlistSelector = new JComboBox<>(playlists.keySet().toArray(new String[0]));
        playlistSelector.setPreferredSize(new Dimension(200, 40));
        playlistSelector.addActionListener(e -> updateTable());

        JButton addPlaylistBtn = createStyledButton("+ Neue Playlist", ACCENT_COLOR);
        addPlaylistBtn.setPreferredSize(new Dimension(160, 40));
        addPlaylistBtn.addActionListener(e -> createPlaylist());

        controls.add(new JLabel("<html><font color='white' size='4'>Playlist: </font></html>"));
        controls.add(playlistSelector);
        controls.add(addPlaylistBtn);

        panel.add(controls, BorderLayout.EAST);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 30, 0, 30));

        String[] columns = {"ID", "Titel", "Interpret", "Album", "Dauer", "Genre", "Jahr"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        songTable = new JTable(tableModel);
        songTable.setRowHeight(40); // Ещё больше "воздуха"
        songTable.setSelectionBackground(ACCENT_COLOR);
        songTable.setSelectionForeground(Color.WHITE);
        songTable.setGridColor(new Color(230, 230, 235));
        songTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JTableHeader header = songTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(Color.WHITE);
        header.setForeground(TEXT_COLOR);
        header.setPreferredSize(new Dimension(0, 45));

        JScrollPane scrollPane = new JScrollPane(songTable);
        scrollPane.setBorder(new LineBorder(new Color(210, 210, 215), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel mainFooter = new JPanel(new GridLayout(2, 1, 15, 15));
        mainFooter.setOpaque(false);
        mainFooter.setBorder(new EmptyBorder(10, 30, 30, 30));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = createStyledButton("Song Hinzufügen", PRIMARY_COLOR);
        JButton delBtn = createStyledButton("Song Löschen", DELETE_COLOR);
        JButton searchBtn = createStyledButton("Suchen", Color.GRAY); // Нейтральная кнопка

        addBtn.addActionListener(e -> addSong());
        delBtn.addActionListener(e -> deleteSong());
        searchBtn.addActionListener(e -> searchSong());

        btnPanel.add(addBtn);
        btnPanel.add(delBtn);
        btnPanel.add(searchBtn);

        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 225), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        statsLabel = new JLabel("Lade Statistik...");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statsLabel.setForeground(TEXT_COLOR);
        statsPanel.add(statsLabel, BorderLayout.CENTER);

        mainFooter.add(btnPanel);
        mainFooter.add(statsPanel);

        return mainFooter;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });

        return btn;
    }


    private void updateTable() {
        tableModel.setRowCount(0);
        String selected = (String) playlistSelector.getSelectedItem();
        if (selected != null) {
            currentPlaylist = playlists.get(selected);
            for (Song song : currentPlaylist.getSongs()) {
                tableModel.addRow(new Object[]{
                        song.getId(), song.getTitle(), song.getArtist(),
                        song.getAlbum(), song.getFormattedDuration(),
                        song.getGenre(), song.getYear()
                });
            }
            updateStatistics();
        }
    }

    private void updateStatistics() {
        if (currentPlaylist == null || currentPlaylist.isEmpty()) {
            statsLabel.setText("Keine Songs in dieser Playlist.");
            return;
        }
        String text = String.format(
                "<html><font color='#4A235A'><b>Vorrätig:</b></font> %d Songs | <font color='#4A235A'><b>Gesamtzeit:</b></font> %s | <font color='#4A235A'><b>Ø-Dauer:</b></font> %.1f Sek. | <font color='#4A235A'><b>Längster Song:</b></font> %s</html>",
                currentPlaylist.size(),
                currentPlaylist.getTotalDurationFormatted(),
                currentPlaylist.getAverageDurationSeconds(),
                currentPlaylist.getLongestSong().getTitle()
        );
        statsLabel.setText(text);
    }

    private void createPlaylist() {
        String name = JOptionPane.showInputDialog(this, "Name der neuen Playlist:");
        if (name != null && !name.trim().isEmpty()) {
            if (playlists.containsKey(name.trim())) return;
            Playlist p = new Playlist(name.trim());
            playlists.put(p.getName(), p);
            playlistSelector.addItem(p.getName());
            playlistSelector.setSelectedItem(p.getName());
        }
    }

    private void addSong() {
        JTextField t = new JTextField(); JTextField a = new JTextField();
        JTextField d = new JTextField("180");
        Object[] msg = {"Titel:", t, "Interpret:", a, "Dauer (Sek):", d};

        if (JOptionPane.showConfirmDialog(this, msg, "Neuer Song", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int id = allSongs.stream().mapToInt(Song::getId).max().orElse(0) + 1;
                Song s = new Song(id, t.getText(), a.getText(), "Album", Integer.parseInt(d.getText()), "Genre", 2024);
                allSongs.add(s);
                currentPlaylist.addSong(s);
                updateTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eingabefehler!");
            }
        }
    }

    private void deleteSong() {
        int row = songTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);
            currentPlaylist.removeSongById(id);
            updateTable();
        }
    }

    private void searchSong() {
        String q = JOptionPane.showInputDialog(this, "Suche (Interpret/Titel):");
        if (q != null && !q.isEmpty()) {
            List<Song> res = currentPlaylist.search(q);
            tableModel.setRowCount(0);
            for (Song s : res) {
                tableModel.addRow(new Object[]{s.getId(), s.getTitle(), s.getArtist(), s.getAlbum(), s.getFormattedDuration(), s.getGenre(), s.getYear()});
            }
        }
    }

    private void loadData() {
        allSongs.addAll(storage.loadSongs());
        Map<Integer, Song> songById = new HashMap<>();
        for (Song s : allSongs) songById.put(s.getId(), s);

        Map<String, List<Integer>> loaded = storage.loadPlaylists();
        for (var entry : loaded.entrySet()) {
            Playlist p = new Playlist(entry.getKey());
            for (Integer id : entry.getValue()) {
                if (songById.containsKey(id)) p.addSong(songById.get(id));
            }
            playlists.put(p.getName(), p);
        }
    }

    private void addSampleData(Playlist p) {
        Song s1 = new Song(1, "Imagine", "John Lennon", "Imagine", 183, "Rock", 1971);
        allSongs.add(s1);
        p.addSong(s1);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MP3PlayerGUI().setVisible(true));
    }
}