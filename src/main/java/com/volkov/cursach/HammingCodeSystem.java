package com.volkov.cursach;

import java.util.Random;

public class HammingCodeSystem {

    // Кодирование одной строки с использованием кода Хэмминга (17, 7)
    public static int[] encodeHorizontal(int[] dataBits) {
        int[] encoded = new int[17];
        int[] parityPositions = {0, 1, 3, 7, 15}; // Позиции проверочных битов

        // Копируем информационные биты в закодированное сообщение
        for (int i = 0, j = 0; i < 17; i++) {
            if (j < parityPositions.length && parityPositions[j] == i) {
                j++; // Пропускаем позиции проверочных битов
            } else {
                if (i - j < dataBits.length) {
                    encoded[i] = dataBits[i - j];
                }
            }
        }

        // Вычисляем проверочные биты
        for (int i = 0; i < parityPositions.length; i++) {
            int parityIndex = parityPositions[i];
            int parity = 0;
            for (int j = 0; j < 17; j++) {
                if (((j + 1) & (parityIndex + 1)) != 0) {
                    parity ^= encoded[j]; // Используем XOR для битов 0 и 1
                }
            }
            encoded[parityIndex] = parity;
        }

        return encoded;
    }


    // Кодирование блока из 17 строк с вертикальными проверочными битами (18, 17)
    public static int[][] encodeVertical(int[][] horizontalEncoded) {
        int rows = horizontalEncoded.length;
        int cols = horizontalEncoded[0].length;
        int[][] blockWithVerticalParity = new int[rows + 1][cols];

        // Копируем горизонтально закодированные строки
        for (int i = 0; i < rows; i++) {
            System.arraycopy(horizontalEncoded[i], 0, blockWithVerticalParity[i], 0, cols);
        }

        // Вычисляем вертикальные проверочные строки
        for (int col = 0; col < cols; col++) {
            int parity = 0;
            for (int row = 0; row < rows; row++) {
                parity ^= blockWithVerticalParity[row][col];
            }
            blockWithVerticalParity[rows][col] = parity; // XOR для вертикальных битов
        }
        return blockWithVerticalParity;
    }

    // Декодирование строки с использованием кода Хэмминга (17, 7)
    public static int[] decodeHorizontal(int[] received) {
        int[] parityPositions = {0, 1, 3, 7, 15};
        int errorPosition = 0;

        // Вычисление синдромов
        for (int i = 0; i < parityPositions.length; i++) {
            int parityIndex = parityPositions[i];
            int parity = 0;
            for (int j = 0; j < 17; j++) {
                if (((j + 1) & (parityIndex + 1)) != 0) {
                    parity ^= received[j];
                }
            }
            if (parity != 0) {
                errorPosition += parityIndex + 1;
            }
        }

        // Исправление ошибки
        if (errorPosition > 0 && errorPosition <= 17) {
            System.out.println("Исправление ошибки в строке, позиция: " + errorPosition);
            received[errorPosition - 1] ^= 1;
        }
        return received;
    }

    // Декодирование блока по вертикали
    public static int[][] decodeVertical(int[][] block) {
        int rows = block.length;
        int cols = block[0].length;

        for (int col = 0; col < cols; col++) {
            int parity = 0;
            for (int row = 0; row < rows - 1; row++) {
                parity ^= block[row][col];
            }
            parity ^= block[rows - 1][col];
            if (parity != 0) {
                System.out.println("Исправление ошибки в столбце: " + col);
                block[rows - 1][col] ^= 1;
            }
        }
        return block;
    }

    // Декодирование блока до выполнения условий
    public static int[][] decodeBlock(int[][] block) {
        boolean changed;
        int iterations = 0;
        final int maxIterations = 100; // Максимальное число итераций

        do {
            System.out.println("Итерация декодирования: " + (iterations + 1));
            changed = false;
            for (int i = 0; i < block.length - 1; i++) {
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
                System.out.println("Декодирование не удалось: превышено максимальное количество итераций.");
                break;
            }
        } while (changed);

        if (!changed) {
            System.out.println("Декодирование завершено успешно после " + iterations + " итераций.");
        }

        return block;
    }

    // Проверка равенства двух блоков
    private static boolean blocksEqual(int[][] block1, int[][] block2) {
        for (int i = 0; i < block1.length; i++) {
            if (!java.util.Arrays.equals(block1[i], block2[i])) {
                return false;
            }
        }
        return true;
    }

    // Копирование блока
    private static int[][] copyBlock(int[][] block) {
        int[][] copy = new int[block.length][block[0].length];
        for (int i = 0; i < block.length; i++) {
            System.arraycopy(block[i], 0, copy[i], 0, block[i].length);
        }
        return copy;
    }

    // Моделирование шума в блоке
    public static int[][] introduceNoise(int[][] block, double noiseProbability) {
        Random random = new Random();
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[i].length; j++) {
                if (random.nextDouble() < noiseProbability) {
                    block[i][j] ^= 1; // Инвертируем бит
                }
            }
        }
        return block;
    }

    // Главный метод для тестирования
    public static void main(String[] args) {
        Random random = new Random();
        int[][] dataBits = new int[17][7];

        // Генерация случайных данных 0 или 1
        for (int i = 0; i < dataBits.length; i++) {
            for (int j = 0; j < dataBits[i].length; j++) {
                dataBits[i][j] = random.nextInt(2);
            }
        }

        System.out.println("Оригинальные данные:");
        printBlock(dataBits);

        // Горизонтальное кодирование
        int[][] horizontalEncoded = new int[dataBits.length][17];
        for (int i = 0; i < dataBits.length; i++) {
            horizontalEncoded[i] = encodeHorizontal(dataBits[i]);
        }

        // Вертикальное кодирование
        int[][] verticalEncoded = encodeVertical(horizontalEncoded);

        System.out.println("Закодированный блок:");
        printBlock(verticalEncoded);

        // Моделирование шума
        double noiseProbability = 0.025;
        int[][] noisyBlock = introduceNoise(verticalEncoded, noiseProbability);

        System.out.println("Блок с ошибками:");
        printBlock(noisyBlock);

        // Декодирование
        int[][] decodedBlock = decodeBlock(noisyBlock);

        System.out.println("Декодированный блок:");
        printBlock(decodedBlock);

        System.out.println("Оригинальные данные для сравнения:");
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
