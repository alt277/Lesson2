package com.flamexander.netty.example.server;

import com.flamexander.netty.example.client.ByteNetwork;
import com.flamexander.netty.example.client.ClientSender;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;


public class Handler2 extends ChannelInboundHandlerAdapter {

    private BufferedOutputStream out;

    private  BufferedInputStream in;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Byte [] arr= ( Byte[]) msg;
        String FileName=(String)msg;
        if (Files.exists(Paths.get("server_storage/" +FileName ))) {
            Sender.sendFile(Paths.get("server_storage/"+FileName),
                    ByteNetwork.getInstance().getCurrentChannel(), future -> {

                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
//                Network.getInstance().stop();
                        }
                        if (future.isSuccess()) {
                            System.out.println(" Файл отослан с сервера " +FileName);
//                Network.getInstance().stop();
                        }
                    });


            System.out.println(" Hnadler2 : file:"+FileName+" отослан ");

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        System.out.println(" ошибки при передаче в 2 хендлере");
    }


}

