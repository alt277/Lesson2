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

    private BufferedOutputStream out;

//    private  BufferedInputStream in;
//   private Path path;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte [] arr= ( byte[]) msg;
        String FileName=new String (arr);
//
//        Channel channel=ctx.channel();
//        path = Paths.get ("server_storage/" +FileName );
//
//        if (Files.exists(path)) {
//            FileRegion region = new DefaultFileRegion(path.toFile(), 0, Files.size(path));
//
//            ByteBuf buf = null;
//            buf = ByteBufAllocator.DEFAULT.directBuffer(1);
//            buf.writeByte((byte) 25);
//            channel.writeAndFlush(buf);
//
//            byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
//            buf = ByteBufAllocator.DEFAULT.directBuffer(4);
//            buf.writeInt(filenameBytes.length);
//            channel.writeAndFlush(buf);
//
//            buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
//            buf.writeBytes(filenameBytes);
//            channel.writeAndFlush(buf);
//
//            buf = ByteBufAllocator.DEFAULT.directBuffer(8);
//            buf.writeLong(Files.size(path));
//            channel.writeAndFlush(buf);
//
//             channel.writeAndFlush(region);
//        }
        if (Files.exists(Paths.get("server_storage/"+FileName))) {
            Sender.sendFile(Paths.get("server_storage/" + FileName),
                    ctx.channel(), future -> {

                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();

                        }
                        if (future.isSuccess()) {
                            System.out.println(" Файл отослан с сервера " + FileName);

                        }
                    });
            System.out.println(" Handler2 : file:" + FileName + " отослан ");

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        System.out.println(" ошибки при передаче в 2 хендлере");
    }


}

