package it.pgp.nfctextexchangeexample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import org.spongycastle.crypto.StreamCipher;
import org.spongycastle.crypto.digests.SHA3Digest;
import org.spongycastle.crypto.engines.ChaChaEngine;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

public class HashView extends View {
    final int[] colors8 = {Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.WHITE};
//    final int[] colors16 = {0xFF000000,0xFF808080,0xFFC0C0C0,0xFFFFFFFF,
//                            0xFF800000,0xFFFF0000,0xFF808000,0xFFFFFF00,
//                            0xFF008000,0xFF00FF00,0xFF008080,0xFF00FFFF,
//                            0xFF000080,0xFF0000FF,0xFF800080,0xFFFF00FF};

    final int[] colors1s = {0xFF000000,0xFF7F0000,0xFF7F006E,0xFF1F007F,
            0xFF007F7F,0xFF007F00,0xFF7F7F00,0xFF7F4400,
            0xFFFF8800,0xFFFFFF00,0xFF00FF00,0xFF00FFFF,
            0xFF7F7F7F,0xFFFF00FF,0xFFFF0000,0xFFFFFFFF};

    /*
    2: Black + Red
    4: Black + RGB
    8: Black + RGB + White + CMY
    16: Black + RGB + White + CMY + others
     */

    final int[] colors16_Ordered = {0xFF000000,0xFFFF0000,0xFF00FF00,0xFF1F007F, // Black+RGB
            0xFFFFFFFF,0xFFFFFF00,0xFF00FFFF,0xFFFF00FF, // White + CMY
            0xFF7F0000,0xFF007F7F,0xFF007F00,0xFF7F7F00,
            0xFF7F4400,0xFF7F006E,0xFFFF8800,0xFF7F7F7F};

    // generate pseudorandom stream of given length from key of fixed length
    public static byte[] c20_streamgen(byte[] key, int outBitLen) throws IllegalArgumentException {
        byte[] nonce = new byte[8];
        int roundedByteSize = (outBitLen%8==0)?outBitLen/8:outBitLen/8+1;
        byte[] input = new byte[roundedByteSize];
        StreamCipher c20 = new ChaChaEngine();
        ParametersWithIV p = new ParametersWithIV(new KeyParameter(key),nonce);
        c20.init(true,p);
        byte[] cipherText = new byte[input.length];
        c20.processBytes(input,0,input.length,cipherText,0);
        return cipherText;
    }


    PaintRect[][] M;

    public HashView(Context context, byte[] in, int gridSize, int bitsPerCell, int width, int height) {
        super(context);
        M = gridCalculator(in, gridSize, bitsPerCell, width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        draw2(canvas, M);
    }

    boolean[] byteArrayToBitArray (byte[] b) {
        boolean[] x = new boolean[b.length*8];
        for (int j=0;j<b.length;j++) {
            for(int i=0;i<8;i++) {
                x[j*8+i] = ((b[j] & (1 << i)) > 0);
            }
        }
        return x;
    }

    int getBitSeqFromBooleanArray(int bit_index, int bit_length, boolean[] bb) {
        int a = 0;
        int m=1;
        for(int i=bit_index+bit_length-1;i>=bit_index;i--) {
            a += (bb[i])?m:0;
            m<<=1;
        }
        return a;
    }

    PaintRect[][] gridCalculator(byte[] b, int gridSize, int bitsPerCell, int width, int height) {
        int rSize = Math.min(width, height) / gridSize;

        PaintRect[][] M = new PaintRect[gridSize][gridSize];

        // compression: get fixed-size hash from arbitrary length input
        byte[] digestOctets = new byte[32];
        SHA3Digest sd = new SHA3Digest(256);
        sd.update(b, 0, b.length);
        sd.doFinal(digestOctets, 0);

        // expansion: get desired-size bit stream from fixed length hash
        int outSize = gridSize * gridSize * bitsPerCell;
        byte[] outDigest = c20_streamgen(digestOctets,outSize);

        boolean[] bb = byteArrayToBitArray(outDigest);

        for (int i = 0; i < gridSize; i++)
            for (int j = 0; j < gridSize; j++) {
                Rect currentRect = new Rect(i * rSize, j * rSize, (i + 1) * rSize, (j + 1) * rSize);
                // int rColor = GetBits(bitsPerCell*(gridSize*i+j),bitsPerCell,digestOctets);
//                int rColor = GetBits(digestOctets, bitsPerCell * (gridSize * i + j), bitsPerCell);
                int rColor = getBitSeqFromBooleanArray(bitsPerCell*(gridSize*i+j),bitsPerCell,bb);

                Paint currentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                currentPaint.setStyle(Paint.Style.FILL);
                currentPaint.setColor(colors16_Ordered[rColor]);
                M[i][j] = new PaintRect(currentRect, currentPaint);
//                Log.d("************",""+rColor);
//                Log.d("************",""+i+" "+j);
            }

        return M;
    }

    void draw2(Canvas canvas, PaintRect[][] M) {
        for (PaintRect[] aM : M)
            for (PaintRect anAM : aM)
                canvas.drawRect(anAM.rect, anAM.rPaint);
    }

    private class PaintRect {
        Rect rect;
        Paint rPaint;

        PaintRect(Rect rect, Paint rPaint) {
            this.rect = rect;
            this.rPaint = rPaint;
        }
    }
}
