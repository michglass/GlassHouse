package com.michglass.glasshouse.glasshouse;

/**
 * Created by vbganesh on 3/25/14.
 */
public class BluetoothSMS {
    private String phoneNumber;
    private String Message;
    public void setNum(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    public void setMessage(String Message){
        this.Message = Message;
    }
    public String buildBluetoothSMS(){
        return new String("comm;" + phoneNumber + ";" + Message);
    }
}
