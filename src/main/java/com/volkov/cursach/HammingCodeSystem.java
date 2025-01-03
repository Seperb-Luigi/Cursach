package com.volkov.cursach;

import java.util.Random;

public class HammingCodeSystem {

    // Кодування одного рядка з використанням коду Хеммінгу (9, 5)
    public static int[] encodeHorizontal(int[] dataBits) {
        if (dataBits.length != 5) {
            throw new IllegalArgumentException("Довжина dataBits повинна дорівнювати 5.");
        }

        int[] encoded = new int[9]; // Підсумковий рядок із перевірочними бітами
        int[] parityPositions = {0, 1, 3, 7}; // Позиції перевірочних бітів

        // Копіюємо інформаційні біти у закодоване повідомлення
        for (int i = 0, j = 0; i < 9; i++) {
            if (j < parityPositions.length && parityPositions[j] == i) {
                j++; // Пропускаємо позиції для перевірочних бітів
            } else {
                encoded[i] = dataBits[i - j];
            }
        }

        // Обчислюємо перевірочні біти
        for (int i = 0; i < parityPositions.length; i++) {
            int parityIndex = parityPositions[i];
            int parity = 0;
            for (int j = 0; j < 9; j++) {
                // Перевіряємо, чи належить поточний біт до цієї групи перевірок
                if (((j + 1) & (parityIndex + 1)) != 0) {
                    parity ^= encoded[j]; // Використовуємо XOR для розрахунку парності
                }
            }
            encoded[parityIndex] = parity; // Встановлюємо значення перевірочного біта
        }

        return encoded;
    }

    // Кодування блоку з 18 рядків із вертикальними перевірочними бітами (23, 18)
    public static int[][] encodeVertical(int[][] horizontalEncoded) {
        int rows = horizontalEncoded.length;
        int cols = horizontalEncoded[0].length;
        int[][] blockWithVerticalParity = new int[rows + 5][cols]; // Додаємо 5 рядків для вертикальних перевірочних бітів

        // Копіюємо горизонтально закодовані рядки
        for (int i = 0; i < rows; i++) {
            System.arraycopy(horizontalEncoded[i], 0, blockWithVerticalParity[i], 0, cols);
        }

        // Обчислюємо вертикальні рядки перевірок
        for (int col = 0; col < cols; col++) {
            for (int parityRow = rows; parityRow < rows + 5; parityRow++) {
                int parity = 0;
                for (int row = 0; row < rows; row++) {
                    parity ^= blockWithVerticalParity[row][col];
                }
                blockWithVerticalParity[parityRow][col] = parity;
            }
        }

        return blockWithVerticalParity;
    }

    // Декодування рядка з використанням коду Хеммінгу (9, 5)
    public static int[] decodeHorizontal(int[] received) {
        int[] parityPositions = {0, 1, 3, 7};
        int errorPosition = 0;

        // Обчислення синдромів
        for (int i = 0; i < parityPositions.length; i++) {
            int parityIndex = parityPositions[i];
            int parity = 0;
            for (int j = 0; j < 9; j++) {
                if (((j + 1) & (parityIndex + 1)) != 0) {
                    parity ^= received[j];
                }
            }
            if (parity != 0) {
                errorPosition += parityIndex + 1;
            }
        }

        // Виправлення помилки
        if (errorPosition > 0 && errorPosition <= 9) {
            System.out.println("Виправлення помилки у рядку, позиція: " + errorPosition);
            received[errorPosition - 1] ^= 1;
        }
        return received;
    }

    // Декодування блоку з вертикалі
    public static int[][] decodeVertical(int[][] block) {
        int rows = block.length;
        int cols = block[0].length;

        for (int col = 0; col < cols; col++) {
            int parity = 0;
            for (int row = 0; row < rows - 5; row++) {
                parity ^= block[row][col];
            }
            for (int row = rows - 5; row < rows; row++) {
                parity ^= block[row][col];
            }

            if (parity != 0) {
                System.out.println("Виправлення помилки у стовпці: " + col);
                block[rows - 1][col] ^= 1;
            }
        }
        return block;
    }

    // Декодування блоку до виконання умов
    public static int[][] decodeBlock(int[][] block) {
        boolean changed;
        int iterations = 0;
        final int maxIterations = 100; // Максимальна кількість ітерацій

        do {
            System.out.println("Ітерація декодування: " + (iterations + 1));
            changed = false;
            for (int i = 0; i < block.length - 5; i++) {
                int[] original = block[i].clone();
                block[i] = decodeHorizontal(block[i]);
                if (!java.util.Arrays.equals(original, block[i])) {
                    changed = true;
                }
            }

            int[][] originalBlock = copyBlock(block);
            block = decodeVertical(block);
            if (!blocksEqual(originalBlock, block)) {
                changed = true;
            }

            iterations++;
            if (iterations >= maxIterations) {
                System.out.println("Декодування не вдалося: перевищено максимальну кількість ітерацій.");
                break;
            }
        } while (changed);

        if (!changed) {
            System.out.println("Декодування завершено успішно після " + iterations + " ітерацій.");
        }

        return block;
    }

    // Перевірка рівності двох блоків
    private static boolean blocksEqual(int[][] block1, int[][] block2) {
        for (int i = 0; i < block1.length; i++) {
            if (!java.util.Arrays.equals(block1[i], block2[i])) {
                return false;
            }
        }
        return true;
    }

    // Копіювання блоку
    private static int[][] copyBlock(int[][] block) {
        int[][] copy = new int[block.length][block[0].length];
        for (int i = 0; i < block.length; i++) {
            System.arraycopy(block[i], 0, copy[i], 0, block[i].length);
        }
        return copy;
    }

    // Моделювання шуму в блоці
    public static int[][] introduceNoise(int[][] block, double noiseProbability) {
        Random random = new Random();
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[i].length; j++) {
                if (random.nextDouble() < noiseProbability) {
                    block[i][j] ^= 1; // Інвертуємо біт
                }
            }
        }
        return block;
    }

    // Головний метод для тестування
    public static void main(String[] args) {
        Random random = new Random();
        int[][] dataBits = new int[18][5];

        // Генерація випадкових даних 0 або 1
        for (int i = 0; i < dataBits.length; i++) {
            for (int j = 0; j < dataBits[i].length; j++) {
                dataBits[i][j] = random.nextInt(2);
            }
        }

        System.out.println("Оригінальні дані:");
        printBlock(dataBits);

        // Горизонтальне кодування
        int[][] horizontalEncoded = new int[dataBits.length][9];
        for (int i = 0; i < dataBits.length; i++) {
            horizontalEncoded[i] = encodeHorizontal(dataBits[i]);
        }

        // Вертикальне кодування
        int[][] verticalEncoded = encodeVertical(horizontalEncoded);

        System.out.println("Закодований блок:");
        printBlock(verticalEncoded);

        // Моделювання шуму
        double noiseProbability = 0.015;
        int[][] noisyBlock = introduceNoise(verticalEncoded, noiseProbability);

        System.out.println("Блок із помилками:");
        printBlock(noisyBlock);

        // Декодування
        int[][] decodedBlock = decodeBlock(noisyBlock);

        System.out.println("Декодований блок:");
        printBlock(decodedBlock);

        System.out.println("Оригінальні дані для порівняння:");
        printBlock(horizontalEncoded);
    }

    private static void printBlock(int[][] block) {
        for (int[] row : block) {
            for (int bit : row) {
                System.out.print(bit + " ");
            }
            System.out.println();
        }
    }
}
