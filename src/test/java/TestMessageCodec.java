import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import netty.message.LoginRequestMessage;
import netty.message.QuitMessage;
import netty.protocol.MessageCodec;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestMessageCodec {
    @Test
    public void testEncodeDecode() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 12, 4, 0, 0));
        embeddedChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
        embeddedChannel.pipeline().addLast(new MessageCodec());

        // 测试编码
        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("wjl", "wjl123");
//        embeddedChannel.writeOutbound(loginRequestMessage);

        // 测试解码
        List<Object> list = new ArrayList<>();
        messageCodec.encode(null, loginRequestMessage, list);
        ByteBuf byteBuf = (ByteBuf) list.get(0);
        byteBuf.retain();
        embeddedChannel.writeInbound(byteBuf);
        log.debug("success");
    }

    @Test
    public void testEncode() {
        LoggingHandler Log = new LoggingHandler();
        MessageCodec codec = new MessageCodec();
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(Log);
        channel.pipeline().addLast(codec);
        channel.pipeline().addLast(Log);

        channel.writeOutbound(new QuitMessage());
    }
}
