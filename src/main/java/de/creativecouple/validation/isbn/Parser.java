/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 CreativeCouple
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.creativecouple.validation.isbn;

import de.creativecouple.validation.isbn.ISBN.Hyphenation;

final class Parser {

    static ISBN parse(String isbnString) {
        if (isbnString == null || isbnString.isEmpty()) {
            return null;
        }
        final char[] isbn = getDigits(isbnString);
        if (isbn == null) {
            return null;
        }
        if (!(isbn[10] == 0 ? validateChecksum10(isbn) : validateChecksum13(isbn))) {
            return null;
        }
        Hyphenation hyphenation = ISBNRanges.ranges[isbn[0]][isbn[1]][isbn[2]][isbn[3]][isbn[4]][isbn[5]][isbn[6]][isbn[7]][isbn[8]][isbn[9]][isbn[10]];
        if (hyphenation == null) {
            return null;
        }
        return new ISBN(new String(isbn), hyphenation);
    }

    private static char[] getDigits(CharSequence string) {
        char[] chars = new char[13];
        int to = string.length() - 1;
        char ch = string.charAt(0);
        if (ch < '0' || ch > '9') {
            return null;
        }
        chars[0] = ch;
        int charsPos = 1;
        for (int i = 1; i < to; i++) {
            if (charsPos >= 13) {
                return null;
            }
            ch = string.charAt(i);
            if (ch >= '0' && ch <= '9') {
                chars[charsPos++] = ch;
            } else if (ch != '-' && ch != '_' && ch != '–' && ch != '—' && ch != '−') {
                // remove all unicode dashes (0x2d, 0x2013, 0x2014, 0x2212)
                return null;
            }
        }
        ch = string.charAt(to);
        if (charsPos == 9) {
            chars[10] = 0;
            if (ch == 'x' || ch == 'X') {
                chars[9] = '9' + 1;
                return chars;
            }
            if (ch >= '0' && ch <= '9') {
                chars[9] = ch;
                return chars;
            }
            return null;
        }
        if (charsPos == 12 && ch >= '0' && ch <= '9') {
            chars[12] = ch;
            return chars;
        }
        return null;
    }

    private static boolean validateChecksum10(char[] isbn) {
        if ((10 * isbn[0] + 9 * isbn[1] + 8 * isbn[2] + 7 * isbn[3] + 6 * isbn[4] + 5 * isbn[5] + 4 * isbn[6] + 3 * isbn[7] + 2 * isbn[8] + isbn[9]) % 11 != 0) {
            return false;
        }
        isbn[12] = (char) ('0' + (10 - ('9' + '8' + isbn[1] + isbn[3] + isbn[5] + isbn[7] + '0' + 3 * ('7' + isbn[0] + isbn[2] + isbn[4] + isbn[6] + isbn[8])) % 10) % 10);
        for (int x = 8; x >= 0; x--) {
            isbn[x + 3] = isbn[x];
        }
        isbn[2] = '8';
        isbn[1] = '7';
        isbn[0] = '9';
        return true;
    }

    private static boolean validateChecksum13(char[] isbn) {
        return (isbn[0] + isbn[2] + isbn[4] + isbn[6] + isbn[8] + isbn[10] + isbn[12] + 3 * (isbn[1] + isbn[3] + isbn[5] + isbn[7] + isbn[9] + isbn[11])) % 10 == 0;
    }

}
