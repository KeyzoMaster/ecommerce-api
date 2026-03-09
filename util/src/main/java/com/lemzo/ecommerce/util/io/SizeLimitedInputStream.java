package com.lemzo.ecommerce.util.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream qui limite la quantité de données lues pour éviter les attaques DoS.
 */
public class SizeLimitedInputStream extends FilterInputStream {

    private final long maxSize;
    private long bytesRead;

    public SizeLimitedInputStream(final InputStream inputStream, final long maxSize) {
        super(inputStream);
        this.maxSize = maxSize;
    }

    @Override
    public int read() throws IOException {
        final int byteRead = super.read();
        if (byteRead != -1) {
            checkSize(1);
        }
        return byteRead;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int length) throws IOException {
        final int bytesCount = super.read(buffer, offset, length);
        if (bytesCount != -1) {
            checkSize(bytesCount);
        }
        return bytesCount;
    }

    private void checkSize(final long count) throws IOException {
        bytesRead += count;
        if (bytesRead > maxSize) {
            throw new IOException("Taille maximale du flux dépassée : " + maxSize);
        }
    }
}
