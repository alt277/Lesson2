package com.flamexander.netty.example.server;

import com.flamexander.netty.example.client.ByteNetwork;
import com.flamexander.netty.example.client.ClientSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;


public class Handler2 extends ChannelInboundHandlerAdapter {
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private static final String GET_FILE= "GET";
    private static final String DELETE_FILE= "DEL";
    private static final String OPEN_ACCESS= "OP/";
    private static final String CLOSE_ASSESS= "CL/";
    private static final String SYNCHRONIZE= "SYN";
//    private  BufferedInputStream in;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] arr = (byte[]) msg;
        String message = new String(arr);
        System.out.println("message= "+message);
        String[] parts = message.split("\\?");
        String fileName="";
        for (int i = 1; i < parts.length; i++) {
            fileName += parts[i];
        }


        System.out.println("Имя файла после удал. комманды:"+fileName);
        if (message.startsWith(GET_FILE)) {
            if (Files.exists(Paths.get("server_storage/" + fileName))) {
                Sender.sendFile(Paths.get("server_storage/" + fileName),
                        ctx.channel(), future -> {

                            if (!future.isSuccess()) {
                                future.cause().printStackTrace();
                            }
                            if (future.isSuccess()) {
                                System.out.println(" Файл отослан с сервера " + message);
                            }
                        });
                System.out.println(" Handler2 : file:" + fileName + " отослан ");
            }
        }
        if (message.startsWith(DELETE_FILE)) {
            Files.deleteIfExists(Paths.get("server_storage/"+ fileName));
            System.out.println("Блок delete!");
        }
        if (message.startsWith(OPEN_ACCESS)) {
            if (Files.exists(Paths.get("server_storage/" + fileName))) {
                ByteBuf buf = null;
                byte[] filenameBytes = Paths.get("server_storage/" + fileName).getFileName().toString().getBytes(StandardCharsets.UTF_8);
                out = new BufferedOutputStream(new FileOutputStream("Access_storage/" + fileName));
                out.write(filenameBytes);
                System.out.println(" блок откр доступ ");
            }
        }

        if (message.startsWith(CLOSE_ASSESS)) {
            Files.deleteIfExists(Paths.get("Access_storage/"+ fileName));
            System.out.println("Блок закрыть доступ!");
        }




    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        System.out.println(" ошибки при передаче в 2 хендлере");
    }


}

