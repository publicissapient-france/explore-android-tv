package fr.xebia.tv.app;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class TvWebSocketServer extends WebSocketServer {

    private OnMessageListener onMessageListener;


    public TvWebSocketServer(InetSocketAddress address) {
        super(address);
    }



    @Override public void onOpen(WebSocket conn, ClientHandshake handshake) {

    }

    @Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override public void onMessage(WebSocket conn, String message) {
        if (onMessageListener != null) {
            onMessageListener.onMessage(message);
        }
        conn.send("Hey");
    }

    @Override public void onError(WebSocket conn, Exception ex) {

    }

    public interface OnMessageListener {
        void onMessage(String message);
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

}
