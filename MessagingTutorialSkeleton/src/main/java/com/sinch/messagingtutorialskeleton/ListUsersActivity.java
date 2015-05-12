package com.sinch.messagingtutorialskeleton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.messagingtutorialskeleton.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sinch.android.rtc.Sinch;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Andy on 14/03/2015.
 */
public class ListUsersActivity extends Activity {


    private ListViewAdapter listViewAdapter;
    private ListView usersListView;
    private Button logoutButton;
    private Spinner status_spinner;
    private String[] arraySpinner;
    private ProgressDialog progressDialog;
    private BroadcastReceiver receiver = null;
    private Timer timer = new Timer();
    private TimerTask timerTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listusers);

        // Save the current Installation to Parse.
        ParseInstallation.getCurrentInstallation().saveInBackground();

        // Associate the device with a user
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", ParseUser.getCurrentUser());
        installation.saveInBackground();

        //display the loading spinner for starting sinch
        showSpinner();

        // set up logout button for an onlclick listener, log the user out of parse and return to the login activity
        logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(getApplicationContext(), MessageService.class));
                ParseUser.logOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        //create options array for spinner
        this.arraySpinner = new String[]{
                "online", "offline", "busy"
        };

        //display spinner on screen and attach array
        status_spinner = (Spinner) findViewById(R.id.status_spinner);
        final ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arraySpinner);
        status_spinner.setAdapter(spinnerAdapter);

        //set the selected value of the spinner to the users status in Parse
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> user_status, com.parse.ParseException e) {
                if (e == null) {
                    //set the selection on the spinner to the value of the current parse users status
                    status_spinner.setSelection(spinnerAdapter.getPosition(user_status.get(0).get("status").toString()));
                } else {
                    Toast.makeText(getApplicationContext(), "Error loading default spinner value", Toast.LENGTH_LONG).show();
                }

            }
        });

        //Create onclick listener for spinner and save new status to parse when the item is selected
        status_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ParseUser user = ParseUser.getCurrentUser();
                user.put("status", parent.getItemAtPosition(position));

                if (parent.getItemAtPosition(position) == "online"){
                    user.put("status_color", "#000000");
                } else if (parent.getItemAtPosition(position) == "offline"){
                    user.put("status_color", "#a9a9a9");
                } else if (parent.getItemAtPosition(position) == "busy"){
                    user.put("status_color", "#cb333b");
                }

                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {

                        } else {
                            Toast.makeText(getApplicationContext(), "There was a problem updating your status", Toast.LENGTH_LONG).show();
                        }
                    }

                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

        //set clickable list of users
        private void setConversationsList(){

            //Initialize the subclass of PQM
            listViewAdapter = new ListViewAdapter(this);

            //Initialize ListView and set initial view to listadapter
            usersListView = (ListView) findViewById(R.id.usersListView);
            usersListView.setAdapter(listViewAdapter);





            usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                    openConversation(listViewAdapter.getItem(i));
                }
            });

        }


    //open conversation with one person
    public void openConversation(ParseObject name) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", name.getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> user, com.parse.ParseException e) {
                if (e == null) {
                    Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
                    intent.putExtra("RECIPIENT_ID", user.get(0).getObjectId());
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Error finding that user", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void showSpinner(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        //create broadcast reciever to listen for the broadcast from MessageService
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("Success", false);
                progressDialog.dismiss();

                //show message if the sinch service failed to start
                if (!success){
                    Toast.makeText(getApplicationContext(), "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("com.sinch.messagingtutorialskeleton.ListUsersActivity"));
    }

    @Override
    public void onPause(){
        super.onPause();
        timer.cancel();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onResume() {
        setConversationsList();
        super.onResume();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                listViewAdapter.loadObjects();
            }
        };
        timer.schedule(timerTask, 30000, 30000);

    }
}
