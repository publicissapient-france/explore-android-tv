package fr.xebia.tv.discovery;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.InetAddress;
import java.net.URI;

public class TvWebSocketClient extends WebSocketClient {


    public TvWebSocketClient(InetAddress inetAddress, int port) {
        super(URI.create("ws://" + inetAddress.getHostAddress() + ":" + port), new Draft_17());
    }

    @Override public void onOpen(ServerHandshake handshakedata) {

    }

    @Override public void onMessage(String message) {

    }

    @Override public void onClose(int code, String reason, boolean remote) {

    }

    @Override public void onError(Exception ex) {

    }
}
