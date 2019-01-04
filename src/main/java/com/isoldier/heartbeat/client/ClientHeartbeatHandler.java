package com.isoldier.heartbeat.client;

import com.isoldier.heartbeat.bean.PacketData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import static com.isoldier.heartbeat.bean.PacketData.Packet.newBuilder;

/**
 * @author jinmeng on 2019/1/4.
 * @version 1.0
 */


public class ClientHeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--- Server is active ---");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--- Server is inactive ---");

        // 5s 之后尝试重新连接服务器
        System.out.println("5s 之后尝试重新连接服务器...");
        Thread.sleep(5 * 1000);
        Client.doConnect();
    }



    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 不管是读事件空闲还是写事件空闲都向服务器发送心跳包
            sendHeartbeatPacket(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("连接出现异常");
    }

    /**
     * 发送心跳包
     *
     * @param ctx
     */
    private void sendHeartbeatPacket(ChannelHandlerContext ctx) {
        PacketData.Packet.Builder builder = newBuilder();
        builder.setPacketType(PacketData.Packet.PacketType.HEARTBEAT);
        PacketData.Packet packet = builder.build();
        ctx.writeAndFlush(packet);
    }
}