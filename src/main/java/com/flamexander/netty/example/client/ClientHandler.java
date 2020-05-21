package com.flamexander.netty.example.client;


import com.flamexander.netty.example.server.Commander;
import com.flamexander.netty.example.server.Filer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private static final byte SIGNAL_BYTE_GET_MESSAGE = 20;
    private static final byte SIGNAL_BYTE_FILE = 25;

    private static State currentState = State.IDLE;
    private static int nextLength;
    private static long fileLength;
    private static long receivedFileLength;
    //  private BufferedOutputStream out;
    private static BufferedOutputStream out;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == SIGNAL_BYTE_FILE) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            }
            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    nextLength = buf.readInt();
                    currentState = State.NAME;
                }
            }
            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    System.out.println("CLIENT STATE: Filename received - " + new String(fileName, "UTF-8"));
                    out = new BufferedOutputStream(new FileOutputStream("client_storage/" + new String(fileName)));
                    currentState = State.FILE_LENGTH;
                }
            }
            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }
            if (currentState == State.FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        System.out.println("File received in cleint: ");
                        out.close();
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }



    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        System.out.println(" ошибки при передаче в хендлере клиента");
    }


}