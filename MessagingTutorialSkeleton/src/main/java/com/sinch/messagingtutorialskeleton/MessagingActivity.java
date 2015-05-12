package com.sinch.messagingtutorialskeleton;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.messagingtutorialskeleton.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Andy on 16/03/2015.
 */
public class MessagingActivity extends Activity {



    private String recipientId;
    private EditText messageBodyField;
    private String messageBody;
    private MessageService.MessageServiceInterface messageService;
    private MessageAdapter messageAdapter;
    private ListView messagesList;
    private String currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MessageClientListener messageClientListener = new MyMessageClientListener();
    private Intent callIntent;


    //MediaPlayer mp = MediaPlayer.create(this, R.raw.alert_tone_1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);


        //get recipientId from the intent
        Intent intent = getIntent();
        recipientId = intent.getStringExtra("RECIPIENT_ID");
        currentUserId = ParseUser.getCurrentUser().getObjectId();

        //create instance of messageAdapter
        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);

        //call the populateMessageHistory class
        populateMessageHistory();

        //create the intent to start the CallActivity
        callIntent = new Intent(getApplicationContext(), CallActivity.class);
        callIntent.putExtra("callerId", currentUserId);
        callIntent.putExtra("recipientId", recipientId);

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        //listen for click on the send button
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send the message
                sendMessage();
            }
        });

        findViewById(R.id.callButton).setOnClickListener(new View.OnClickListener(){
            @Override
        public void onClick (View view){
                startActivity(callIntent);
            }
        });

    }

    private void populateMessageHistory() {
        String[] userIds = {currentUserId, recipientId};
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereContainedIn("senderId", Arrays.asList(userIds));
        query.whereContainedIn("recipientId", Arrays.asList(userIds));
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messageList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        WritableMessage message = new WritableMessage(messageList.get(i).get("recipientId").toString(), messageList.get(i).get("messageText").toString());
                        if (messageList.get(i).get("senderId").toString().equals(currentUserId)) {
                            if (messageList.get(i).containsKey("messageSeen")) {
                                messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, messageList.get(i).get("messageSeen").toString());
                            }else{
                                messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, "false");
                            }

                        } else {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING, "");
                        }
                    }
                }
            }
        });
    }
    public void sendMessage() {
        messageBody = messageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter a message", Toast.LENGTH_LONG).show();
            return;
        }

        messageService.sendMessage(recipientId, messageBody);
        messageBodyField.setText("");
    }


    //unbind the service when the activity is destroyed
    @Override
    public void onDestroy() {
        messageService.removeMessageClientListener(messageClientListener);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private class MyServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder){
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName){
            messageService = null;
        }
    }

    public class MyMessageClientListener implements MessageClientListener {

      

        @Override
        public void onMessageFailed(MessageClient client, Message message, MessageFailureInfo failureInfo){
            Toast.makeText(MessagingActivity.this, "Message failed to send", Toast.LENGTH_LONG).show();
            Log.d("Message Failure Info", failureInfo.getSinchError().toString());
            Log.d("Message Failure Recip", failureInfo.getRecipientId());
        }

        @Override
        public void onIncomingMessage(MessageClient client, final Message message){
            //display an incoming message
            if (message.getSenderId().equals(recipientId)){
                WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING, "");

                //get latest message sent in parse and update seen to true
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
                query.whereEqualTo("senderId", message.getSenderId());
                query.whereEqualTo("recipientId", message.getRecipientIds().get(0));
                query.addDescendingOrder("createdAt");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> messagelist, ParseException e) {
                        if (messagelist.size() != 0) {
                            ParseQuery<ParseObject> seenQuery = ParseQuery.getQuery("ParseMessage");
                            seenQuery.getInBackground(messagelist.get(0).getObjectId(), new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject messageObject, ParseException e) {
                                    if (e == null) {
                                        messageObject.put("messageSeen", true);
                                        messageObject.saveInBackground();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {
            //Display message that was just sent and store in Parse
            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            //only add message to parse if it doesnt already exist in the parse database (stop duplicates)
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            query.whereEqualTo("sinchId", message.getMessageId());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> messageList, ParseException e) {
                    if (e == null) {
                        if (messageList.size() == 0) {
                            ParseObject parseMessage = new ParseObject("ParseMessage");
                            parseMessage.put("senderId", currentUserId);
                            parseMessage.put("recipientId", writableMessage.getRecipientIds().get(0));
                            parseMessage.put("messageText", writableMessage.getTextBody());
                            parseMessage.put("sinchId", writableMessage.getMessageId());
                            parseMessage.saveInBackground();

                            messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING, "");
                        }
                    }
                }
            });

        }



        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo){

        }

        //set up for push notifications
        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs){

            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());

            ParseQuery userQuery = ParseUser.getQuery();
            userQuery.whereEqualTo("objectId", writableMessage.getRecipientIds().get(0));

            ParseQuery  pushQuery = ParseInstallation.getQuery();
            pushQuery.whereMatchesQuery("user", userQuery);

            //send push notification to query
            ParsePush push = new ParsePush();
            //set installation query
            push.setQuery(pushQuery);
            push.setMessage("You got a new message");
            push.sendInBackground();

        }


    }

}
