package com.flamexander.netty.example.server;

import com.flamexander.netty.example.client.ByteNetwork;
import io.netty.buffer.ByteBuf;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Commander {

    public enum State {
        IDLE, NAME_LENGTH, NAME
    }

    private static final byte SIGNAL_BYTE_GET_MESSAGE = 20;
    private static final byte SIGNAL_BYTE_FILE = 25;

    private static State currentState = State.IDLE;
    private static int nextLength;
    private static long fileLength;
    ;
    private static BufferedInputStream in;

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
                    buf.readBytes(fileName);     // считываем в fileName
                    System.out.println("STATE: Filename received:" + new String(fileName, "UTF-8"));
                    if (Files.exists(Paths.get("server_storage/" + fileName))) {
                        Filer.sendFile(Paths.get("server_storage/" + fileName),
                                ByteNetwork.getInstance().getCurrentChannel(), future -> {
                                    if (!future.isSuccess()) {
                                        future.cause().printStackTrace();
//                Network.getInstance().stop();
                                    }
                                    if (future.isSuccess()) {
                                        System.out.println("Файл успешно передан с сервера");
//                Network.getInstance().stop();
                                    }
                                });
                        System.out.println("Button Commander.Filer.sendFile works");

                        currentState = State.IDLE;
                    }                              // создали путь куда писать    -направили трубу
                }

            }
        }
    }
}