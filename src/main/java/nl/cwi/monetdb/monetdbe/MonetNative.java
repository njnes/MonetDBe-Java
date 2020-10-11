package nl.cwi.monetdb.monetdbe;

import java.nio.ByteBuffer;

public class MonetNative {
    static {
        System.load("/home/bernardo/MonetDBe-Java/build/libmonetdbe-lowlevel.so");
    }

    protected static native ByteBuffer monetdbe_open(String dbdir);
    protected static native int monetdbe_close(ByteBuffer db);
    protected static native ByteBuffer monetdbe_execute(ByteBuffer statement);
    protected static native String monetdbe_error(ByteBuffer db);
}
