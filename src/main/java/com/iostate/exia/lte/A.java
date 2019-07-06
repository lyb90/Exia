package com.iostate.exia.lte;

import java.io.Serializable;

public class A implements Serializable {
    private static String a = "";

    private void a(String a) throws Exception {
        System.out.println(this);
    }
//    private void writeObject(java.io.ObjectOutputStream s)
//            throws IOException {
//        s.writeObject(this);
//    }
//
//    private void readObject(java.io.ObjectInputStream s)
//            throws IOException, ClassNotFoundException {
//        s.readObject();
//    }

    private class B extends Exception {

    }
}
