package test;

public class Test {


    public static void main(String[] args) {
//
        final String content = "com.yy.android.sniper.apt.darts.yymobile_core$$$DartsFactory$$$8986d5ab57efc76ddbf1ea6237319abc$FreeDataServiceImplDartsInnerInstance";
        final String tmp = "$$$DartsFactory$$$";
        System.out.println(content.substring(content.indexOf(tmp) + tmp.length()));
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
