package com.flamexander.netty.example.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Filer {
    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }
    private static final byte SIGNAL_BYTE_GET_MESSAGE=20;
    private static final byte SIGNAL_BYTE_FILE=25;

    private static State currentState = State.IDLE;
    private static int nextLength;
    private static long fileLength;
    private static long receivedFileLength;

    private static BufferedOutputStream out;

    public static void writeFile(ByteBuf buf) throws IOException {
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {

                currentState = State.NAME_LENGTH;    // то переключаемся на  длину файла
                receivedFileLength = 0L;
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
                    currentState = State.FILE_LENGTH;
                }                              // создали путь куда писать    -направили трубу
            }
            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();        // считали длину
                    System.out.println("STATE: File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }

            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());       // пишем по одному байту по созданному пути
                    receivedFileLength++;          //  и фиксируем сколько записали
                    if (fileLength == receivedFileLength) {   // сверяемся с длиной файла
                        currentState = State.IDLE;          // если получили ожидаемое возвр.в нач.состояние
                        System.out.println("File received "+receivedFileLength);
                        out.close();
                        break;           //  и уходим из цикла записи
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            out.close();
            buf.release();                       //   и чистим  буфер
        }
    }

    public static void sendFile(Path path, Channel channel, ChannelFutureListener finishListener) throws IOException {

        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
        //  пишем файл по указанному пути         с нулевой позиции  сколько: по размеру файла по этому пути
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(SIGNAL_BYTE_FILE);
        channel.writeAndFlush(buf);

        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(8);
        buf.writeLong(Files.size(path));  // размер файла по этому пути
        channel.writeAndFlush(buf);
                                               // отправляем файл,делая это событием
        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
                // и  добавляем слушателя к событию  отправки:
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
                                                /* channelFutureListener finishListener
                                                передан в аргументах метода ( callback ) */
    }


 }
