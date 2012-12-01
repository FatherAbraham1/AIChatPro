package aichatpro;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileHelper {
    public static String ReadFile(String filename) {
        if (fileCache.containsKey(filename)) {
            return fileCache.get(filename);
        }

        StringBuilder str = null;

        try {
            BufferedReader reader =
                new BufferedReader(new FileReader(filename));

            str = new StringBuilder(bufferSize);

            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line);
                if (line != null) {
                    str.append('\n');
                }
            }

            reader.close();
            if (useCache) {
                fileCache.put(filename, str.toString());
            }
        } catch (Exception e) {

        }

        return (str != null) ? str.toString() : null;
    }

    public static byte[] ReadFileBinary(String filename) {
        if (binaryFileCache.containsKey(filename)) {
            return binaryFileCache.get(filename);
        }

        byte[] buff = new byte[bufferSize];
        byte[] read = null;

        int sizeRead = 0;

        try {
            FileInputStream reader = new FileInputStream(filename);

            int r;
            while ((r = reader.read()) != -1 && sizeRead < buff.length) {
                buff[sizeRead++] = (byte) r;
            }

            read = new byte[sizeRead];
            System.arraycopy(buff, 0, read, 0, sizeRead);

            if (useCache) {
                binaryFileCache.put(filename, read);
            }
        } catch (Exception e) {

        }

        return read;
    }

    // 256KB
    private static int bufferSize = 1024 << 8;
    public static boolean useCache = true;

    // Cache memory
    private static Map<String, String> fileCache =
        new HashMap<String, String>();
    private static Map<String, byte[]> binaryFileCache =
        new HashMap<String, byte[]>();
}
