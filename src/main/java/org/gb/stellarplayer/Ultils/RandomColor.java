package org.gb.stellarplayer.Ultils;

import java.util.Random;

public class RandomColor {
    public static String getRandomColor() {
        Random random = new Random();
        int nextInt = random.nextInt(256*256*256);
        String colorCode = String.format("%06x", nextInt);
        return colorCode;
    }
}
