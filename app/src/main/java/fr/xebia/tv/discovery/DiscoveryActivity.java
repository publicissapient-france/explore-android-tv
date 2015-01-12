package fr.xebia.tv.discovery;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

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
    @InjectView(R.id.pdfview) PDFView pdfView;

    List<String> availableTvs = new ArrayList<>();
    ArrayAdapter<String> availableTvsAdapter;
    private NsdServiceInfo mService;
    private NsdManager nsdManager;
    private List<NsdServiceInfo> availableTvsInfo = new ArrayList<>();
    private WebSocketClient webSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discovery_activity);
        ButterKnife.inject(this);
        availableTvsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, availableTvs);
        availableTvsListView.setAdapter(availableTvsAdapter);
        nsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        nsdManager.discoverServices("_http._tcp.", NsdManager.PROTOCOL_DNS_SD, new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {

            }

            @Override
            public void onDiscoveryStarted(String serviceType) {

            }

            @Override
            public void onDiscoveryStopped(String serviceType) {

            }

            @Override
            public void onServiceFound(final NsdServiceInfo serviceInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (serviceInfo.getServiceName().startsWith("NsdTvDiscovery")) {
                            availableTvs.add(serviceInfo.getServiceName());
                            availableTvsInfo.add(serviceInfo);
                            availableTvsAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {

            }
        });

        pdfView.fromAsset("slides.pdf")
               .enableSwipe(true)
               .onPageChange(new OnPageChangeListener() {
                   @Override
                   public void onPageChanged(final int page, final int pageCount) {
                       new Thread(new Runnable() {
                           @Override
                           public void run() {
                               if (webSocketClient != null) {
                                   Log.i(TAG, "Sending page " + page);
                                   webSocketClient.send("page:" + page);
                               }
                           }
                       }).start();
                   }
               })
               .load();
    }

    @OnItemClick(R.id.available_tvs)
    public void onTvSelected(final int position) {
        NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {

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
                    @Override
                    public void run() {
                        try {
                            webSocketClient = new TvWebSocketClient(host, port);
                            if (webSocketClient.connectBlocking()) {
                                webSocketClient.send("Coucou");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        availableTvsListView.setVisibility(View.GONE);
                                        pdfView.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Timber.e(e, "Uh oh!");
                        }
                    }
                }).start();
            }
        };
        nsdManager.resolveService(availableTvsInfo.get(position), mResolveListener);
    }
}
