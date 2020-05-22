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
import java.nio.file.Paths;


public class InHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }
    private static final byte SIGNAL_BYTE_MESSAGE =20;
    private static final byte SIGNAL_BYTE_FILE=25;

    private State currentState = State.IDLE;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;
    Channel channel;
    Path path;
    String command;

                                // контекст  - вся информация о соединении с клиентом
    @Override                                // ссылка на контекст  +  посылка
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        byte readed=0;
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                 readed = buf.readByte();
                if (readed == SIGNAL_BYTE_FILE || readed== SIGNAL_BYTE_MESSAGE) {
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
                   if(readed==SIGNAL_BYTE_MESSAGE)  command = new String(fileName);
                    System.out.println("SERVER STATE: Filename received in server: " + new String(fileName, "UTF-8"));
                    out = new BufferedOutputStream(new FileOutputStream("server_storage/" + new String(fileName)));
                    if (readed == SIGNAL_BYTE_FILE)
                        currentState = State.FILE_LENGTH;
                    if (readed == SIGNAL_BYTE_MESSAGE) {
                        currentState= State.IDLE;
//                        channel= Server.getCurrentChannel();
//                        if (Files.exists(Paths.get("server_storage/" + name))) {
//                            Sender.sendFile(Paths.get("server_storage/" + name),
//                                    channel, future -> {
//                                        if (!future.isSuccess()) {
//                                            future.cause().printStackTrace();
//
//                                        }
//                                        if (future.isSuccess()) {
//                                            System.out.println("Файл передан с сервера");
//
//                                        }
//                                    });
                        }
                    }
                }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("Server- STATE: File length received - " + fileLength);
                    currentState = State.FILE;
                }
            }
            if (currentState == State.FILE) {
                System.out.println("Получение файла на сервере");
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        System.out.println("File received in server");
                        out.close();
                        break;
                    }
                }
            }
        }
        if (buf.readableBytes() == 0) {
           buf.release();
        }
        if (readed==SIGNAL_BYTE_MESSAGE) {
            if (Files.exists(Paths.get("server_storage/" + command))) {
                sendFile(Paths.get("server_storage/" + command),
                        ServerVariant2.getCurrentChannel(), future -> {
                            if (!future.isSuccess()) {
                                future.cause().printStackTrace();
                            }
                            if (future.isSuccess()) {
                                System.out.println("файл передан с сервера" + command);

                            }
                        });

            }
        }}
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace(); // обязательно делать токое переопределение чтобы знать
        ctx.close();             // что произошло
        System.out.println(" ошибки при передаче во входяшем хендлере");
    }


}
