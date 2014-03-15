package com.michglass.glasshouse.glasshouse;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Oliver
 * Date: 02/19/2014
 */
public class ConnectedThread extends Thread {

    // Debug
    private static final String TAG = "Connected Thread";

    // Handler that sends messages to activity
    private Handler mHandler;

    // Socket and Input Stream
    private final BluetoothSocket mmBTSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    /**
     * Constructor
     * Set up Socket
     * Set up Handler
     * Get I/O streams from socket
     */
    public ConnectedThread(BluetoothSocket btSocket, Handler handler) {
        Log.v(TAG, "Constructor");

        // set up socket and Instream
        mmBTSocket = btSocket;
        InputStream tempIn = null;
        OutputStream tempOut = null;

        // set up handler
        mHandler = handler;

        try {
            Log.v(TAG, "Try getting I/O Streams");
            tempIn = btSocket.getInputStream();
            tempOut = btSocket.getOutputStream();
        } catch (IOException ioE) {
            Log.e(TAG, "Failed getting I/O Streams", ioE);
        }

        // success getting I/O streams
        mmInStream = tempIn;
        mmOutStream = tempOut;
    }
    /**
     * Run
     * Listen to Input from Android Phone
     * Send Input to Main Activity
     */
    public void run() {
        Log.v(TAG, "Run");

        byte[] inBuffer = new byte[1024]; // input buffer that will store the msg form Android
        int bytes; // bytes returned from mmInStream.read

        // listen to incoming data until exception occurs
        //TODO break while loop when Android app stops or this app gets shut down
        while(true) {
            Log.v(TAG, "Loop ConnectedThread");
            try {
                bytes = mmInStream.read(inBuffer);

                // convert byte array to int
                ByteBuffer wrapper = ByteBuffer.wrap(inBuffer);
                int inMessage = wrapper.getInt();
                Log.v(TAG, "InputStream: " + inMessage);

                // Handler sends message to Activity
                sendCommand(inMessage);

            } catch (IOException ioE) {
                Log.e(TAG, "Failed reading inStream", ioE);
                break;
            }
        }
        Log.v(TAG, "Run Return");
    }
    /**
     * Write
     * Write a message to Output Stream and send it to Android Device
     * @param msg Message that gets send to Android
     */
    public void write(int msg) {
        Log.v(TAG, "Write Out");
        byte[] buffer = ByteBuffer.allocate(4).putInt(msg).array();

        try {
            mmOutStream.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Failed writing to Android", e);
        }
    }
    /**
     * Cancel
     * Close the socket
     */
    public void cancel() {
        try {
            Log.v(TAG, "Try closing Socket");
            mmBTSocket.close();
        } catch (IOException ioE) {
            Log.e(TAG, "Failed closing connection");
        }
    }

    /**
     * Send a command as message to activity
     * Those are the input for Glass UI
     * Basic commands (see main activity): Move in Timeline, Select Card, Go Back
     * @param commandMsg Command as message to activity
     */
    public void sendCommand(int commandMsg) {

        Message msg = new Message();
        msg.what = commandMsg;
        mHandler.sendMessage(msg);
    }
    /**
     * For Debug, send string message to activity
     * and display on card
     * @param stringMsg Message to be displayed on Card
     */
    public void sendStringMessage(String stringMsg){

        Message msg = new Message();
        msg.what = BluetoothService.MESSAGE_INCOMING;
        Bundle data = new Bundle();
        data.putString(BluetoothService.EXTRA_MESSAGE, stringMsg);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }
}
