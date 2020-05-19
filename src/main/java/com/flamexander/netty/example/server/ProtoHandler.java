package com.flamexander.netty.example.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;

public class ProtoHandler extends ChannelInboundHandlerAdapter {
    public enum State {
        IDLE, NAME_LENGTH, NAME, FILE_LENGTH, FILE
    }
    private static final byte SIGNAL_BYTE_GET_MESSAGE=20;
    private static final byte SIGNAL_BYTE_FILE=25;


    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        byte readed = buf.readByte();
        if (readed ==SIGNAL_BYTE_FILE )
            Filer.writeFile(buf);

        if (readed == SIGNAL_BYTE_GET_MESSAGE) {

            Commander.sendFile(buf);
        }
//        if (buf.readableBytes() == 0) {
//
//            buf.release();                       //   и чистим  буфер
//        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


}
