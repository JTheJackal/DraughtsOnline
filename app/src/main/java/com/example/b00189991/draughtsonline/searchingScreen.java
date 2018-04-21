package com.example.b00189991.draughtsonline;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class searchingScreen extends Activity implements View.OnClickListener {

    ImageButton cancelBTN;
    Firebase fb;
    SharedPreferences preferences;
    String name;
    String playerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_screen);

        //Initialise Firebase.
        Firebase.setAndroidContext(this);
        //fb = new Firebase("https://uws-checkersapp.firebaseio.com/");
        fb = new Firebase("https://boiling-torch-4353.firebaseio.com/");

        //Find the first player on the searching list only.
        final Firebase searchingRef = new Firebase("https://boiling-torch-4353.firebaseio.com/PlayersSearching");
        final Firebase gamesRef = new Firebase("https://boiling-torch-4353.firebaseio.com/GamesInProgress");
        final Query queryRef = searchingRef.orderByKey().limitToFirst(1);

        fb.child("PlayersSearching").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() < 3) {

                    //There are no opponents on the list.
                    System.out.println("There are no opponents waiting to play.");

                    //Once this player is added to the GamesInProgress node, an opponent was found.
                    gamesRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            //If the newest node contains this player's name...
                            if(dataSnapshot.getKey().contains("" + name)){

                                //Make sure the name is correct and doesn't just contain the letters.
                                for(DataSnapshot child: dataSnapshot.getChildren()){

                                    System.out.println(child.getKey());

                                    if(child.getKey().equals("Player1") && child.getValue().equals(name)){

                                        //Confirmed. Move to the game screen.
                                        System.out.println("Names match.");
                                        Intent intent = new Intent(getApplicationContext(), gameScreen.class);
                                        startActivity(intent);

                                    }else{

                                        System.out.println("No match. Similar characters only.");
                                    }
                                }
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });

                } else {

                    //There are other players on the list.
                    queryRef.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot2) {

                            String opponentName = "None";

                            //The query returned one child so find the children belonging to it.
                            for(DataSnapshot child: dataSnapshot2.getChildren()){

                                //Children of the child.
                                for(DataSnapshot child2: child.getChildren()){

                                    opponentName = child2.getValue().toString();

                                    System.out.println("Your opponent is: " + opponentName);
                                    //An opponent was found, so add player to gameInProgress with opponent.
                                    Map<String, Object> game = new HashMap<String, Object>();
                                    game.put("Player1", child2.getValue());
                                    game.put("Player2", name);
                                    game.put("Move", 1);
                                    game.put("Turn", child2.getValue());
                                    gamesRef.child(opponentName + "VS" + name).setValue(game);

                                    //Remove the players from the searching list.
                                    searchingRef.child(child.getKey()).setValue(null);
                                    searchingRef.child(playerID).setValue(null);

                                    //Move to the Game screen.
                                    Intent intent = new Intent(getApplicationContext(), gameScreen.class);
                                    startActivity(intent);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        //Get the name from sharedPreferences.
        preferences = getApplicationContext().getSharedPreferences("sharedPrefs", 0);
        name = preferences.getString("Name", "NoName");

        //Keep a reference of the key for the new Firebase entry.
        Firebase entry = fb.child("PlayersSearching").push();
        entry.child("name").setValue(name);
        playerID = entry.getKey();

        //Store the player's unique ID on the device for future use.
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ID", name);
        editor.commit();

        //Assign button reference and attach listener.
        cancelBTN = (ImageButton)findViewById(R.id.cancelBTN);
        cancelBTN.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_searching_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        //The cancel button has been touched so return back to the title screen.
        Intent intent;

        switch(view.getId()){

            case R.id.cancelBTN:

                intent = new Intent(this, titleScreen.class);
                startActivity(intent);

                //Because the cancel button has been touched, the player needs to be removed from the search list.
                fb.child("PlayersSearching").child(name).setValue(null);

                break;
        }
    }
}
