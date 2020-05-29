package com.flamexander.netty.example.server;

import com.flamexander.netty.example.client.ByteNetwork;
import com.flamexander.netty.example.client.ClientSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;


public class Handler2 extends ChannelInboundHandlerAdapter {
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private static final String GET_FILE = "GET";
    private static final String DELETE_FILE = "DEL";
    private static final String OPEN_ACCESS = "OPE";
    private static final String CLOSE_ASSESS = "CLO";
    private static final String SYNCHRONIZE = "SYN";
    private static String AUTHORISE = "AUT?";

    private static final byte SYGNAL_AUTH_OK = 15;

    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] arr = (byte[]) msg;
        String message = new String(arr);
        System.out.println("message= " + message);
        String[] parts = message.split("\\?");
        String fileName = "";
        for (int i = 1; i < parts.length; i++) {
            fileName += parts[i];
        }


        System.out.println("Имя файла после удал. комманды:" + fileName);
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
            Files.deleteIfExists(Paths.get("server_storage/" + fileName));
            Files.deleteIfExists(Paths.get("Access_storage/" + fileName));
            System.out.println("Блок delete!");
        }
        if (message.startsWith(OPEN_ACCESS)) {
            System.out.println(" блок откр доступ ");
            if (Files.exists(Paths.get("server_storage/" + fileName))) {

                int size = (int) Files.size(Paths.get("server_storage/" + fileName));
                byte[] arr1 = new byte[size];
                in = new BufferedInputStream(new FileInputStream("server_storage/" + fileName));
                in.read(arr1);
                out = new BufferedOutputStream(new FileOutputStream("Access_storage/" + fileName));
                out.write(arr1);
                in.close();
                out.close();

                //     Files.write(Paths.get("server_storage/" + fileName)), ;
                //    FileRegion region = new DefaultFileRegion(Paths.get("server_storage/" + fileName), 0, Files.size(path));
                //   out.write(region);
                System.out.println(" блок откр доступ ");
            }
        }

        if (message.startsWith(CLOSE_ASSESS)) {
            System.out.println(" блок закр доступ ");
            if (Files.exists(Paths.get("Access_storage/" + fileName))) {
                Files.delete(Paths.get("Access_storage/" + fileName));
                System.out.println(" блок закр доступ ");
            }
            Files.deleteIfExists(Paths.get("Access_storage/" + fileName));
        }

        if (message.startsWith(AUTHORISE)) {
            authService = new BaseAuthService();
            System.out.println("сообщение с логином и паролем =  " + message);
            String[] mass = message.split("\\?");

            String nick =
                    authService.getNickByLoginPass(mass[1], mass[2]);
            if (nick != null) {

                Sender.sendOK(ctx.channel());
                System.out.println("Успешная авторизация клиента " + nick);
            }
        } else {
            System.out.println("Неверные логин/пароль");
        }

    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        System.out.println(" ошибки при передаче в 2 хендлере");
    }


}

