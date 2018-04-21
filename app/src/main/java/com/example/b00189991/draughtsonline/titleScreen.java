package com.example.b00189991.draughtsonline;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.Firebase;

import org.w3c.dom.Text;


public class titleScreen extends Activity implements View.OnClickListener{

    ImageButton findBTN;
    ImageButton resetBTN;
    ImageButton quitBTN;
    EditText nameTXT;
    TextView winsText;
    TextView lossText;
    TextView quitText;
    String name;
    Firebase fb;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);

        //Initialise Firebase.
        Firebase.setAndroidContext(this);
        //fb = new Firebase("https://uws-checkersapp.firebaseio.com/");
        fb = new Firebase("https://boiling-torch-4353.firebaseio.com/");

        //Attach the elements to variables.
        findBTN = (ImageButton)findViewById(R.id.findBTN);
        resetBTN = (ImageButton)findViewById(R.id.resetBTN);
        quitBTN = (ImageButton)findViewById(R.id.quitBTN);
        nameTXT = (EditText)findViewById(R.id.nameTXT);

        winsText = (TextView)findViewById(R.id.winsTXT);
        lossText = (TextView)findViewById(R.id.lossesTXT);
        quitText = (TextView)findViewById(R.id.quitsTXT);

        //Check for a name in sharedPreferences.
        preferences = getApplicationContext().getSharedPreferences("sharedPrefs", 0);
        nameTXT.setText(preferences.getString("Name", ""));

        //Set listeners for the buttons.
        findBTN.setOnClickListener(this);
        resetBTN.setOnClickListener(this);
        quitBTN.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_title_screen, menu);
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

        //Find which button was touched.
        switch(view.getId()){

            case R.id.resetBTN:

                //Reset the win/lose/quit stats.
                nameTXT.setText("");
                preferences.edit().remove("sharedPrefs").commit();
                winsText.setText("Wins: 0");
                lossText.setText("Losses: 0");
                quitText.setText("Quits: 0");
                break;

            case R.id.quitBTN:

                //Exit the application.
                finish();
                System.exit(0);
                break;

            case R.id.findBTN:

                //Add the player name to the searching node.
                name = (String) nameTXT.getText().toString();

                //Store the name on the device.
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Name", name);
                editor.commit();

                //Take the player to the searching screen.
                Intent intent = new Intent(this, searchingScreen.class);
                startActivity(intent);
                break;
        }


    }
}
