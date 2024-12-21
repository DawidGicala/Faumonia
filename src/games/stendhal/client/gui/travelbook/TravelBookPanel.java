package games.stendhal.client.gui.travelbook;

import games.stendhal.client.stendhal;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.RPEntity;


public class TravelBookPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final String FILE_NAME = "mob_kills.properties"; // Nazwa pliku do zapisu
    private final JPanel mobListPanel; // Panel dla listy mobów
    private final Map<String, JPanel> mobPanels; // Mapowanie nazwa -> panel z mobkiem

 // Dodanie przycisku odświeżania listy
    private JButton refreshButton;

    public TravelBookPanel() {
        // Ustawienie głównego układu
        setLayout(new BorderLayout());
        mobPanels = new HashMap<>();

        // --- Nagłówek panelu ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Dzisiaj pokonałeś:");
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 16));

        // Przycisk do czyszczenia listy
        JButton clearButton = new JButton("X");
        clearButton.setForeground(Color.RED);
        clearButton.setFont(new Font("Arial", Font.BOLD, 16));
        clearButton.addActionListener(e -> clearMobList());

        JPanel clearButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        clearButtonPanel.add(clearButton);

        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.add(clearButtonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // --- Panel z przyciskiem "Odśwież listę" ---
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refreshButton = new JButton("Odśwież listę");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshButton.addActionListener(e -> refreshMobList());
        refreshPanel.add(refreshButton);

        add(refreshPanel, BorderLayout.SOUTH); // Umieszczenie przycisku na dole panelu

        // --- Panel listy mobów ---
        mobListPanel = new JPanel();
        mobListPanel.setLayout(new BoxLayout(mobListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(mobListPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Metoda do odświeżania listy ręcznie przez użytkownika
    private void refreshMobList() {
        String playerName = User.getCharacterName();
        if (playerName == null || playerName.isEmpty()) {
            playerName = "default_player";
            System.out.println("[ERROR] Nie można odświeżyć listy – nazwa gracza jest niedostępna.");
        }

        if (RPEntity.getMobKillCount() != null) {
            RPEntity.getMobKillCount().clear(); // Wyczyść globalną listę przed wczytaniem danych
            loadMobList(playerName, RPEntity.getMobKillCount());
            System.out.println("[INFO] Lista zabójstw została ręcznie odświeżona przez gracza: " + playerName);
        } else {
            System.out.println("[ERROR] Nie można odświeżyć listy – mobKillCount jest null.");
        }
    }


    // Metoda do aktualizacji liczby zabitych mobków
    public void updateMobCount(String mobName, int count) {
        if (!mobPanels.containsKey(mobName)) {
            JPanel mobPanel = createMobPanel(mobName);
            mobPanels.put(mobName, mobPanel);
            mobListPanel.add(mobPanel);
        }

        // Aktualizacja etykiety w istniejącym panelu
        JPanel mobPanel = mobPanels.get(mobName);
        JLabel mobLabel = (JLabel) mobPanel.getComponent(0);
        mobLabel.setText(count + " x " + mobName);

        // Odświeżenie panelu
        mobListPanel.revalidate();
        mobListPanel.repaint();
    }

    // Metoda do tworzenia panelu dla nowego mobka
    private JPanel createMobPanel(String mobName) {
        JPanel mobPanel = new JPanel(new BorderLayout());
        mobPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel mobLabel = new JLabel();
        mobLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        mobPanel.add(mobLabel, BorderLayout.CENTER);

        JButton removeButton = new JButton("X");
        removeButton.setForeground(Color.RED);
        removeButton.setFont(new Font("Arial", Font.BOLD, 14));
        removeButton.addActionListener(e -> removeMob(mobName));
        mobPanel.add(removeButton, BorderLayout.EAST);

        return mobPanel;
    }

    // Metoda do usuwania konkretnego mobka z listy
    private void removeMob(String mobName) {
        JPanel mobPanel = mobPanels.remove(mobName);
        if (mobPanel != null) {
            mobListPanel.remove(mobPanel);
            mobListPanel.revalidate();
            mobListPanel.repaint();
        }
    }

    // Metoda do czyszczenia całej listy
    private void clearMobList() {
        mobPanels.clear();
        mobListPanel.removeAll();
        mobListPanel.revalidate();
        mobListPanel.repaint();
    }

    public void saveMobList(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            playerName = "default_player";
        }

        Properties properties = new Properties();
        String filePath = getSaveFilePath(playerName);

        // Wczytaj istniejące dane, jeśli plik istnieje
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
            System.out.println("[INFO] Wczytano istniejące dane z pliku: " + filePath);
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] Brak istniejącego pliku, zostanie utworzony nowy: " + filePath);
        } catch (IOException e) {
            System.out.println("[ERROR] Wystąpił błąd podczas wczytywania istniejącego pliku: " + filePath);
            e.printStackTrace();
        }

        // Nadpisz wartość bez sumowania
        for (Map.Entry<String, JPanel> entry : mobPanels.entrySet()) {
            JLabel mobLabel = (JLabel) entry.getValue().getComponent(0);
            String mobText = mobLabel.getText();
            int count = Integer.parseInt(mobText.split(" x ")[0]);

            // Zapisz aktualną wartość z GUI
            properties.setProperty(entry.getKey(), String.valueOf(count));
        }

        // Zapisz dane z powrotem do pliku
        try (OutputStream output = new FileOutputStream(filePath)) {
            properties.store(output, "Mob Kill List for " + playerName);
            System.out.println("[INFO] Lista zabójstw została zapisana do pliku: " + filePath);
        } catch (IOException e) {
            System.out.println("[ERROR] Nie udało się zapisać pliku: " + filePath);
            e.printStackTrace();
        }
    }


 // Metoda pomocnicza do pobierania ścieżki pliku
    private String getSaveFilePath(String playerName) {
        String folderPath = stendhal.getGameFolder() + "travelbook/";
        File folder = new File(folderPath);

        // Sprawdzenie istnienia folderu
        if (!folder.exists()) {
            System.out.println("[INFO] Folder 'travelbook' nie istnieje. Tworzenie folderu...");
            boolean folderCreated = folder.mkdirs();
            if (folderCreated) {
                System.out.println("[INFO] Folder 'travelbook' został utworzony: " + folder.getAbsolutePath());
            } else {
                System.out.println("[ERROR] Nie udało się utworzyć folderu: " + folder.getAbsolutePath());
            }
        } else {
            System.out.println("[INFO] Folder 'travelbook' istnieje: " + folder.getAbsolutePath());
        }

        String filePath = folderPath + playerName + "_mob_kills.properties";
        File file = new File(filePath);

        // Sprawdzenie istnienia pliku
        if (file.exists()) {
            System.out.println("[INFO] Plik istnieje: " + file.getAbsolutePath());
        } else {
            System.out.println("[INFO] Plik nie istnieje i zostanie utworzony: " + file.getAbsolutePath());
        }

        return filePath;
    }

 // Metoda do wczytywania listy mobów dla konkretnego gracza
    public void loadMobList(String playerName, Map<String, Integer> mobKillCount) {
        if (playerName == null || playerName.isEmpty()) {
            System.out.println("[ERROR] Próba wczytania danych z pustą nazwą gracza!");
            playerName = "default_player";
        }

        Properties properties = new Properties();
        String filePath = getSaveFilePath(playerName);

        // Wyczyść GUI przed załadowaniem nowych danych
        mobPanels.clear();
        mobListPanel.removeAll();
        mobListPanel.revalidate();
        mobListPanel.repaint();

        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);

            for (String mobName : properties.stringPropertyNames()) {
                int count = Integer.parseInt(properties.getProperty(mobName));
                mobKillCount.put(mobName, count); // Aktualizacja mapy
                updateMobCount(mobName, count); // Aktualizacja GUI
            }

            System.out.println("[INFO] Lista zabójstw została wczytana z pliku: " + filePath);
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] Plik nie istnieje, brak danych do wczytania: " + filePath);
        } catch (IOException e) {
            System.out.println("[ERROR] Wystąpił błąd podczas wczytywania pliku: " + filePath);
            e.printStackTrace();
        }
    }

}
