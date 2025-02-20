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
    private static char[][] board;
    private static long kasusCount = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Masukkan path dari input file: ");
        String filePath = scanner.nextLine();
    
        PuzzleData puzzleData = readPuzzleData(filePath);
    
        board = new char[puzzleData.getN()][puzzleData.getM()];
        for (int i = 0; i < puzzleData.getN(); i++) {
            for (int j = 0; j < puzzleData.getM(); j++) {
                board[i][j] = '.';
            }
        }
        
        try (PrintWriter writer = new PrintWriter(new File("result.txt"))) {
            // Start timer
            long startTime = System.currentTimeMillis();
    
            List<char[][]> boardState = new ArrayList<>();
            boolean solved = solvePuzzle(puzzleData.getBlocks(), 0, boardState, writer);
    
            // End timer
            long endTime = System.currentTimeMillis();

            if (solved) {
                System.out.println("Solusi ditemukan:");
                printBoard();
            } else {
                System.out.println("Tidak ada solusi yang ditemukan.");
            }
            System.out.println("Waktu pencarian: " + (endTime - startTime) + " ms");
            System.out.println("Jumlah kasus yang ditinjau: " + kasusCount);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Main solver algorithm
    private static boolean solvePuzzle(List<Block> blocks, int index, List<char[][]> boardState, PrintWriter writer) {
        if (isBoardFilled()) {
            if (index < blocks.size()) {
                writer.println("Jumlah blok melebihi kapasitas papan.");
                writer.println();
                return false;
            }
            // Print board
            printBoardStateToFile(copyBoard(board), writer);
            writer.println();
            writer.flush();
            return true;
        }
        if (index == blocks.size()) {
            return false;
        }
        
        Block currentBlock = blocks.get(index);
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                for (char[][] orientation : generateOrientations(currentBlock.getShape())) {
                    if (validPlace(orientation, row, col)) {
                        placeBlock(orientation, row, col, currentBlock.getIdentifier());
                        kasusCount++;

                        // Print board after block placement
                        printBoardStateToFile(copyBoard(board), writer);
                        writer.println();
                        writer.flush();
        
                        if (solvePuzzle(blocks, index + 1, boardState, writer)) {
                            return true;
                        }
        
                        removeBlock(orientation, row, col);

                        // Print board after block removal (backtracking)
                        printBoardStateToFile(copyBoard(board), writer);
                        writer.println();
                        writer.flush();
                    }
                }
            }
        }
        return false;
    }

    private static List<char[][]> generateOrientations(char[][] shape) {
        List<char[][]> orientations = new ArrayList<>();
        orientations.add(shape);

        // Original shape rotations
        char[][] rot90 = rotate90(shape);
        char[][] rot180 = rotate90(rot90);
        char[][] rot270 = rotate90(rot180);

        if (!contains(orientations, rot90)) orientations.add(rot90);
        if (!contains(orientations, rot180)) orientations.add(rot180);
        if (!contains(orientations, rot270)) orientations.add(rot270);

        // Reflect shape & reflected shape rotations
        char[][] reflected = reflect(shape);
        char[][] refRot90 = rotate90(reflected);
        char[][] refRot180 = rotate90(refRot90);
        char[][] refRot270 = rotate90(refRot180);

        if (!contains(orientations, reflected)) orientations.add(reflected);
        if (!contains(orientations, refRot90)) orientations.add(refRot90);
        if (!contains(orientations, refRot180)) orientations.add(refRot180);
        if (!contains(orientations, refRot270)) orientations.add(refRot270);
        
        return orientations;
    }

    private static char[][] rotate90(char[][] shape) {
        int m = shape.length;
        int n = shape[0].length;
        char[][] rotated = new char[n][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                rotated[j][m - 1 - i] = shape[i][j];
            }
        }
        return rotated;
    }

    private static char[][] reflect(char[][] shape) {
        int m = shape.length;
        int n = shape[0].length;
        char[][] reflected = new char[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                reflected[i][j] = shape[i][n - 1 - j];
            }
        }
        return reflected;
    }

    private static boolean contains(List<char[][]> list, char[][] candidate) {
        for (char[][] shape : list) {
            if (shapesEqual(shape, candidate)) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean shapesEqual(char[][] a, char[][] b) {
        if (a.length != b.length) return false;
        if (a[0].length != b[0].length) return false;
        
        for (int i = 0; i < a.length; i++) {
            if (a[i].length != b[i].length) return false;
            for (int j = 0; j < a[i].length; j++) {
                if (a[i][j] != b[i][j])
                    return false;
            }
        }
        return true;
    }

    private static boolean validPlace(char[][] shape, int row, int col) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != '.') {
                    int boardR = row + r;
                    int boardC = col + c;
                    if (boardR < 0 || boardR >= board.length ||
                        boardC < 0 || boardC >= board[0].length) {
                        return false;
                    }
                    if (board[boardR][boardC] != '.') {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void placeBlock(char[][] shape, int row, int col, char id) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != '.') {
                    board[row + r][col + c] = id;
                }
            }
        }
    }

    private static void removeBlock(char[][] shape, int row, int col) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[r].length; c++) {
                if (shape[r][c] != '.') {
                    board[row + r][col + c] = '.';
                }
            }
        }
    }

    private static boolean isBoardFilled() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    private static void printBoard() {
        for (char[] row : board) {
            System.out.println(new String(row));
        }
    }

    private static char[][] copyBoard(char[][] original) {
        int rows = original.length;
        int cols = original[0].length;
        char[][] copy = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, cols);
        }
        return copy;
    }

    private static void printBoardStateToFile(char[][] boardState, PrintWriter writer) {
        for (char[] row : boardState) {
            writer.println(new String(row));
        }
        writer.println();
        writer.flush();
    }

    private static PuzzleData readPuzzleData(String filePath) {
        int N = 0, M = 0, P = 0;
        String S = "";
        List<Block> blocks = new ArrayList<>();
    
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            // 1. Read N M P
            if (fileScanner.hasNextLine()) {
                String[] firstLine = fileScanner.nextLine().trim().split("\\s+");
                N = Integer.parseInt(firstLine[0]);
                M = Integer.parseInt(firstLine[1]);
                P = Integer.parseInt(firstLine[2]);
            }
    
            // 2. Read S
            if (fileScanner.hasNextLine()) {
                S = fileScanner.nextLine().trim();
            }
    
            // 3. Read remaining lines and group by block.
            List<List<String>> groups = new ArrayList<>();
            List<String> currentGroup = new ArrayList<>();
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.trim().isEmpty()) {
                    continue;
                }
                char firstChar = line.stripLeading().charAt(0);
                if (currentGroup.isEmpty()) {
                    currentGroup.add(line);
                } else {
                    char firstCharCurrentGroup = currentGroup.get(0).stripLeading().charAt(0);
                    if (firstChar == firstCharCurrentGroup) {
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
                int maxWidth = 0;
                for (String shapeLine : group) {
                    if (shapeLine.length() > maxWidth) {
                        maxWidth = shapeLine.length();
                    }
                }
    
                char[][] shapeArray = new char[group.size()][maxWidth];
                for (int i = 0; i < group.size(); i++) {
                    String rowText = group.get(i);
                    rowText = rowText.replace(' ', '.');
                    while (rowText.length() < maxWidth) {
                        rowText += ".";
                    }
                    shapeArray[i] = rowText.toCharArray();
                }
    
                char id = group.get(0).stripLeading().charAt(0);
                blocks.add(new Block(id, shapeArray));
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
