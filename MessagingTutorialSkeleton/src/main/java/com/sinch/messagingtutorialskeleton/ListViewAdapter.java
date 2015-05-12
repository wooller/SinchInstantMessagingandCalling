package com.sinch.messagingtutorialskeleton;



import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.messagingtutorialskeleton.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import org.w3c.dom.Text;


/**
 * Created by Andy on 05/04/2015.
 * Adapter extends ArrayAdapter to give values to multiple text views in a list
 *
 */

public class ListViewAdapter extends ParseQueryAdapter<ParseObject>{

    public ListViewAdapter(Context context){
        //Use Query factory to constuct a PQA that will not include the current user
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>(){
           public ParseQuery create(){
               String currentUserId = ParseUser.getCurrentUser().getObjectId();
               ParseQuery query = new ParseQuery("_User");
               query.whereNotEqualTo("objectId", currentUserId);
               return query;

           }
        });
    }

    //Customise the layout by overriding getItemView
    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent){
        if (v == null){
            v = View.inflate(getContext(), R.layout.user_list_items, null);
        }

        super.getItemView(object, v, parent);

        //Add the username textview
        TextView userListItem = (TextView) v.findViewById(R.id.text1);
        userListItem.setText(object.get("username").toString());
        userListItem.setTextColor(Color.parseColor(object.get("status_color").toString()));
        //Add the status textview
        TextView userListSubItem = (TextView) v.findViewById(R.id.userListSubItem);
        userListSubItem.setText(object.get("status").toString());
        userListSubItem.setTextColor(Color.parseColor(object.get("status_color").toString()));

        return v;
    }



    }

