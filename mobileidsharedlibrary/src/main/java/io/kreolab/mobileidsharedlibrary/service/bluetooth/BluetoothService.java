/*
 * Copyright (c) 2018.  mobileidsharedlibrary project
 *
 */

package io.kreolab.mobileidsharedlibrary.service.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import io.kreolab.mobileidsharedlibrary.util.Config;
import timber.log.Timber;


public class BluetoothService {
    static private BluetoothAdapter sBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    static State sState = State.NO_CONNECTED;
    static BluetoothSocket sBluetoothSocket;
    static InputStream sSocketInputStream = null;
    static OutputStream sSocketOutputStream  = null;
    static WeakReference<Handler> weakReferenceToUiHandler;

    public static boolean isSupported() {
        boolean isSupported = false;
        if (sBluetoothAdapter != null) {
            isSupported = true;
        }
        return isSupported;
    }

    public static boolean isEnabled() {
        boolean isEnabled = false;
        if (isSupported() && sBluetoothAdapter.isEnabled()) {
            isEnabled = true;
        }
        return isEnabled;
    }

    public static List<BluetoothDevice> getPairedDevices() {
        return new ArrayList<>(sBluetoothAdapter.getBondedDevices());
    }

    public static void startDiscovery() {

        sBluetoothAdapter.startDiscovery();

    }


    public static void startServer() {

        new Thread(new ServerSetupRunnable()).start();

    }


    public static void clientConnect(BluetoothDevice device, Handler handler) {
        new Thread(new ClientConnectRunnable(device,handler)).start();
    }

    public static void cancelDiscovery() {
        sBluetoothAdapter.cancelDiscovery();
    }

//    public static boolean isDiscovering() {
//        return sBluetoothAdapter.isDiscovering();
//    }

    public  enum State{
            NO_CONNECTED,SERVER_LISTENING,CLIENT_CONNECTING, CONNECTED
    }

    private static void  manageSocketConnection(BluetoothSocket socket){
        new Thread(new ManageConnectionRunnable(socket)).start();
    }


    public static void sendMessage(String message)
    {
        try {
            sSocketOutputStream.write(message.getBytes());
            sSocketOutputStream.close();
        } catch (IOException e) {
            Timber.e(e,"XXX could not write to socket");
        }
    }




    private static void closeSocket()
    {
        if(sBluetoothSocket != null) {
            try {
                sBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ServerSetupRunnable implements Runnable {

        private final BluetoothServerSocket mServerSocket;

        private ServerSetupRunnable() {
            BluetoothServerSocket tempSocket = null;
            try {
                tempSocket = sBluetoothAdapter.listenUsingRfcommWithServiceRecord("MobileID Service", UUID.fromString(Config.SERVICE_UUID));
                //state is listening
                Timber.d("XXXX RFCOMM opened");
            } catch (IOException ex) {
                Timber.d("XXXX RFCOMM could not be opened");
            }
            mServerSocket = tempSocket;

        }

        @Override
        public void run() {
              //wait till connected or exception thrown
            try {
                sState = State.SERVER_LISTENING;
                sBluetoothSocket = mServerSocket.accept();
                //close server socket once socket accepted
                mServerSocket.close();
            } catch (IOException e) {
               Timber.e(e,"XXX Serversocket could not be opened");
            }
            if (sBluetoothSocket != null) {
                Timber.d("XXXX  Remote connection accepted");
                sState = State.CONNECTED;
                manageSocketConnection(sBluetoothSocket);
            }
            else {
                Timber.d("XXXX  Remote connection not accepted");
            }
        }
    }


    private static class ClientConnectRunnable implements Runnable {

        BluetoothDevice mRemoteDevice;
        final BluetoothSocket mSocket;


        ClientConnectRunnable(BluetoothDevice device,Handler handler) {
            mRemoteDevice = device;
            weakReferenceToUiHandler = new WeakReference<>(handler);
            BluetoothSocket tempSocket = null;

            try {
                tempSocket = mRemoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(Config.SERVICE_UUID));
            } catch (IOException e) {
                Timber.e("XXXX RFCOMM create error: " + e.getMessage());
            }
            mSocket = tempSocket;
        }

        @Override
        public void run() {
            //for performance reasons, cancel discovery before IO on socket
            cancelDiscovery();

            if (mSocket != null) {
                try {
                    mSocket.connect();
                } catch (IOException e) {

                    Timber.e("XXXX Client connect failed" + e.getMessage());
                }
                Timber.d("XXXX Connection success");
                sState = State.CONNECTED;
                //send notification to GUI
                if(weakReferenceToUiHandler != null && weakReferenceToUiHandler.get() != null)
                {
                    Handler handler = weakReferenceToUiHandler.get();
                    Message message = handler.obtainMessage();
                    handler.sendMessage(message);
                }
                //sent to new thread to start writing
                manageSocketConnection(mSocket);
            }
        }
    }


    private static class ManageConnectionRunnable implements Runnable {
        private final BluetoothSocket mBluetoothSocket;


        ManageConnectionRunnable(BluetoothSocket bluetoothSocket) {
            this.mBluetoothSocket = bluetoothSocket;
            InputStream tempInputStream = null;
            OutputStream tempOutputStream = null;
            try {
                tempInputStream = mBluetoothSocket.getInputStream();
                tempOutputStream = mBluetoothSocket.getOutputStream();

            } catch (IOException e) {
                Timber.e(e,"XXXX socket streams could not be opened");
            }

            sSocketInputStream = tempInputStream;
            sSocketOutputStream = tempOutputStream;

        }

        @Override
        public void run() {
            Reader reader = new InputStreamReader(sSocketInputStream);
            BufferedReader bufferReader = new BufferedReader(reader);
            String line = "";
            StringBuilder contentStringBuilder = new StringBuilder(line);
            try {
                while ((line = bufferReader.readLine()) != null) {
                    contentStringBuilder.append(line);
                }
                //post message to UI handler
                Timber.d("XXXXMessage is "+ contentStringBuilder.toString());
                bufferReader.close();
                closeSocket();
            } catch (IOException e) {
                Timber.e(e,"XXXX error reading ");
            }


        }
    }
}
