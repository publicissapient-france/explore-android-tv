/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package fr.xebia.tv.app;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity {

    private ServerSocket mServerSocket;
    private int mLocalPort;
    private NsdManager mNsdManager;
    private String mServiceName;

    private NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {
        @Override public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

        }

        @Override public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {

        }

        @Override public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            mServiceName = serviceInfo.getServiceName();
        }

        @Override public void onServiceUnregistered(NsdServiceInfo serviceInfo) {

        }
    };

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override public void run() {
                // Initialize a server socket on the next available port.
                try {
                    mServerSocket = new ServerSocket(0);
                    // Store the chosen port.
                    mLocalPort = mServerSocket.getLocalPort();
                    registerService(mLocalPort);

                    //Server is running always. This is done using this while(true) loop
                    while (true) {
                        //Reading the message from the client
                        Socket socket = mServerSocket.accept();
                        InputStream is = socket.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        final String text = br.readLine();
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                ((TextView) findViewById(R.id.main_browse_fragment)).setText(text);
                            }
                        });

                    }
                } catch (Exception e) {
                    // TODO
                }
            }
        }).start();

    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("NsdTvDiscoveryTest");
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        mNsdManager.unregisterService(mRegistrationListener);
    }
}
