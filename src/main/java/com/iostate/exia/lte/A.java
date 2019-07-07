package com.iostate.exia.lte;

import java.io.Serializable;

public class A implements Serializable {
    private static String a = "";
    private String ax = "";

//d

    private static class B extends Exception {
        public static String b = "xx";
        public String bx = "xx";
    }
    //xx

    private class C extends Number {
        public String cx = "xx";

        @Override
        public int intValue() {
            return 0;
        }

        @Override
        public long longValue() {
            return 0;
        }

        @Override
        public float floatValue() {
            return 0;
        }

        @Override
        public double doubleValue() {
            return 0;
        }
    }
}
//}
