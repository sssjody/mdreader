package netty.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtocolLengthDecoder extends LengthFieldBasedFrameDecoder {

    public ProtocolLengthDecoder() {
        this(Integer.MAX_VALUE,12, 4, 0, 0);
    }

    public ProtocolLengthDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
