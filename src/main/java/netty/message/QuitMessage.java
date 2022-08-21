package netty.message;

public class QuitMessage extends Message{
    @Override
    public int getMessageType() {
        return QuitMessage;
    }
}
