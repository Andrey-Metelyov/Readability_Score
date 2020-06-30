package readability;

import java.io.IOException;
import java.lang.module.FindException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            return;
        }
        String filename = args[0];
        try {
            String text = readFileAsString(filename);
            System.out.println("The text is:");
            System.out.println(text);
            processText2(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processText2(String text) {
        String[] sentences = text.split("[.!?]\\s*");
        String[] words = text.split("\\s");
        int characters = text.replaceAll("\\s", "").length();
        int[] syllables = getSyllables(words);

        System.out.println("Words: " + words.length);
        System.out.println("Sentences: " + sentences.length);
        System.out.println("Characters: " + characters);
        System.out.println("Syllables: " + syllables[0]);
        System.out.println("Polysyllables: " + syllables[1]);
        double ARI = (4.71 * characters / words.length + 0.5 * words.length / sentences.length - 21.43);
        double FK = 0.39 * words.length / sentences.length + 11.8 * syllables[0] / words.length - 15.59;
        double SMOG = 1.043 * Math.sqrt(30.0 * syllables[1] / sentences.length) + 3.1291;
        double L = (double) characters / words.length * 100;
        double S = (double) sentences.length / words.length * 100;
        double CL = 0.0588 * L - 0.296 * S - 15.8;
//        System.out.printf("The score is: %.2f\n", score);
//        System.out.println("This text should be understood by " + years[(int) Math.ceil(score) - 1] + " year olds.");

        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");

        Scanner scanner = new Scanner(System.in);
        String answer = scanner.nextLine();
        byte flag = 0;
        switch (answer) {
            case "ARI":
                flag |= 0B0001;
                break;
            case "FK":
                flag |= 0B0010;
                break;
            case "SMOG":
                flag |= 0B0100;
                break;
            case "CL":
                flag |= 0B1000;
                break;
            case "all":
            default:
                flag |= 0B1111;
                break;
        }
        System.out.println("");
        if ((flag & 0B0001) != 0) {
            System.out.printf("Automated Readability Index: %.2f (about %d year olds).\n", ARI, getAge(ARI));
        }
        if ((flag & 0B1000) != 0) {
            System.out.printf("Flesch–Kincaid readability tests: %.2f (about %d year olds).\n", FK, getAge(FK));
        }
        if ((flag & 0B0010) != 0) {
            System.out.printf("Simple Measure of Gobbledygook: %.2f (about %d year olds).\n", SMOG, getAge(SMOG));
        }
        if ((flag & 0B0100) != 0) {
            System.out.printf("Coleman–Liau index: %.2f (about %d year olds).\n", CL, getAge(CL));
        }

        if (flag == 0B1111) {
            double avg = (getAge(ARI)+ getAge(SMOG)+ getAge(CL)+ getAge(FK)) / 4.0;
            System.out.printf("\nThis text should be understood in average by %.2f year olds.\n", avg);
        }
    }

    private static int getAge(double readabilityIndex) {
        int index = (int) Math.round(readabilityIndex);
        int[] years = {
                5, 6, 7, 9, 10,
                11, 12, 13, 14, 15,
                16, 17, 18, 24};
        if (index >= years.length) {
            return years[years.length - 1];
        }
        return years[index];
    }


    private static int[] getSyllables(String[] words) {
        int[] result = new int[2]; // 0 - syllables, 1 - polysyllables
        int totalSyllables = 0;
        int polysyllables = 0;
        for (String word : words) {
            int syllables = getNumberOfSyllables(word);
            totalSyllables += syllables;
            if (syllables > 2) {
                polysyllables++;
            }
//            System.out.println(word + " - " + syllables + " syllables");
        }
        result[0] = totalSyllables;
        result[1] = polysyllables;
        return result;
    }

    private static Integer getNumberOfSyllables(String word) {
        List vowels = List.of('a', 'e', 'i', 'o', 'u', 'y');
        word = word.replaceAll("[!.,]", "").toLowerCase();
        int res = 0;
        for (int i = 0; i < word.length(); i++) {
            if (vowels.contains(word.charAt(i))) {
                if (i > 0 && vowels.contains(word.charAt(i - 1)) || (i == (word.length() - 1) && word.charAt(i) == 'e')) {
                    continue;
                }
                res++;
            }
        }
        return res == 0 ? 1 : res;
    }

    public static String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }
}
