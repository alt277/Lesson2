package com.flamexander.netty.example.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

public class OutHandler extends ChannelOutboundHandlerAdapter {


//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//      String str=(String)msg;
//      byte[] arr=str.getBytes();
//        ByteBuf buf=ctx.alloc().buffer(arr.length);
//        buf.writeBytes(arr);
//        ctx.writeAndFlush(buf);
//  }
}
