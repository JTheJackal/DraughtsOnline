package com.example.b00189991.draughtsonline;

/**
 * Created by B00189991 on 14/04/2016.
 */
public class Move {

    private String player;
    private String searching;
    private String searchingText;

    public Move(){

        //Empty default constructor, necessary for Firebase to be able to deserialise posts.
    }

    public String getPlayer() {

        return this.player;
    }

    public String getSearching() {

        return this.searching;
    }

    public String getSearchingText(){

        return this.searchingText;
    }
}
