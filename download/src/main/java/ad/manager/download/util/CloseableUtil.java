package ad.manager.download.util;

import java.io.Closeable;
import java.io.IOException;

public class CloseableUtil  {

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
