package netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import netty.config.Config;
import netty.message.Message;
import netty.serialize.Serialize;

import java.util.List;

/**
 * 魔数：用来在第一时间判定接收的数据是否为无效数据包
 * 版本号：可以支持协议的升级
 * 序列化算法：消息正文到底采用哪种序列化反序列化方式
 * 如：json、protobuf、hessian、jdk
 * 指令类型：是登录、注册、单聊、群聊… 跟业务相关
 * 请求序号：为了双工通信，提供异步能力
 * 正文长度
 * 消息正文
 */
@ChannelHandler.Sharable
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {

    /**
     *  魔数
     */
    private static final byte[] MAGIC_NUMBER = new byte[]{'C', 'A', 'F', 'E'};

    /**
     *  版本
     */
    private static final Byte VERSION = 1;

    /**
     *  序列化方式
     */
    private static final Byte SER_METHOD = 1;

    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(MAGIC_NUMBER);
        buffer.writeByte(VERSION);
        buffer.writeByte(Config.getSerializeAlgorithm().ordinal());
        buffer.writeByte(msg.getMessageType());
        buffer.writeInt(msg.getSequenceId());
        // 补齐16位
        buffer.writeByte(0xff);

        byte[] msgBytes = Config.getSerializeAlgorithm().serialize(msg);
        buffer.writeInt(msgBytes.length);
        buffer.writeBytes(msgBytes);
        out.add(buffer);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 获取魔数
        int magic = in.readInt();
        // 获取版本号
        byte version = in.readByte();
        // 获得序列化方式
        byte seqType = in.readByte();
        // 获得指令类型
        byte messageType = in.readByte();
        // 获得请求序号
        int sequenceId = in.readInt();
        // 移除补齐字节
        in.readByte();
        // 获得正文长度
        int length = in.readInt();
        // 获得正文
        byte[] msgBytes = new byte[length];
        in.readBytes(msgBytes, 0, length);

        Serialize.Algorithm algorithm = Serialize.Algorithm.values()[seqType];
        Class<? extends Message> messageClass = Message.getMessageClass(messageType);
        Message message = algorithm.deserialize(messageClass, msgBytes);
        // 将信息放入List中，传递给下一个handler
        out.add(message);
    }
}
