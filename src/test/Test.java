package test;

import kotlin.text.Charsets;
import utils.CloseUtils;
import utils.Log;

import java.io.*;

public class Test {


    public static void main(String[] args) {
        String filePath = "/home/g8489/mappings/ycloud-mappings.txt";
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (!file.isFile()) {
            return;
        }
        BufferedReader reader = null;
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8.name());
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(" ")) {
                    String[] array = line.split("->");
                    if (!equals(array[0].replace(" ", ""), array[1].replace(" ", "").replace(":", "q"))) {
                        System.out.println(line);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            Log.e("", "IOException occurred. ");
        } finally {
            CloseUtils.close(is);
            CloseUtils.close(reader);
        }
    }

    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }
}
