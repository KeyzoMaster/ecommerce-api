package com.lemzo.ecommerce.util.io;

import com.lemzo.ecommerce.core.api.exception.BusinessRuleException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper d'InputStream qui limite la lecture à un nombre maximal d'octets.
 * Prévient les attaques par déni de service via de très gros fichiers.
 */
public class SizeLimitedInputStream extends FilterInputStream {

    private final long maxSize;
    private long bytesRead = 0;

    public SizeLimitedInputStream(InputStream in, long maxSize) {
        super(in);
        this.maxSize = maxSize;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            updateCount(1);
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n != -1) {
            updateCount(n);
        }
        return n;
    }

    private void updateCount(int n) {
        bytesRead += n;
        if (bytesRead > maxSize) {
            throw new BusinessRuleException("error.storage.quota_exceeded");
        }
    }

    public long getBytesRead() {
        return bytesRead;
    }
}
