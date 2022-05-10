package it.pgp.nfctextexchangeexample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class StaticHelper {
    public static byte[] bToSend;
    public static byte[] bReceived;

    // public static final int SIZE = 16384;
    public static final int SIZE = 96; // ESSID (32) + KEY (32) + HOST (~32)

    public static byte[] concat(byte[]... b) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] x : b) outputStream.write(x);
        return outputStream.toByteArray();
    }

    public static byte[] orderedByteArrayConcat(byte[] b, byte[] c) throws IOException {
        if (b.length > c.length) return concat(b,c);
        else if (b.length < c.length) return concat(c,b);
        else { // b.length == c.length
            for (int i=0;i<b.length;i++) {
                if ((int)b[i]>(int)c[i]) return concat(b,c);
                else if ((int)b[i]<(int)c[i]) return concat(c,b);
            }
            return concat(b,c); // arrays are equal
        }
    }
}
