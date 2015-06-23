package org.ethereum.android.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

class Locale {
    final static int US=0;
}

public class Scanner {

    private BufferedInputStream in;

    int c;

    int offset;

    boolean atBeginningOfLine;

    public Scanner(InputStream stream) {

        in = new BufferedInputStream(stream);
        try {
            atBeginningOfLine = true;
            c  = (char)in.read();
        } catch (IOException e) {
            c  = -1;
        }
    }

    public boolean hasNext() {

        if (!atBeginningOfLine)
            throw new Error("hasNext only works "+
                    "after a call to nextLine");
        return c != -1;
    }

    public String next() {
        StringBuffer sb = new StringBuffer();
        atBeginningOfLine = false;
        try {
            while (c <= ' ') {
                c = in.read();
            }
            while (c > ' ') {
                sb.append((char)c);
                c = in.read();
            }
        } catch (IOException e) {
            c = -1;
            return "";
        }
        return sb.toString();
    }

    public String nextLine() {
        StringBuffer sb = new StringBuffer();
        atBeginningOfLine = true;
        try {
            while (c != '\n') {
                sb.append((char)c);
                c = in.read();
            }
            c = in.read();
        } catch (IOException e) {
            c = -1;
            return "";
        }
        return sb.toString();
    }

    public int nextInt() {
        String s = next();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0; //throw new Error("Malformed number " + s);
        }
    }

    public double nextDouble() {
        return new Double(next());
    }

    public long nextLong() {
        return Long.parseLong(next());
    }

    public void useLocale(int l) {}
}