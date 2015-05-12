package com.sinch.messagingtutorialskeleton;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.messagingtutorialskeleton.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sinch.android.rtc.messaging.WritableMessage;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Andy on 17/03/2015.
 */
public class MessageAdapter extends BaseAdapter {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private List<Triplet<WritableMessage, Integer, String>> messages;
    private LayoutInflater layoutInflater;

    public MessageAdapter(Activity activity){
        layoutInflater = activity.getLayoutInflater();
        messages = new ArrayList<Triplet<WritableMessage, Integer, String>>();

    }

    public void addMessage(WritableMessage message, int direction, String messageSeen){
        messages.add(new Triplet(message, direction, messageSeen));
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return messages.size();
    }

    @Override
    public Object getItem(int i){
        return messages.get(i);
    }

    @Override
    public long getItemId(int i){
        return i;
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    @Override
    public int getItemViewType(int i){
        return  messages.get(i).second;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup){
        int direction = getItemViewType(i);


        //show message on left or right depending on if incoming or outgoing
        if (convertView == null){
            int res = 0;
            if (direction == DIRECTION_INCOMING){
                res = R.layout.message_right;
            }else if (direction == DIRECTION_OUTGOING){
                res = R.layout.message_left;
            }
            convertView = layoutInflater.inflate(res, viewGroup, false);
        }

        WritableMessage message = messages.get(i).first;

        TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
        txtMessage.setText(message.getTextBody());

        if (direction == DIRECTION_OUTGOING){

            String messageSeen = messages.get(i).third;
            String messageSeenTxt;
            if (messageSeen == "true"){
                messageSeenTxt = "Seen";
            }else{
                messageSeenTxt = "";
            }

            TextView txtSent = (TextView) convertView.findViewById(R.id.txtSender);
            txtSent.setText(messageSeenTxt);


           /* ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            query.whereEqualTo("senderId", ParseUser.getCurrentUser().getObjectId());
            query.whereEqualTo("recipientId", message.getRecipientIds().get(0));
            query.addDescendingOrder("createdAt");
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> messagelist, ParseException e) {
                    if (messagelist.size() != 0) {

                        Log.d("MessageAdapter", String.valueOf(messagelist.size()));

                        if (messagelist.get(0).get("messageSeen").toString() == "true") {


                            txtSent.setText("Seen");
                        }

                    } else {
                        Log.d("MessageAdapter", "Query Failed");
                    }
                }
            });*/
        }


        return convertView;
    }

}
