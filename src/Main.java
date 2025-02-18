import java.io.*;
import java.util.*;

class PuzzleData {
    private int N, M, P;
    private String S;
    private List<Block> blocks;

    public PuzzleData(int N, int M, int P, String S, List<Block> blocks) {
        this.N = N;
        this.M = M;
        this.P = P;
        this.S = S;
        this.blocks = blocks;
    }

    public int getN() {
        return N;
    }

    public int getM() {
        return M;
    }

    public int getP() {
        return P;
    }

    public String getS() {
        return S;
    }

    public List<Block> getBlocks() {
        return blocks;
    }
}

class Block {
    private char identifier;
    private char[][] shape;

    public Block(char identifier, char[][] shape) {
        this.identifier = identifier;
        this.shape = shape;
    }

    public char getIdentifier() {
        return identifier;
    }

    public char[][] getShape() {
        return shape;
    }
}

public class Main {
    static char[][] board;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Masukkan path dari input file: ");
        String filePath = scanner.nextLine();

        PuzzleData puzzleData = readPuzzleData(filePath);

        // Debugging Statement
        // testParsing(puzzleData);

        // Init the board
        board = new char[puzzleData.getN()][puzzleData.getM()];
        for (int i = 0; i < puzzleData.getN(); i++) {
            for (int j = 0; j < puzzleData.getM(); j++) {
                board[i][j] = '.';
            }
        }

        // Call backtracking
        // Execution time starts here
        if (solvePuzzle(puzzleData.getBlocks(), 0)) {
            System.out.println("Solusi ditemukan:");
            printBoard();
        } else {
            System.out.println("Tidak ada solusi yang ditemukan.");
        }
    }

    // TODO
    private static boolean solvePuzzle(List<Block> blocks, int index) {
        // Board condition check
        if (isBoardFilled()) {
            if (index < blocks.size()) {
                System.out.println("Jumlah blok melebihi kapasitas papan.");
                return false;
            }
            return true;
        }

        if (index == blocks.size()) {
            return false;
        }

        // Brute force searching
    }

    public static isBoardFilled() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    public static void printBoard() {
        for (char[] row : board) {
            System.out.println(new String(row));
        }
    }

    private static PuzzleData readPuzzleData(String filePath) {
        int N = 0, M = 0, P = 0;
        String S = "";
        List<Block> blocks = new ArrayList<>();

        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            if (fileScanner.hasNextLine()) {
                String[] firstLine = fileScanner.nextLine().trim().split("\\s+");
                N = Integer.parseInt(firstLine[0]);
                M = Integer.parseInt(firstLine[1]);
                P = Integer.parseInt(firstLine[2]);
            }

            if (fileScanner.hasNextLine()) {
                S = fileScanner.nextLine().trim();
            }

            List<String> lines = new ArrayList<>();
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }

            List<List<String>> groups = new ArrayList<>();
            List<String> currentGroup = new ArrayList<>();
            for (String line : lines) {
                if (currentGroup.isEmpty()) {
                    currentGroup.add(line);
                } else {
                    if (line.charAt(0) == currentGroup.get(0).charAt(0)) {
                        currentGroup.add(line);
                    } else {
                        groups.add(currentGroup);
                        currentGroup = new ArrayList<>();
                        currentGroup.add(line);
                    }
                }
            }
            if (!currentGroup.isEmpty()) {
                groups.add(currentGroup);
            }

            for (List<String> group : groups) {
                char identifier = group.get(0).charAt(0);
                char[][] shape = new char[group.size()][];
                for (int i = 0; i < group.size(); i++) {
                    shape[i] = group.get(i).toCharArray();
                }
                blocks.add(new Block(identifier, shape));
            }
            if (blocks.size() != P) {
                System.out.println("Jumlah block tidak sesuai dengan P. Ditemukan: " + blocks.size() + ", sedangkan P: " + P);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new PuzzleData(N, M, P, S, blocks);
    }

    private static void testParsing(PuzzleData puzzleData) {
        System.out.println("N = " + puzzleData.getN());
        System.out.println("M = " + puzzleData.getM());
        System.out.println("P = " + puzzleData.getP());
        System.out.println("S = " + puzzleData.getS());
    
        System.out.println("List of Blocks:");
        for (Block block : puzzleData.getBlocks()) {
            System.out.println("Identifier: " + block.getIdentifier());
            for (char[] row : block.getShape()) {
                System.out.println(new String(row));
            }
            System.out.println("---");
        }
    }
}
