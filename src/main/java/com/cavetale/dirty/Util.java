package com.cavetale.dirty;

import java.util.ArrayList;
import java.util.List;

final class Util {
    private Util() { }

    public static List<String> splitCamelCase(String src) {
        List<String> tokens = new ArrayList<>();
        int wordStart = 0;
        char c = src.charAt(0);
        int capsCount = Character.isUpperCase(c) ? 1 : 0;
        for (int i = 1; i < src.length(); ++i) {
            c = src.charAt(i);
            if (Character.isUpperCase(c)) {
                switch (capsCount) {
                case 0:
                    tokens.add(src.substring(wordStart, i));
                    wordStart = i;
                    break;
                default:
                    break;
                }
                capsCount += 1;
            } else {
                switch (capsCount) {
                case 0:
                case 1:
                    break;
                default:
                    tokens.add(src.substring(wordStart, i - 1));
                    wordStart = i - 1;
                }
                capsCount = 0;
            }
        }
        tokens.add(src.substring(wordStart, src.length()));
        return tokens;
    }

    public static String camelToSnakeCase(String src) {
        List<String> tokens = splitCamelCase(src);
        return String.join("_", tokens).toLowerCase();
    }
}
