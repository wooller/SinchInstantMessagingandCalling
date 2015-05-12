package com.sinch.messagingtutorialskeleton;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import com.example.messagingtutorialskeleton.R;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.messaging.MessageClientListener;

/**
 * Created by Andy on 12/05/2015.
 */
public class CallActivity extends Activity {
    private String callerId;
    private String recipientId;
    private Call call;
    private Button callButton;
    private MessageService.MessageServiceInterface messageService;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private CallClientListener callClientListener = new MyCallClientListener();
    private SinchClient sinchClient;

    @Override
     protected void onCreate(Bundle savedInstanceState) {

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        Intent callIntent = getIntent();
        callerId = callIntent.getStringExtra("callerId");
        recipientId = callIntent.getStringExtra("recipientId");

        callButton = (Button) findViewById(R.id.callButton);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (call == null){
                    call = messageService.startCall(recipientId);
                    callButton.setText("Hang Up");
                }else {
                    call.hangup();
                    call = null;
                    callButton.setText("Call");
                }
            }
        });
    }

    private class MyServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder){
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addCallClientListener(callClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName){
            messageService = null;
        }
    }

    public class MyCallClientListener implements CallClientListener {

    }
}
