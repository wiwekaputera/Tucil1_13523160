import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class PuzzleSolverGUI {
    private static BoardPanel boardPanel;
    private static JLabel solveStatusLabel, timeLabel, countLabel;
    private static JButton saveButton;
    private static String finalSolution = "";

    public static void main(String[] args) {
        Main.board = new char[0][0];

        JFrame frame = new JFrame("IQ Puzzler Pro Solver");
        frame.setSize(920, 540);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        frame.add(panel);

        boardPanel = new BoardPanel(Main.board);

        placeComponents(panel);

        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;

        // Row 0: File Path panel
        JPanel filePathPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel fileLabel = new JLabel("File Path:");
        JTextField filePathField = new JTextField(20);
        JButton browseButton = new JButton("Browse");

        browseButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            // Optional: restrict to .txt files
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
            int choice = fc.showOpenDialog(panel);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                filePathField.setText(file.getAbsolutePath());
            }
        });

        filePathPanel.add(fileLabel);
        filePathPanel.add(filePathField);
        filePathPanel.add(browseButton);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(filePathPanel, gbc);

        // Row 1: Solve Puzzle Button
        JButton solveButton = new JButton("Solve Puzzle");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(solveButton, gbc);

        // Row 2: Board Panel
        JPanel boardContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        boardContainer.add(boardPanel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(boardContainer, gbc);

        // Row 3: Information labels
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        solveStatusLabel = new JLabel("", SwingConstants.CENTER);
        timeLabel = new JLabel("", SwingConstants.CENTER);
        countLabel = new JLabel("", SwingConstants.CENTER);
        infoPanel.add(solveStatusLabel);
        infoPanel.add(timeLabel);
        infoPanel.add(countLabel);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(infoPanel, gbc);

        // Row 4: Save Results Button
        saveButton = new JButton("Simpan Hasil");
        saveButton.setVisible(false);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(saveButton, gbc);

        // Save button action
        saveButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Simpan Hasil");
            int choice = fc.showSaveDialog(panel);
            if (choice == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }
                try (PrintWriter pw = new PrintWriter(file)) {
                    pw.print(finalSolution);
                    JOptionPane.showMessageDialog(panel, "Hasil disimpan ke " + file.getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Solve button action
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                solveStatusLabel.setText("");
                timeLabel.setText("");
                countLabel.setText("");
                saveButton.setVisible(false);
                finalSolution = "";

                String filePath = filePathField.getText().trim();
                if (filePath.isEmpty() || !filePath.toLowerCase().endsWith(".txt")) {
                    solveStatusLabel.setText("Masukkan file path yang valid.");
                    return;
                }
            
                File f = new File(filePath);
                if (!f.exists() || f.isDirectory()) {
                    solveStatusLabel.setText("Masukkan file path yang valid.");
                    return;
                }

                PuzzleData puzzleData = Main.readPuzzleData(filePath);
                Main.board = new char[puzzleData.getN()][puzzleData.getM()];
                for (int i = 0; i < puzzleData.getN(); i++) {
                    for (int j = 0; j < puzzleData.getM(); j++) {
                        Main.board[i][j] = '.';
                    }
                }
                Main.kasusCount = 0;

                boardPanel.board = Main.board;
                int cellSize = 40;
                int cellGap = 2;
                int panelWidth = puzzleData.getM() * (cellSize + cellGap);
                int panelHeight = puzzleData.getN() * (cellSize + cellGap);
                boardPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
                boardPanel.revalidate();
                boardPanel.repaint();

                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    long startTime = System.currentTimeMillis();
                    Timer timer;

                    @Override
                    protected Boolean doInBackground() throws Exception {
                        timer = new Timer(300, new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                countLabel.setText("Jumlah kasus yang ditinjau: " + Main.kasusCount);
                            }
                        });
                        timer.start();
                        boolean solved = Main.solvePuzzle(puzzleData.getBlocks(), 0, new ArrayList<>(), null);
                        if (solved) {
                            finalSolution = boardToString(Main.board);
                        }
                        return solved;
                    }

                    @Override
                    protected void done() {
                        if (timer != null) {
                            timer.stop();
                        }
                        long endTime = System.currentTimeMillis();
                        try {
                            boolean solved = get();
                            if (solved) {
                                solveStatusLabel.setText("Solusi ditemukan!");
                                saveButton.setVisible(true);
                            } else {
                                solveStatusLabel.setText("Tidak ada solusi yang ditemukan.");
                            }
                            timeLabel.setText("Waktu pencarian: " + (endTime - startTime) + " ms");
                            countLabel.setText("Jumlah kasus yang ditinjau: " + Main.kasusCount);
                            boardPanel.repaint();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                worker.execute();
            }
        });
    }

    private static String boardToString(char[][] board) {
        if (board == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char[] row : board) {
            for (char c : row) {
                sb.append(c);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    static class BoardPanel extends JPanel {
        char[][] board;

        public BoardPanel(char[][] board) {
            this.board = board;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (board == null) return;
            Graphics2D g2d = (Graphics2D) g;
            int cellSize = 40;
            int arcSize = 15;
            int cellGap = 2;
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    char cell = board[i][j];
                    Color color = Main.shapeColor.getOrDefault(cell, Color.WHITE);
                    int x = j * (cellSize + cellGap);
                    int y = i * (cellSize + cellGap);
                    g2d.setColor(color);
                    g2d.fillRoundRect(x, y, cellSize, cellSize, arcSize, arcSize);
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(String.valueOf(cell), x + cellSize / 2 - 5, y + cellSize / 2 + 5);
                }
            }
        }
    }
}
