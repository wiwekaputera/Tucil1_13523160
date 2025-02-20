import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class PuzzleSolverGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("IQ Puzzler Pro Solver");
        frame.setSize(720, 480);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridBagLayout());
        frame.add(panel);
        placeComponents(panel);
        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel fileLabel = new JLabel("File Path:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(fileLabel, gbc);

        JTextField filePathField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(filePathField, gbc);

        JButton solveButton = new JButton("Solve Puzzle");
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(solveButton, gbc);

        JTextArea resultArea = new JTextArea(14, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        panel.add(scrollPane, gbc);

        solveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultArea.setText("");

                String filePath = filePathField.getText().trim();
                if (filePath.isEmpty()) {
                    resultArea.append("Masukkan file path yang valid.\n");
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

                try (PrintWriter writer = new PrintWriter(new File("result.txt"))) {
                    long startTime = System.currentTimeMillis();

                    List<char[][]> path = new ArrayList<>();
                    boolean solved = Main.solvePuzzle(puzzleData.getBlocks(), 0, path, writer);
                    long endTime = System.currentTimeMillis();
                    if (solved) {
                        resultArea.append("Solusi ditemukan:\n");
                        for (char[] row : Main.board) {
                            resultArea.append(new String(row) + "\n");
                        }
                    } else {
                        resultArea.append("Tidak ada solusi yang ditemukan.\n");
                    }
                    resultArea.append("\nWaktu pencarian: " + (endTime - startTime) + " ms\n");
                    resultArea.append("Jumlah kasus yang ditinjau: " + Main.kasusCount + "\n");

                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                    resultArea.append("Error: Unable to write to result.txt\n");
                }
            }
        });
    }
}
