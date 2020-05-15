package com.flamexander.netty.example.proto_file;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == (byte) 25) {               // сигнальный байт
                    currentState = State.NAME_LENGTH;    // то переключаемся на  длину файла
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
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
                    out.write(buf.readByte());         // пишем байты по созданному пути
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {   // сверяемся с длиной файла
                        currentState = State.IDLE;          // если получили ожидаемое возвр.в нач.состояние
                        System.out.println("File received");
                        out.close();
                        break;                             //  и уходим
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();                       //   и чистим  буфер
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
