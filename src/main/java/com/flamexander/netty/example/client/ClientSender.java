package com.flamexander.netty.example.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientSender {

    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }

    private static final byte SIGNAL_COMMAND =20;
    private static final byte SIGNAL_BYTE_FILE=25;


    private static final String GET_FILE= "GET?";
    private static String DELETE_FILE= "DEL?";
    private static String OPEN_ACCESS= "OP/?";
    private static String CLOSE_ASSESS= "CL/?";
    private static String SYNCHRONIZE= "SYN";



    private State currentState = State.IDLE;
    private  int nextLength;
    private long fileLength;
    private  long receivedFileLength;

    private  BufferedOutputStream out;



    public static void sendFile(Path path, Channel channel, ChannelFutureListener finishListener) throws IOException {

        FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

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
        buf.writeLong(Files.size(path));
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
    public static void sendFileREQ(Path path, Channel channel, ChannelFutureListener finishListener) throws IOException {
    //   FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));

        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(SIGNAL_COMMAND);
        channel.writeAndFlush(buf);

        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
//        channel.writeAndFlush(buf);
        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }


    }
    public static void getFile( String filename, Channel channel, ChannelFutureListener finishListener) throws IOException {
          String command=GET_FILE+filename;
          sendCommand(command,channel,finishListener);
    }
    public static void deleteFile( String filename, Channel channel, ChannelFutureListener finishListener) throws IOException {
        String command=DELETE_FILE+ filename;
        sendCommand(command,channel,finishListener);
    }
    public static void openAccess( String filename, Channel channel, ChannelFutureListener finishListener) throws IOException {
        String command=OPEN_ACCESS+filename;
        sendCommand(command,channel,finishListener);
    }
    public static void closeAccess( String filename, Channel channel, ChannelFutureListener finishListener) throws IOException {
        String command=CLOSE_ASSESS+filename;
        sendCommand(command,channel,finishListener);
    }

    public static void sendCommand(String command,  Channel channel, ChannelFutureListener finishListener) throws IOException {
     //  String mes=command+path.getFileName().toString();
        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte(SIGNAL_COMMAND);
        channel.writeAndFlush(buf);

        byte[] filenameBytes = command.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(filenameBytes.length);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
//        channel.writeAndFlush(buf);
        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }


    }
}