package com.sinch.messagingtutorialskeleton;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;


/**
 * Created by Andy on 15/03/2015.
 */
public class MessageService extends Service implements SinchClientListener {

    private static final String APP_KEY = "de12cd69-0057-4f82-97f2-e55c447fcb10";
    private static final String APP_SECRET = "rQk2xxokB0O3P+yO/hY/jQ==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
    private SinchClient sinchClient = null;
    private MessageClient messageClient = null;
    private CallClient callClient = null;
    private String currentUserId;
    private LocalBroadcastManager broadcaster;
    private Intent broadcastIntent = new Intent("com.sinch.messagingtutorialskeleton.ListUsersActivity");

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //get the current user id from Parse
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        if (currentUserId != null && !isSinchClientStarted()){
            startSinchClient(currentUserId);
        }

        broadcaster = LocalBroadcastManager.getInstance(this);

        return super.onStartCommand(intent, flags, startId);
    }

    public void startSinchClient(String username){
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(username)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        //Client listener requires below methods
        sinchClient.addSinchClientListener(this);

        //messaging and call are turned on
        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportCalling(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.setSupportPushNotifications(true);

        sinchClient.checkManifest();
        sinchClient.registerPushNotificationData("6034618621".getBytes());
        sinchClient.start();
        sinchClient.registerPushNotificationData("6034618621".getBytes());
    }

    private boolean isSinchClientStarted() {
        return sinchClient != null && sinchClient.isStarted();
    }

    //next 5 methods are for the sinch client listener
    @Override
    public void onClientFailed(SinchClient client, SinchError error){

        broadcastIntent.putExtra("Success", false);
        broadcaster.sendBroadcast(broadcastIntent);

        sinchClient = null;
    }

    @Override
    public void onClientStarted(SinchClient client){
        broadcastIntent.putExtra("Success", true);
        broadcaster.sendBroadcast(broadcastIntent);

        client.startListeningOnActiveConnection();
        messageClient = client.getMessageClient();
        callClient = client.getCallClient();
    }

    @Override
    public void onClientStopped(SinchClient client){

        sinchClient = null;
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration clientRegistration){

    }

    @Override
    public void onLogMessage(int level, String area, String message){

    }

    @Override
    public IBinder onBind(Intent intent){
        return serviceInterface;
    }

    public void sendMessage(String recipientUserId, String textBody){
        if (messageClient != null){
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            messageClient.send(message);
        }
    }

    public void addMessageClientListener(MessageClientListener listener){
        if (messageClient != null){
            messageClient.addMessageClientListener(listener);
        }
    }

    public void removeMessageClientListener(MessageClientListener listener){
        if (messageClient != null){
            messageClient.removeMessageClientListener(listener);
        }
    }

    public void startCall(String recipientUserId) {
        if (callClient != null) {
           callClient.callUser(recipientUserId);
        }
    }

    public void addCallClientListener(CallClientListener listener){
        if (callClient != null){
            callClient.addCallClientListener(listener);
        }
    }

    public void removeCallClientListener(CallClientListener listener){
        if (callClient != null){
            callClient.removeCallClientListener(listener);
        }
    }


    @Override
    public void onDestroy(){
        sinchClient.stopListeningOnActiveConnection();
        sinchClient.terminate();
    }


    //public interface for ListUsersActivity, MessagingActivity and CallActivity
    public class MessageServiceInterface extends Binder {

        public void sendMessage(String recipientUserId, String textBody){
            MessageService.this.sendMessage(recipientUserId, textBody);
        }

        public void addMessageClientListener(MessageClientListener listener){
            MessageService.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener){
            MessageService.this.removeMessageClientListener(listener);
        }

        public void startCall (String recipientUserId){
            MessageService.this.startCall(recipientUserId);
        }

        public void addCallClientListener (CallClientListener listener){
            MessageService.this.addCallClientListener(listener);
        }

        public void removeCallClientListener (CallClientListener listener){
            MessageService.this.removeCallClientListener(listener);
        }

        public boolean isSinchClientStarted(){
            return MessageService.this.isSinchClientStarted();
        }
    }

}
