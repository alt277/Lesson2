package com.flamexander.netty.example.server;

import io.netty.buffer.ByteBuf;

import java.io.*;

public class Commander {

    public enum State {
        IDLE, NAME_LENGTH, NAME
    }
    private static final byte SIGNAL_BYTE_GET_MESSAGE=20;
    private static final byte SIGNAL_BYTE_FILE=25;

    private static State currentState = State.IDLE;
    private static int nextLength;
    private static long fileLength;
   ;
    private static BufferedOutputStream out;

    public static void sendFile(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                currentState = State.NAME_LENGTH;    // то переключаемся на  длину файла

                System.out.println("STATE: Start file receiving");

            }
            if (currentState == State.NAME_LENGTH) {    //тогда заходим в блок для длины
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    nextLength = buf.readInt();      //  считали в поле длина
                    currentState = State.NAME;       //  переключились на имя
                }
            }
            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    System.out.println("STATE: Filename received:" + new String(fileName, "UTF-8"));

                    out = new BufferedOutputStream(new FileOutputStream("server_storage/" + new String(fileName)));
//                    in = new BufferedInputStream(new FileInputStream("server_storage/" + new String(fileName)));
                    currentState = State.IDLE;
                }                              // создали путь куда писать    -направили трубу
            }

        }
    }
}
