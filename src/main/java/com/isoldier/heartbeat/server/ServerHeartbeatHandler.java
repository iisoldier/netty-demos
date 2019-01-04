package com.isoldier.heartbeat.server;

import com.isoldier.heartbeat.bean.PacketData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

/**
 * @author jinmeng on 2019/1/4.
 * @version 1.0
 */

public class ServerHeartbeatHandler extends ChannelInboundHandlerAdapter {

    private volatile int counter;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("--- Client is active ---");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("--- Client is inactive ---");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 判断接收到的包类型
        if (msg instanceof PacketData.Packet) {
            PacketData.Packet packet = (PacketData.Packet) msg;

            switch (packet.getPacketType()) {

                case HEARTBEAT:
                    handleHeartbeat(ctx, packet);
                    break;

                case DATA:
                    handleData(ctx, packet);
                    break;

                default:
                    break;
            }
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 空闲3s之后触发 (心跳包丢失)
            if (counter >= 3) {
                // 连续丢失3个心跳包 (断开连接)
                ctx.channel().close().sync();
                System.out.println("已与Client断开连接");
            } else {
                counter++;
                System.out.println("丢失了第 " + counter + " 个心跳包");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        System.out.println("连接出现异常");
    }

    /**
     * 处理心跳包
     *
     * @param ctx
     * @param packet
     */
    private void handleHeartbeat(ChannelHandlerContext ctx, PacketData.Packet packet) {
        // 充值计数器
        counter = 0;
        System.out.println("收到心跳包");
        ReferenceCountUtil.release(packet);
    }

    /**
     * 处理数据包
     *
     * @param ctx
     * @param packet
     */
    private void handleData(ChannelHandlerContext ctx,PacketData.Packet packet) {
        // 充值计数器
        counter = 0;
        String data = packet.getData();
        System.out.println(data);
        ReferenceCountUtil.release(packet);
    }
}