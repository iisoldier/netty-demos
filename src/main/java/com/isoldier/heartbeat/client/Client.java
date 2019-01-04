package com.isoldier.heartbeat.client;

import com.isoldier.heartbeat.bean.PacketData;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Random;

import static com.isoldier.heartbeat.bean.PacketData.Packet.newBuilder;

/**
 * @author jinmeng on 2019/1/4.
 * @version 1.0
 */
public class Client {

    private static Channel channel;
    private static Bootstrap bootstrap;


    private static Integer readIdle = 3;
    private static Integer writeIdle = 5;
    private static Integer allIdle = 10;


    public static void main(String[] args) {

        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());
                            pipeline.addLast(new IdleStateHandler(readIdle,writeIdle,allIdle));
                            pipeline.addLast(new ClientHeartbeatHandler());
                        }
                    });

            // 连接服务器
            doConnect();

            // 不定时发送向服务器发送数据包
            Random random = new Random();
            for(int i = 0; i< 100; i++ ){
                int num = random.nextInt(20);
                Thread.sleep(num * 1000);
                PacketData.Packet.Builder builder = newBuilder();
                builder.setPacketType(PacketData.Packet.PacketType.DATA);
                builder.setData("发送数据包： " + num);
                PacketData.Packet packet = builder.build();
                channel.writeAndFlush(packet);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
        }
    }

    /**
     * 抽取出该方法 (断线重连时使用)
     *
     * @throws InterruptedException
     */
    public static void doConnect() throws InterruptedException {

        channel = bootstrap.connect("127.0.0.1", 8192).sync().channel();
    }


}