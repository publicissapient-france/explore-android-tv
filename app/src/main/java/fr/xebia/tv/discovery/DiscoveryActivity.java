package fr.xebia.tv.discovery;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.java_websocket.client.WebSocketClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import timber.log.Timber;


public class DiscoveryActivity extends Activity {

    private static final String TAG = "DiscoveryActivity";

    @InjectView(R.id.available_tvs) ListView availableTvsListView;

    List<String> availableTvs = new ArrayList<>();
    ArrayAdapter<String> availableTvsAdapter;
    private NsdManager.ResolveListener mResolveListener;
    private NsdServiceInfo mService;
    private NsdManager nsdManager;
    private List<NsdServiceInfo> availableTvsInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discovery_activity);
        ButterKnife.inject(this);
        availableTvsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, availableTvs);
        availableTvsListView.setAdapter(availableTvsAdapter);
        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener() {
            @Override public void onStartDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override public void onStopDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override public void onDiscoveryStarted(String serviceType) {

            }

            @Override public void onDiscoveryStopped(String serviceType) {

            }

            @Override public void onServiceFound(final NsdServiceInfo serviceInfo) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (serviceInfo.getServiceName().startsWith("NsdTvDiscovery")) {
                            availableTvs.add(serviceInfo.getServiceName());
                            availableTvsInfo.add(serviceInfo);
                            availableTvsAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override public void onServiceLost(NsdServiceInfo serviceInfo) {

            }
        });
    }

    @OnItemClick(R.id.available_tvs) public void onTvSelected(final int position) {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(final NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                mService = serviceInfo;
                final int port = mService.getPort();
                final InetAddress host = mService.getHost();
                new Thread(new Runnable() {
                    @Override public void run() {
                        try {
                            WebSocketClient webSocketClient = new TvWebSocketClient(host, port);
                            if (webSocketClient.connectBlocking()) {
                                webSocketClient.send("Coucou");
                            }
                        } catch (Exception e) {
                            // TODO
                            Timber.e(e, "Uh oh!");
                        }
                    }
                }).start();
            }
        };
        nsdManager.resolveService(availableTvsInfo.get(position), mResolveListener);
    }
}
