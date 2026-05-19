package mp3player;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MP3PlayerGUI extends JFrame {

    private final Color PRIMARY_COLOR = new Color(174, 102, 31);
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Color DELETE_COLOR = new Color(231, 76, 60);
    private final Color EDIT_COLOR = new Color(241, 196, 15);

    private static final String SONG_FILE = "songs.csv";
    private static final String PLAYLIST_FILE = "playlist.csv";

    private CsvStorage storage;
    private Map<String, Playlist> playlists = new LinkedHashMap<>();
    private List<Song> allSongs = new ArrayList<>();
    private Playlist currentPlaylist;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private DefaultListModel<String> playlistListModel;
    private JList<String> playlistJList;
    private JLabel totalSongsLabel;
    private JLabel totalPlaylistsLabel;

    private JLabel playlistTitleLabel;
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
        setTitle("MP3 Library Manager v1.6");
        setSize(1024, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createDashboardPanel(), "DASHBOARD");
        mainPanel.add(createPlaylistViewPanel(), "PLAYLIST_VIEW");

        add(mainPanel);
        cardLayout.show(mainPanel, "DASHBOARD");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                storage.saveSongs(allSongs);
                storage.savePlaylists(playlists);
            }
        });
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(30, 40, 40, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Musik Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(PRIMARY_COLOR);
        header.add(title, BorderLayout.WEST);
        panel.add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        centerPanel.setOpaque(false);

        JPanel statsContainer = new JPanel(new GridLayout(2, 1, 0, 20));
        statsContainer.setOpaque(false);

        JPanel songStatCard = createStatCard("Gesamtanzahl Songs", "0", PRIMARY_COLOR);
        totalSongsLabel = (JLabel) songStatCard.getClientProperty("valueLabel");

        JPanel playlistStatCard = createStatCard("Verfügbare Playlists", "0", ACCENT_COLOR);
        totalPlaylistsLabel = (JLabel) playlistStatCard.getClientProperty("valueLabel");

        statsContainer.add(songStatCard);
        statsContainer.add(playlistStatCard);
        centerPanel.add(statsContainer);

        JPanel playlistChoicePanel = new JPanel(new BorderLayout(10, 10));
        playlistChoicePanel.setBackground(Color.WHITE);
        playlistChoicePanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 225), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel listTitle = new JLabel("Deine Playlists:");
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        playlistChoicePanel.add(listTitle, BorderLayout.NORTH);

        playlistListModel = new DefaultListModel<>();
        playlistJList = new JList<>(playlistListModel);
        playlistJList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        playlistJList.setSelectionBackground(ACCENT_COLOR);
        playlistJList.setSelectionForeground(Color.WHITE);
        playlistJList.setFixedCellHeight(40);

        playlistJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedPlaylist();
                }
            }
        });

        playlistChoicePanel.add(new JScrollPane(playlistJList), BorderLayout.CENTER);

        JPanel playlistActionPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        playlistActionPanel.setOpaque(false);

        JButton openBtn = createStyledButton("Öffnen", ACCENT_COLOR);
        openBtn.addActionListener(e -> openSelectedPlaylist());

        JButton createBtn = createStyledButton("+ Neu", PRIMARY_COLOR);
        createBtn.addActionListener(e -> createPlaylist());

        JButton renameBtn = createStyledButton("Umbenennen", EDIT_COLOR);
        renameBtn.addActionListener(e -> renamePlaylist());

        playlistActionPanel.add(openBtn);
        playlistActionPanel.add(createBtn);
        playlistActionPanel.add(renameBtn);
        playlistChoicePanel.add(playlistActionPanel, BorderLayout.SOUTH);

        centerPanel.add(playlistChoicePanel);
        panel.add(centerPanel, BorderLayout.CENTER);

        updateDashboardData();
        return panel;
    }

    private JPanel createPlaylistViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_COLOR);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 90));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        playlistTitleLabel = new JLabel("PLAYLIST");
        playlistTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        playlistTitleLabel.setForeground(Color.WHITE);
        headerPanel.add(playlistTitleLabel, BorderLayout.WEST);

        JButton backBtn = createStyledButton("Zurück zum Hub", ACCENT_COLOR);
        backBtn.setPreferredSize(new Dimension(160, 40));
        backBtn.addActionListener(e -> {
            updateDashboardData();
            cardLayout.show(mainPanel, "DASHBOARD");
        });
        headerPanel.add(backBtn, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setOpaque(false);
        tableContainer.setBorder(new EmptyBorder(0, 30, 0, 30));

        String[] columns = {"ID", "Titel", "Interpret", "Album", "Dauer", "Genre", "Jahr"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        songTable = new JTable(tableModel);
        songTable.setRowHeight(40);
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
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableContainer, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 30, 30, 30));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = createStyledButton("Hinzufügen", PRIMARY_COLOR);
        JButton editBtn = createStyledButton("Bearbeiten", EDIT_COLOR);
        JButton delBtn = createStyledButton("Löschen", DELETE_COLOR);
        JButton searchBtn = createStyledButton("Suchen", Color.GRAY);

        addBtn.addActionListener(e -> addSong());
        editBtn.addActionListener(e -> editSong());
        delBtn.addActionListener(e -> deleteSong());
        searchBtn.addActionListener(e -> searchSong());

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
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

        footerPanel.add(btnPanel);
        footerPanel.add(statsPanel);
        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatCard(String title, String initialValue, Color topBorderColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new MatteBorder(4, 0, 0, 0, topBorderColor),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(initialValue);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(TEXT_COLOR);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.putClientProperty("valueLabel", valueLabel);

        return card;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(150, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(color); }
        });

        return btn;
    }

    private void updateDashboardData() {
        playlistListModel.clear();
        for (String name : playlists.keySet()) {
            playlistListModel.addElement(name);
        }
        totalSongsLabel.setText(String.valueOf(allSongs.size()));
        totalPlaylistsLabel.setText(String.valueOf(playlists.size()));
    }

    private void openSelectedPlaylist() {
        String selectedName = playlistJList.getSelectedValue();
        if (selectedName == null) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie zuerst eine Playlist aus.");
            return;
        }
        currentPlaylist = playlists.get(selectedName);
        playlistTitleLabel.setText(currentPlaylist.getName().toUpperCase());
        updateTable();
        cardLayout.show(mainPanel, "PLAYLIST_VIEW");
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        if (currentPlaylist != null) {
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
                "<html><font color='#2980B9'><b>Vorrätig:</b></font> %d Songs | <font color='#2980B9'><b>Gesamtzeit:</b></font> %s | <font color='#2980B9'><b>Ø-Dauer:</b></font> %.1f Sek. | <font color='#2980B9'><b>Längster Song:</b></font> %s</html>",
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
            if (playlists.containsKey(name.trim())) {
                JOptionPane.showMessageDialog(this, "Diese Playlist existiert bereits.");
                return;
            }
            Playlist p = new Playlist(name.trim());
            playlists.put(p.getName(), p);
            updateDashboardData();
            playlistJList.setSelectedValue(p.getName(), true);
        }
    }
    private void renamePlaylist() {
        String selectedName = playlistJList.getSelectedValue();
        if (selectedName == null) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie zuerst eine Playlist aus.");
            return;
        }

        String newName = JOptionPane.showInputDialog(this, "Neuer Name für die Playlist:", selectedName);
        if (newName != null && !newName.trim().isEmpty()) {
            newName = newName.trim();
            if (playlists.containsKey(newName)) {
                JOptionPane.showMessageDialog(this, "Eine Playlist mit diesem Namen existiert bereits.");
                return;
            }

            Playlist p = playlists.remove(selectedName);
            p.setName(newName);
            playlists.put(newName, p);

            updateDashboardData();
            playlistJList.setSelectedValue(newName, true);
        }
    }
    private void addSong() {
        JTextField t = new JTextField();
        JTextField a = new JTextField();
        JTextField al = new JTextField();
        JTextField d = new JTextField("180");
        JTextField g = new JTextField();
        JTextField y = new JTextField("2026");

        Object[] msg = {
                "Titel:", t,
                "Interpret:", a,
                "Album:", al,
                "Dauer (Sek):", d,
                "Genre:", g,
                "Jahr:", y
        };

        if (JOptionPane.showConfirmDialog(this, msg, "Neuer Song", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int id = allSongs.size() + 1;
                Song s = new Song(id, t.getText(), a.getText(), al.getText().isBlank() ? "Album" : al.getText(), Integer.parseInt(d.getText()), g.getText().isBlank() ? "Genre" : g.getText(), Integer.parseInt(y.getText()));
                allSongs.add(s);
                currentPlaylist.addSong(s);
                updateTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eingabefehler!");
            }
        }
    }

    private void editSong() {
        int row = songTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie zuerst einen Song aus der Tabelle aus.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        Song selectedSong = currentPlaylist.findById(id);

        if (selectedSong != null) {
            SongEditDialog dialog = new SongEditDialog(this, selectedSong);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                updateTable();
            }
        }
    }

    private void deleteSong() {
        int row = songTable.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableModel.getValueAt(row, 0);

            currentPlaylist.removeSongById(id);

            allSongs.removeIf(song -> song.getId() == id);

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
        allSongs.clear();
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
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MP3PlayerGUI().setVisible(true));
    }
}

class SongEditDialog extends JDialog {
    private JTextField titleField;
    private JTextField artistField;
    private JTextField albumField;
    private JTextField durationField;
    private JTextField genreField;
    private JTextField yearField;
    private boolean saved = false;

    public SongEditDialog(Frame parent, Song song) {
        super(parent, "Song bearbeiten", true);
        setSize(400, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        titleField = new JTextField(song.getTitle());
        artistField = new JTextField(song.getArtist());
        albumField = new JTextField(song.getAlbum());
        durationField = new JTextField(String.valueOf(song.getDurationSeconds()));
        genreField = new JTextField(song.getGenre());
        yearField = new JTextField(String.valueOf(song.getYear()));

        formPanel.add(new JLabel("Titel:")); formPanel.add(titleField);
        formPanel.add(new JLabel("Interpret:")); formPanel.add(artistField);
        formPanel.add(new JLabel("Album:")); formPanel.add(albumField);
        formPanel.add(new JLabel("Dauer (Sek):")); formPanel.add(durationField);
        formPanel.add(new JLabel("Genre:")); formPanel.add(genreField);
        formPanel.add(new JLabel("Jahr:")); formPanel.add(yearField);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveButton = new JButton("Speichern");
        JButton cancelButton = new JButton("Abbrechen");

        saveButton.addActionListener(e -> {
            try {
                int duration = Integer.parseInt(durationField.getText());
                int year = Integer.parseInt(yearField.getText());

                if (duration < 0 || year < 0) {
                    throw new IllegalArgumentException();
                }

                song.setTitle(titleField.getText());
                song.setArtist(artistField.getText());
                song.setAlbum(albumField.getText());
                song.setDurationSeconds(duration);
                song.setGenre(genreField.getText());
                song.setYear(year);

                saved = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Bitte überprüfen Sie Ihre Eingaben. ID/Dauer/Jahr müssen positive Zahlen sein.");
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isSaved() {
        return saved;
    }
}