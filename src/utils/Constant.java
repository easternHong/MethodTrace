package utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by g8489 on 18-4-18.
 */

public class Constant {

    public static final String LOGGER_TAG = "YYXposed:";

    /**
     * Signature    Java Type
     * Z    boolean
     * B    byte
     * C    char
     * S    short
     * I    int
     * J    long
     * F    float
     * D    double
     * V    void
     * L fully-qualified-class ;    fully-qualified-class
     * [ type   type[]
     */
    public static Map<String, String> mSignatureMap = new HashMap<String, String>();


    static {
        mSignatureMap.put("V", Void.class.getName());
        mSignatureMap.put("Z", Boolean.class.getName());
        mSignatureMap.put("B", Byte.class.getName());
        mSignatureMap.put("C", Character.class.getName());
        mSignatureMap.put("S", Short.class.getName());
        mSignatureMap.put("I", Integer.class.getName());
        mSignatureMap.put("J", Long.class.getName());
        mSignatureMap.put("F", Float.class.getName());
        mSignatureMap.put("D", Double.class.getName());
    }

    public static void main(String[] args) {
        System.out.println(mSignatureMap);
    }



}
