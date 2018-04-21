package com.example.b00189991.draughtsonline;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


public class resultsScreen extends Activity implements View.OnClickListener {

    ImageButton retryBTN;
    ImageButton newBTN;
    ImageButton menuBTN;
    ImageButton quitBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results_screen);

        retryBTN = (ImageButton)findViewById(R.id.retryBTN);
        newBTN = (ImageButton)findViewById(R.id.newBTN);
        menuBTN = (ImageButton)findViewById(R.id.menuBTN);
        quitBTN = (ImageButton)findViewById(R.id.quitBTN);

        retryBTN.setOnClickListener(this);
        newBTN.setOnClickListener(this);
        menuBTN.setOnClickListener(this);
        quitBTN.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_results_screen, menu);
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

        Intent intent;

        switch (view.getId()){

            case R.id.retryBTN:

                intent = new Intent(this, gameScreen.class);
                startActivity(intent);
                break;

            case R.id.newBTN:

                intent = new Intent(this, searchingScreen.class);
                startActivity(intent);
                break;

            case R.id.menuBTN:

                intent = new Intent(this, titleScreen.class);
                startActivity(intent);
                break;

            case R.id.quitBTN:

                finish();
                System.exit(0);
                break;
        }
    }
}
