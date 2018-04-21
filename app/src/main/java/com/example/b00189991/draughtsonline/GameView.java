package com.example.b00189991.draughtsonline;

/**
 * Created by Joshua Styles B00189991 on 23/03/2016.
 *
 * SurfaceView will be used for smooth animations between piece movements.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameView extends SurfaceView implements Runnable {

    private Firebase fb;
    private Paint paint = new Paint();
    private Bitmap tempWhite, tempRed;
    private Bitmap board;
    private Sprite gameBoard;
    private Bitmap piecesTakenTemp, piecesLostTemp;
    private Sprite piecesTaken, piecesLost;
    private Bitmap serverMSGBG, serverMSGBGTemp;
    private Bitmap quitBTN, quitBTNTemp;
    private Sprite quit;
    private Bitmap infoBar, infoBarTemp;
    private SurfaceHolder holder;
    private int width;
    private Display display;
    private Point realHeight;
    private String name, serverMSG, opponentName, turnTime, totalTime;
    SharedPreferences preferences;
    Thread thread = null;
    volatile boolean running = false;
    static final long FPS = 10;
    MotionEvent event;
    private Sprite[] whitePieces = new Sprite[12];
    private Sprite[] redPieces = new Sprite[12];
    private Sprite[] playerPieces = new Sprite[12];
    private Sprite[] enemyPieces = new Sprite[12];
    private int piecePointer = 99;
    private int lostPointer = 99;

    /*
    private List<Integer> prevMovesX = new ArrayList<Integer>();
    private List<Integer> prevMovesY = new ArrayList<Integer>();
    private List<Integer> newMovesX = new ArrayList<Integer>();
    private List<Integer> newMovesY = new ArrayList<Integer>();
    private List<Integer> stolenPieces = new ArrayList<Integer>();
    */

    private int prevMoveX;
    private int prevMoveY;
    private int newMoveX;
    private int newMoveY;

    private boolean extraJump = false;
    private float touchX, touchY;
    private Sprite activePiece = null;
    private boolean jumpMade = false;
    private boolean movePossible = false;
    private boolean named = false;
    private boolean playerTurn = false;
    private boolean player1 = false;
    private boolean player2 = false;
    private Firebase thisGameRef;
    private boolean simulated = false;

    public GameView(Context context) {

        super(context);
        thread = new Thread(this);
        holder = getHolder();

        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                running = false;
                while (retry) {
                    try {
                        thread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                createSprites();
                running = true;
                thread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }
        });
    }

    private void createSprites(){

        //Get the name from sharedPreferences.
        preferences = getContext().getSharedPreferences("sharedPrefs", 0);
        name = preferences.getString("Name", "NoName");

        //Load images and assign.
        board = BitmapFactory.decodeResource(getResources(), R.drawable.board);

        piecesTakenTemp = BitmapFactory.decodeResource(getResources(), R.drawable.piecestakenbg);
        piecesLostTemp = BitmapFactory.decodeResource(getResources(), R.drawable.pieceslostbg);
        serverMSGBGTemp = BitmapFactory.decodeResource(getResources(), R.drawable.msgbg);
        quitBTNTemp = BitmapFactory.decodeResource(getResources(), R.drawable.quitgamebtn);
        infoBarTemp = BitmapFactory.decodeResource(getResources(), R.drawable.infobarbg);

        width = getContext().getResources().getDisplayMetrics().widthPixels;
        System.out.println("width of screen: " + width);
        serverMSGBG = Bitmap.createScaledBitmap(serverMSGBGTemp, (int)width, serverMSGBGTemp.getHeight() - 100, true);
        quitBTN = Bitmap.createScaledBitmap(quitBTNTemp, quitBTNTemp.getWidth() - 130, quitBTNTemp.getHeight()- 100, true);
        infoBar = Bitmap.createScaledBitmap(infoBarTemp, (int)width, infoBarTemp.getHeight() - 70, true);

        piecesLost = new Sprite(piecesLostTemp, 0, 0 + serverMSGBG.getHeight() + infoBar.getHeight(), width, null);
        gameBoard = new Sprite(board, 0, (0 + serverMSGBG.getHeight() + infoBar.getHeight() + piecesLost.getHeight()), width, width);
        piecesTaken = new Sprite(piecesTakenTemp, 0, 0 + serverMSGBG.getHeight() + infoBar.getHeight() + piecesLost.getHeight() + width, width, null);
        quit = new Sprite(quitBTN, (int)width - quitBTN.getWidth(), 0, null, null);

        tempWhite = BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite);
        tempRed = BitmapFactory.decodeResource(getResources(), R.drawable.piecered);

        int boardPositionY = gameBoard.getY();
        int tileWidth = gameBoard.getWidth()/8;
        int row = 0;
        int column = 1;

        //Set up white pieces.
        for(int i = 0; i < 12; i++){

            whitePieces[i] = new Sprite(tempWhite, 0 + (column*tileWidth), boardPositionY + (row*tileWidth), tileWidth, tileWidth);

            column += 2;

            if(i == 3){

                row++;
                column = 0;
            }else if(i == 7){

                row++;
                column = 1;
            }
        }

        //Reset values for setting up red pieces.
        row = 5;
        column = 0;


        for(int i = 0; i < 12; i++){

            redPieces[i] = new Sprite(tempRed, 0 + (column*tileWidth), boardPositionY + (row*tileWidth), tileWidth, tileWidth);
            //redPieces[i] = new Sprite(tempRed, 0 , 0, tileWidth, tileWidth);


            column += 2;

            if(i == 3){

                row++;
                column = 1;
            }else if(i == 7){

                row++;
                column = 0;
            }
        }

        //Set up the paint variable for drawing text on screen.
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(30);

        //Initialise Firebase.
        System.out.println("initialising Firebase");
        Firebase.setAndroidContext(getContext());
        //fb = new Firebase("https://uws-checkersapp.firebaseio.com/");
        fb = new Firebase("https://boiling-torch-4353.firebaseio.com/");
        final Firebase gamesRef = new Firebase("https://boiling-torch-4353.firebaseio.com/GamesInProgress");

        //Callbacks needed to control the game.

        System.out.println("Creating callbacks");
        //Once this player is added to the GamesInProgress node, an opponent was found.
        gamesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //If the newest node contains this player's name...
                if (dataSnapshot.getKey().contains("" + name)) {

                    //Make sure the name is correct and doesn't just contain the letters.
                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                        System.out.println(child.getKey());

                        if (child.getKey().equals("Player1") && child.getValue().equals(name)) {

                            thisGameRef = gamesRef.child(dataSnapshot.getKey());
                            //Confirmed. Take a note of the opponent name.
                            System.out.println("Names match. Game node found: " + dataSnapshot.getKey());
                            opponentName = dataSnapshot.child("Player2").getValue().toString();
                            player1 = true;
                            player2 = false;


                            if(dataSnapshot.child("Turn").getValue().toString().equals(opponentName)){

                                //It is the opponent's turn.
                                playerTurn = false;
                            }else{

                                playerTurn = true;

                            }

                        } else if(child.getKey().equals("Player2") && child.getValue().equals(name)) {

                            thisGameRef = gamesRef.child(dataSnapshot.getKey());
                            System.out.println("Names match. Game node found: " + dataSnapshot.getKey());
                            opponentName = dataSnapshot.child("Player1").getValue().toString();
                            player1 = false;
                            player2 = true;

                            if(dataSnapshot.child("Turn").getValue().toString().equals(opponentName)){

                                //It is the opponent's turn.
                                playerTurn = false;
                            }else{

                                playerTurn = true;
                            }
                        }else{

                            System.out.println("Wrong game");
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                //If the newest node contains this player's name...
                if (dataSnapshot.getKey().contains("" + name)) {

                    boolean correctNode = false;

                    //Make sure the name is correct and doesn't just contain the letters.
                    for (DataSnapshot child : dataSnapshot.getChildren()) {

                        System.out.println(child.getKey());

                        if (child.getKey().equals("Player1") && child.getValue().equals(name)) {

                            thisGameRef = gamesRef.child(dataSnapshot.getKey());

                            //Confirmed. Take a note of the opponent name.
                            System.out.println("Names match. Game node found: " + dataSnapshot.getKey());
                            opponentName = dataSnapshot.child("Player2").getValue().toString();
                            player1 = true;
                            player2 = false;
                            correctNode = true;


                            if(dataSnapshot.child("Turn").getValue().toString().equals(opponentName)){

                                //It is the opponent's turn.
                                playerTurn = false;


                            }else{

                                playerTurn = true;
                            }

                        } else if(child.getKey().equals("Player2") && child.getValue().equals(name)) {

                            System.out.println("Names match. Game node found: " + dataSnapshot.getKey());
                            opponentName = dataSnapshot.child("Player1").getValue().toString();
                            player1 = false;
                            player2 = true;
                            correctNode = true;

                            if(dataSnapshot.child("Turn").getValue().toString().equals(opponentName)){

                                //It is the opponent's turn.
                                playerTurn = false;
                            }else{

                                playerTurn = true;
                            }
                        }else{

                            System.out.println("Wrong game");
                        }

                        System.out.println("About to check move nodes. correctNode is: " + correctNode);
                        if(child.getKey().equals("newMoveX") && correctNode){

                            newMoveX = Integer.parseInt(child.getValue().toString());

                        }

                        if(child.getKey().equals("newMoveY") && correctNode){

                            newMoveY = Integer.parseInt(child.getValue().toString());

                        }

                        if(child.getKey().equals("prevMoveX") && correctNode){

                            prevMoveX = Integer.parseInt(child.getValue().toString());
                        }

                        if(child.getKey().equals("prevMoveY") && correctNode){

                            prevMoveY = Integer.parseInt(child.getValue().toString());
                        }

                        if(child.getKey().equals("Stolen") && correctNode){

                            lostPointer = Integer.parseInt(child.getValue().toString());
                        }
                    }
                }
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

            /*
            //TESTING//
            redPieces[0].moveX(whitePieces[10].getX() - whitePieces[10].getWidth());
            redPieces[0].moveY(whitePieces[10].getY() + whitePieces[10].getWidth());
            redPieces[1].moveX(redPieces[0].getX() - redPieces[0].getWidth() * 2);
            redPieces[1].moveY(redPieces[0].getY() + redPieces[0].getWidth() * 2);
            //TESTING
            */

        //Assign player pieces.
        if(player1){

            playerPieces = whitePieces;
            enemyPieces = redPieces;
        }else{

            playerPieces = redPieces;
            enemyPieces = whitePieces;
        }

    }

    @Override
    public void run () {

        long ticksPS = 500 / FPS;
        long startTime;
        long sleepTime;

        while (running) {
            Canvas c = null;
            startTime = System.currentTimeMillis();
            try {
                c = getHolder().lockCanvas();
                synchronized (getHolder()) {
                    onDraw(c);
                }
            } finally {
                if (c != null) {
                    getHolder().unlockCanvasAndPost(c);
                }
            }
            sleepTime = ticksPS - (System.currentTimeMillis() - startTime);
            try {
                if (sleepTime > 0)
                    thread.sleep(sleepTime);
                else
                    thread.sleep(10);
            } catch (Exception e) {
            }
        }
    }

    protected void update() {

        int tileWidth = playerPieces[0].getWidth();
        int enemyRow;
        //piecePointer = 99;

        //Assign player pieces.
        if(player1){

            playerPieces = whitePieces;
            enemyPieces = redPieces;
            for(int i = 0; i < 12; i++){

                playerPieces[i].setRed(false);
            }
            enemyRow = gameBoard.getY() + 7 * tileWidth;
        }else{

            playerPieces = redPieces;
            enemyPieces = whitePieces;
            for(int i = 0; i < 12; i++){

                playerPieces[i].setRed(true);
            }
            enemyRow = gameBoard.getY();
        }

            /*
            if (playerPieces == whitePieces) {

                enemyRow = gameBoard.getY() + 7 * tileWidth;
            } else {

                enemyRow = gameBoard.getY();
            }
            */

        //Game logic and handling.
        if(playerTurn) {

            simOpponentTurn(tileWidth);
            playerMovement(tileWidth);
        }else{

            //It's not the players turn so simulate opponent moves when they are made.
            if(!simulated){

                //simOpponentTurn(tileWidth);
            }
        }

        checkNewKing(enemyRow);
        checkInactive(tileWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        update();

        canvas.drawBitmap(serverMSGBG, 0, 0, null);
        canvas.drawBitmap(infoBar, 0, 0 + serverMSGBG.getHeight(), null);
        quit.onDraw(canvas);
        piecesLost.onDraw(canvas);
        gameBoard.onDraw(canvas);
        piecesTaken.onDraw(canvas);
        for (int i = 0; i < 12; i++) {

            whitePieces[i].onDraw(canvas);
            redPieces[i].onDraw(canvas);
        }

        canvas.drawText("Opponent: " + opponentName, 10, serverMSGBGTemp.getHeight() + 5, paint);
        canvas.drawText("Turn: " + playerTurn, canvas.getWidth()/2, serverMSGBGTemp.getHeight() + 5, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //Test for what is being touched.
        float x = event.getX();
        float y = event.getY();

        touchX = x;
        touchY = y;

        if (quit.isHit(x, y)) {

            System.out.println("Quit button was pressed.");
        }

        for (int i = 0; i < 12; i++) {

            if (playerPieces[i].isHit(x, y)) {

                System.out.println("Piece: " + i + " was touched.");
                //Make all other pieces not touched to avoid buggy movement.
                for (int j = 0; j < 12; j++) {

                    playerPieces[j].makeNotTouched();
                }
                playerPieces[i].makeTouched();
                System.out.println("Piece: " + i + " is Touched: " + playerPieces[i].getTouched());
            }
        }

        return true;
    }

    public void playerMovement(int tileWidth) {

        boolean occupied = false;
        boolean neighbourOccupied = false;
        int enemyRow;
        int targetX = 0, targetY = 0;
        Sprite tempPiece = null;

        if (playerPieces == whitePieces) {

            enemyRow = gameBoard.getY() + 7 * tileWidth;
        } else {

            enemyRow = gameBoard.getY();
        }

        if (jumpMade) {

            System.out.println("Checking for available moves.");
            movePossible = possibleMoves();
            jumpMade = false;
        }
        if (activePiece != null) {

            if (movePossible) {

                //Check for more moves that can be made around this piece without letting the player select another piece.
                //Check the touch is within the bounds of the board.
                if (touchY > gameBoard.getY() && touchY < gameBoard.getY() + gameBoard.getHeight()) {

                    //Check for a touch on the left.
                    if (touchX < activePiece.getX() && touchX > activePiece.getX() - tileWidth) {
                        //Top left touch.
                        if (touchY < activePiece.getY() && touchY > activePiece.getY() - tileWidth && (activePiece.isKing() || player2)) {

                            System.out.println("Move to top left.");
                            touchX = 0;
                            touchY = 0;

                            //Check if tile is occupied by friendly piece.
                            for (int j = 0; j < 12; j++) {

                                if (playerPieces[j].getX() == activePiece.getX() - tileWidth && playerPieces[j].getY() == activePiece.getY() - tileWidth) {

                                    System.out.println("Tile occupied.");
                                    occupied = true;
                                }
                            }

                            //Check if tile is occupied by enemy piece.
                            for (int j = 0; j < 12; j++) {

                                if (enemyPieces[j].getX() == activePiece.getX() - tileWidth && enemyPieces[j].getY() == activePiece.getY() - tileWidth) {

                                    System.out.println("Tile occupied by enemy.");
                                    //Check for possible jump by checking the next space over.

                                    targetX = enemyPieces[j].getX() - tileWidth;
                                    targetY = enemyPieces[j].getY() - tileWidth;
                                    for (int k = 0; k < 12; k++) {

                                        if (enemyPieces[k].getX() == enemyPieces[j].getX() - tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {
                                            //A possible jump can't be made.
                                            neighbourOccupied = true;

                                        }
                                    }
                                    occupied = true;
                                }
                            }

                            if (!occupied) {

                                //activePiece.moveX(playerPieces[i].getX() - tileWidth);
                                //activePiece.moveY(playerPieces[i].getY() - tileWidth);
                            } else if (!neighbourOccupied) {

                                jumpMade = true;

                                //Store the previous position and new positions.
                                prevMoveX = 8*activePiece.getX()/width;
                                prevMoveY = 8*activePiece.getY()/width;
                                activePiece.moveX((float)targetX);
                                activePiece.moveY((float)targetY);
                                newMoveX = 8*activePiece.getX()/width;
                                newMoveY = 8*activePiece.getY()/width;
                            }
                        } else if (touchY > activePiece.getY() + tileWidth && touchY < activePiece.getY() + (tileWidth * 2) && (activePiece.isKing() || player1)) {

                            //Bottom left touch.
                            System.out.println("Move to bottom left.");
                            touchX = 0;
                            touchY = 0;

                            //Check if tile is occupied by friendly piece.
                            for (int j = 0; j < 12; j++) {

                                if (playerPieces[j].getX() == activePiece.getX() - tileWidth && playerPieces[j].getY() == activePiece.getY() + tileWidth) {

                                    System.out.println("Tile occupied.");
                                    occupied = true;
                                }
                            }

                            //Check if tile is occupied by enemy piece.
                            for (int j = 0; j < 12; j++) {

                                if (enemyPieces[j].getX() == activePiece.getX() - tileWidth && enemyPieces[j].getY() == activePiece.getY() + tileWidth) {

                                    System.out.println("Tile occupied by enemy.");
                                    //Check for possible jump by checking the next space over.
                                    targetX = enemyPieces[j].getX() - tileWidth;
                                    targetY = enemyPieces[j].getY() + tileWidth;
                                    for (int k = 0; k < 12; k++) {

                                        if (enemyPieces[k].getX() == enemyPieces[j].getX() - tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                                            //A possible jump can't be made.
                                            System.out.println("Neighbour occupied too");
                                            neighbourOccupied = true;

                                        }
                                    }
                                    occupied = true;
                                }
                            }

                            if (!occupied) {

                            } else if (!neighbourOccupied) {

                                jumpMade = true;
                                prevMoveX = 8*activePiece.getX()/width;
                                prevMoveY = 8*activePiece.getY()/width;
                                activePiece.moveX((float)targetX);
                                activePiece.moveY((float)targetY);
                                newMoveX = 8*activePiece.getX()/width;
                                newMoveY = 8*activePiece.getY()/width;
                            }
                        }
                    } else if (touchX > activePiece.getX() + tileWidth && touchX < activePiece.getX() + (tileWidth * 2)) {

                        //Checking for touches on the right side of the piece.
                        //Top right touch.
                        if (touchY < activePiece.getY() && touchY > activePiece.getY() - tileWidth && (activePiece.isKing() || player2)) {

                            System.out.println("Move to top right.");
                            touchX = 0;
                            touchY = 0;

                            //Check if tile is occupied by friendly piece.
                            for (int j = 0; j < 12; j++) {

                                if (playerPieces[j].getX() == activePiece.getX() + tileWidth && playerPieces[j].getY() == activePiece.getY() - tileWidth) {

                                    System.out.println("Tile occupied.");
                                    occupied = true;
                                }
                            }

                            //Check if tile is occupied by enemy piece.
                            for (int j = 0; j < 12; j++) {

                                if (enemyPieces[j].getX() == activePiece.getX() + tileWidth && enemyPieces[j].getY() == activePiece.getY() - tileWidth) {

                                    System.out.println("Tile occupied by enemy.");
                                    //Check for possible jump by checking the next space over.

                                    targetX = enemyPieces[j].getX() + tileWidth;
                                    targetY = enemyPieces[j].getY() - tileWidth;
                                    for (int k = 0; k < 12; k++) {

                                        if (enemyPieces[k].getX() == enemyPieces[j].getX() + tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {
                                            //A possible jump can't be made.
                                            neighbourOccupied = true;

                                        }
                                    }
                                    occupied = true;
                                }
                            }

                            if (!occupied) {


                            } else if (!neighbourOccupied) {

                                prevMoveX = 8*activePiece.getX()/width;
                                prevMoveY = 8*activePiece.getY()/width;
                                activePiece.moveX((float)targetX);
                                activePiece.moveY((float)targetY);
                                newMoveX = 8*activePiece.getX()/width;
                                newMoveY = 8*activePiece.getY()/width;
                            }
                        } else if (touchY > activePiece.getY() + tileWidth && touchY < activePiece.getY() + (tileWidth * 2) && (activePiece.isKing() || player1)) {

                            //Bottom right touch.
                            System.out.println("Move to bottom right.");
                            touchX = 0;
                            touchY = 0;

                            //Check if tile is occupied by friendly piece.
                            for (int j = 0; j < 12; j++) {

                                if (playerPieces[j].getX() == activePiece.getX() + tileWidth && playerPieces[j].getY() == activePiece.getY() + tileWidth) {

                                    System.out.println("Tile occupied.");
                                    occupied = true;
                                }
                            }

                            //Check if tile is occupied by enemy piece.
                            for (int j = 0; j < 12; j++) {

                                if (enemyPieces[j].getX() == activePiece.getX() + tileWidth && enemyPieces[j].getY() == activePiece.getY() + tileWidth) {

                                    System.out.println("Tile occupied by enemy.");
                                    //Check for possible jump by checking the next space over.

                                    targetX = enemyPieces[j].getX() + tileWidth;
                                    targetY = enemyPieces[j].getY() + tileWidth;
                                    for (int k = 0; k < 12; k++) {

                                        if (enemyPieces[k].getX() == enemyPieces[j].getX() + tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                                            //A possible jump can't be made.
                                            neighbourOccupied = true;

                                        }
                                    }
                                    occupied = true;
                                }
                            }

                            if (!occupied) {

                            } else if (!neighbourOccupied) {

                                prevMoveX = 8*activePiece.getX()/width;
                                prevMoveY = 8*activePiece.getY()/width;
                                activePiece.moveX((float)targetX);
                                activePiece.moveY((float)targetY);
                                newMoveX = 8*activePiece.getX()/width;
                                newMoveY = 8*activePiece.getY()/width;
                            }

                            //Reset occupation detection.
                            occupied = false;
                            neighbourOccupied = false;
                        }
                    }
                }
            } else {

                activePiece = null;
                System.out.println("No possible moves were found");
                playerTurn = false;

                //Update Firebase entry with new movements.
                Map<String, Object> game = new HashMap<String, Object>();
                if(player1){
                    game.put("Player1", name);
                    game.put("Player2", opponentName);
                }else{
                    game.put("Player1", opponentName);
                    game.put("Player2", name);
                }

                game.put("prevMoveX", prevMoveX);
                game.put("prevMoveY", prevMoveY);
                game.put("newMoveX", newMoveX);
                game.put("newMoveY", newMoveY);

                game.put("Turn", opponentName);
                game.put("Stolen", piecePointer);
                thisGameRef.setValue(game);
            }

        } else {

            for (int i = 0; i < 12; i++) {

                if (playerPieces[i].getTouched()) {

                    //Check for the player moving the piece.
                    //Check the touch is within the bounds of the board.
                    if (touchY > gameBoard.getY() && touchY < gameBoard.getY() + gameBoard.getHeight()) {

                        //Check for a touch on the left.
                        if (touchX < playerPieces[i].getX() && touchX > playerPieces[i].getX() - tileWidth) {
                            //Top left touch.
                            if (touchY < playerPieces[i].getY() && touchY > playerPieces[i].getY() - tileWidth && (playerPieces[i].isKing() || player2)) {

                                System.out.println("Move to top left.");
                                touchX = 0;
                                touchY = 0;
                                playerPieces[i].makeNotTouched();

                                //Check if tile is occupied by friendly piece.
                                for (int j = 0; j < 12; j++) {

                                    if (playerPieces[j].getX() == playerPieces[i].getX() - tileWidth && playerPieces[j].getY() == playerPieces[i].getY() - tileWidth) {

                                        System.out.println("Tile occupied.");
                                        occupied = true;
                                    }
                                }

                                //Check if tile is occupied by enemy piece.
                                for (int j = 0; j < 12; j++) {

                                    if (enemyPieces[j].getX() == playerPieces[i].getX() - tileWidth && enemyPieces[j].getY() == playerPieces[i].getY() - tileWidth) {

                                        System.out.println("Tile occupied by enemy.");
                                        //Check for possible jump by checking the next space over.

                                        enemyPieces[j].makeInactive();
                                        piecePointer = j;
                                        targetX = enemyPieces[j].getX() - tileWidth;
                                        targetY = enemyPieces[j].getY() - tileWidth;
                                        for (int k = 0; k < 12; k++) {

                                            if (enemyPieces[k].getX() == enemyPieces[j].getX() - tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {
                                                //A possible jump can't be made.
                                                neighbourOccupied = true;
                                                enemyPieces[j].makeActive();
                                                piecePointer = 99;
                                            }

                                            if (playerPieces[k].getX() == enemyPieces[j].getX() - tileWidth && playerPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {
                                                //A possible jump can't be made.
                                                neighbourOccupied = true;
                                                enemyPieces[j].makeActive();
                                                piecePointer = 99;
                                            }
                                        }
                                        occupied = true;
                                    }
                                }

                                if (!occupied) {

                                    prevMoveX = 8*playerPieces[i].getX()/width;
                                    prevMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerPieces[i].moveX(playerPieces[i].getX() - tileWidth);
                                    playerPieces[i].moveY(playerPieces[i].getY() - tileWidth);
                                    newMoveX = 8 * playerPieces[i].getX() / width;
                                    newMoveY = 8 * (playerPieces[i].getY() - gameBoard.getY()) / width;
                                    playerTurn = false;

                                    //Update Firebase entry.
                                    Map<String, Object> game = new HashMap<String, Object>();
                                    if(player1){
                                        game.put("Player1", name);
                                        game.put("Player2", opponentName);
                                    }else{
                                        game.put("Player1", opponentName);
                                        game.put("Player2", name);
                                    }

                                    game.put("prevMoveX", prevMoveX);
                                    game.put("prevMoveY", prevMoveY);
                                    game.put("newMoveX", newMoveX);
                                    game.put("newMoveY", newMoveY);
                                    game.put("Turn", opponentName);
                                    game.put("Stolen", piecePointer);
                                    if(tempPiece != null) {

                                        //Send coordinates to server.
                                        game.put("JumpedX", tempPiece.getX());
                                        game.put("JumpedY", tempPiece.getY());
                                        //Remove piece from this game.
                                        tempPiece.makeInactive();
                                    }
                                    thisGameRef.setValue(game);

                                } else if (!neighbourOccupied) {

                                    activePiece = playerPieces[i];
                                    jumpMade = true;
                                    //Store the previous position and new positions.
                                    prevMoveX = 8*playerPieces[i].getX()/width;
                                    prevMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerPieces[i].moveX((float) targetX);
                                    playerPieces[i].moveY((float) targetY);
                                    newMoveX =8 * playerPieces[i].getX() / width;
                                    newMoveY = 8 * (playerPieces[i].getY() - gameBoard.getY()) / width;
                                }
                            } else if (touchY > playerPieces[i].getY() + tileWidth && touchY < playerPieces[i].getY() + (tileWidth * 2) && (playerPieces[i].isKing() || player1)) {

                                //Bottom left touch.
                                System.out.println("Move to bottom left.");
                                System.out.println("PlayerPiece.X: " + playerPieces[i].getX());
                                System.out.println("PlayerPiece.Y: " + playerPieces[i].getY());
                                touchX = 0;
                                touchY = 0;
                                playerPieces[i].makeNotTouched();

                                //Check if tile is occupied by friendly piece.
                                for (int j = 0; j < 12; j++) {

                                    if (playerPieces[j].getX() == playerPieces[i].getX() - tileWidth && playerPieces[j].getY() == playerPieces[i].getY() + tileWidth) {

                                        System.out.println("Tile occupied.");
                                        occupied = true;
                                    }
                                }

                                //Check if tile is occupied by enemy piece.
                                for (int j = 0; j < 12; j++) {

                                    if (enemyPieces[j].getX() == playerPieces[i].getX() - tileWidth && enemyPieces[j].getY() == playerPieces[i].getY() + tileWidth) {

                                        System.out.println("Tile occupied by enemy.");
                                        //Check for possible jump by checking the next space over.

                                        enemyPieces[j].makeInactive();
                                        piecePointer = j;
                                        targetX = enemyPieces[j].getX() - tileWidth;
                                        targetY = enemyPieces[j].getY() + tileWidth;
                                        for (int k = 0; k < 12; k++) {

                                            if (enemyPieces[k].getX() == enemyPieces[j].getX() - tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                                                //A possible jump can't be made.
                                                System.out.println("Neighbour occupied too");
                                                neighbourOccupied = true;
                                                enemyPieces[j].makeActive();
                                                piecePointer = 99;
                                            }

                                            if (playerPieces[k].getX() == enemyPieces[j].getX() - tileWidth && playerPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                                                //A possible jump can't be made.
                                                System.out.println("Neighbour occupied too");
                                                neighbourOccupied = true;
                                                enemyPieces[j].makeActive();
                                                piecePointer = 99;
                                            }

                                        }
                                        occupied = true;
                                    }
                                }

                                if (!occupied) {

                                    System.out.println("Tilewidth = " + tileWidth);
                                    //Store the previous position and new positions.
                                    prevMoveX = 8*playerPieces[i].getX()/width;
                                    prevMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerPieces[i].moveX(playerPieces[i].getX() - tileWidth);
                                    playerPieces[i].moveY(playerPieces[i].getY() + tileWidth);
                                    newMoveX = 8*playerPieces[i].getX()/width;
                                    newMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerTurn = false;

                                    //Update Firebase entry.
                                    Map<String, Object> game = new HashMap<String, Object>();

                                    if(player1){
                                        game.put("Player1", name);
                                        game.put("Player2", opponentName);
                                    }else{
                                        game.put("Player1", opponentName);
                                        game.put("Player2", name);
                                    }

                                    game.put("prevMoveX", prevMoveX);
                                    game.put("prevMoveY", prevMoveY);
                                    game.put("newMoveX", newMoveX);
                                    game.put("newMoveY", newMoveY);
                                    game.put("Turn", opponentName);
                                    game.put("Stolen", piecePointer);
                                    if(tempPiece != null) {

                                        game.put("JumpedX", tempPiece.getX());
                                        game.put("JumpedY", tempPiece.getY());
                                        tempPiece.makeInactive();
                                    }
                                    thisGameRef.setValue(game);

                                    System.out.println("PlayerPiece.X: " + playerPieces[i].getX());
                                    System.out.println("PlayerPiece.Y: " + playerPieces[i].getY());

                                } else if (!neighbourOccupied) {

                                    activePiece = playerPieces[i];
                                    jumpMade = true;
                                    //Store the previous position and new positions.
                                    prevMoveX = 8*playerPieces[i].getX()/width;
                                    prevMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerPieces[i].moveX((float) targetX);
                                    playerPieces[i].moveY((float) targetY);
                                    newMoveX = 8*playerPieces[i].getX()/width;
                                    newMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                }
                            }
                        } else if (touchX > playerPieces[i].getX() + tileWidth && touchX < playerPieces[i].getX() + (tileWidth * 2)) {

                            //Checking for touches on the right side of the piece.
                            //Top right touch.
                            if (touchY < playerPieces[i].getY() && touchY > playerPieces[i].getY() - tileWidth && (playerPieces[i].isKing() || player2)) {

                                System.out.println("Move to top right.");
                                touchX = 0;
                                touchY = 0;
                                playerPieces[i].makeNotTouched();


                                //Check if tile is occupied by friendly piece.
                                for (int j = 0; j < 12; j++) {

                                    if (playerPieces[j].getX() == playerPieces[i].getX() + tileWidth && playerPieces[j].getY() == playerPieces[i].getY() - tileWidth) {

                                        System.out.println("Tile occupied.");
                                        occupied = true;
                                    }
                                }

                                //Check if tile is occupied by enemy piece.
                                for (int j = 0; j < 12; j++) {

                                    if (enemyPieces[j].getX() == playerPieces[i].getX() + tileWidth && enemyPieces[j].getY() == playerPieces[i].getY() - tileWidth) {

                                        System.out.println("Tile occupied by enemy.");
                                        //Check for possible jump by checking the next space over.

                                        enemyPieces[j].makeInactive();
                                        targetX = enemyPieces[j].getX() + tileWidth;
                                        targetY = enemyPieces[j].getY() - tileWidth;
                                        piecePointer = j;
                                        for (int k = 0; k < 12; k++) {

                                            if (enemyPieces[k].getX() == enemyPieces[j].getX() + tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {
                                                //A possible jump can't be made.
                                                neighbourOccupied = true;
                                                enemyPieces[j].makeActive();
                                                piecePointer = 99;

                                            }

                                            if (playerPieces[k].getX() == enemyPieces[j].getX() + tileWidth && playerPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {
                                                //A possible jump can't be made.
                                                neighbourOccupied = true;
                                                enemyPieces[j].makeActive();
                                                piecePointer = 99;
                                            }

                                            if(!neighbourOccupied){

                                                tempPiece = enemyPieces[j];
                                            }
                                        }
                                        occupied = true;
                                    }
                                }

                                if (!occupied) {

                                    //Store the previous position and new positions.
                                    prevMoveX = 8*playerPieces[i].getX()/width;
                                    prevMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerPieces[i].moveX(playerPieces[i].getX() + tileWidth);
                                    playerPieces[i].moveY(playerPieces[i].getY() - tileWidth);
                                    newMoveX = 8 * playerPieces[i].getX() / width;
                                    newMoveY = 8 * (playerPieces[i].getY() - gameBoard.getY()) / width;
                                    playerTurn = false;

                                    //Update Firebase entry.
                                    Map<String, Object> game = new HashMap<String, Object>();
                                    if(player1){
                                        game.put("Player1", name);
                                        game.put("Player2", opponentName);
                                    }else{
                                        game.put("Player1", opponentName);
                                        game.put("Player2", name);
                                    }
                                    game.put("prevMoveX", prevMoveX);
                                    game.put("prevMoveY", prevMoveY);
                                    game.put("newMoveX", newMoveX);
                                    game.put("newMoveY", newMoveY);
                                    if(tempPiece != null) {

                                        game.put("JumpedX", tempPiece.getX());
                                        game.put("JumpedY", tempPiece.getY());
                                        tempPiece.makeInactive();
                                    }

                                    game.put("Turn", opponentName);
                                    game.put("Stolen", piecePointer);
                                    thisGameRef.setValue(game);
                                } else if (!neighbourOccupied) {

                                    activePiece = playerPieces[i];
                                    jumpMade = true;
                                    //Store the previous position and new positions.
                                    prevMoveX = 8*playerPieces[i].getX()/width;
                                    prevMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerPieces[i].moveX((float) targetX);
                                    playerPieces[i].moveY((float) targetY);
                                    newMoveX = 8 * playerPieces[i].getX() / width;
                                    newMoveY = 8 * (playerPieces[i].getY() - gameBoard.getY()) / width;
                                }
                            } else if (touchY > playerPieces[i].getY() + tileWidth && touchY < playerPieces[i].getY() + (tileWidth * 2) && (playerPieces[i].isKing() || player1)) {

                                //Bottom right touch.
                                System.out.println("Move to bottom right.");
                                touchX = 0;
                                touchY = 0;
                                playerPieces[i].makeNotTouched();

                                //Check if tile is occupied by friendly piece.
                                for (int j = 0; j < 12; j++) {

                                    if (playerPieces[j].getX() == playerPieces[i].getX() + tileWidth && playerPieces[j].getY() == playerPieces[i].getY() + tileWidth) {

                                        System.out.println("Tile occupied.");
                                        occupied = true;
                                    }
                                }

                                //Check if tile is occupied by enemy piece.
                                for (int j = 0; j < 12; j++) {

                                    if (enemyPieces[j].getX() == playerPieces[i].getX() + tileWidth && enemyPieces[j].getY() == playerPieces[i].getY() + tileWidth) {

                                        System.out.println("Tile occupied by enemy.");
                                        //Check for possible jump by checking the next space over.

                                        enemyPieces[j].makeInactive();
                                        targetX = enemyPieces[j].getX() + tileWidth;
                                        targetY = enemyPieces[j].getY() + tileWidth;
                                        piecePointer = j;
                                        for (int k = 0; k < 12; k++) {

                                            if (enemyPieces[k].getX() == enemyPieces[j].getX() + tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                                                //A possible jump can't be made.
                                                neighbourOccupied = true;
                                                enemyPieces[j].makeActive();
                                                piecePointer = 99;
                                            }

                                            if (playerPieces[k].getX() == enemyPieces[j].getX() + tileWidth && playerPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                                                //A possible jump can't be made.
                                                neighbourOccupied = true;
                                                enemyPieces[j].makeActive();
                                                piecePointer = 99;
                                            }

                                            if(!neighbourOccupied){

                                                tempPiece = enemyPieces[j];
                                            }
                                        }
                                        occupied = true;
                                    }
                                }

                                if (!occupied) {

                                    //Store the previous position and new positions.
                                    prevMoveX = 8*playerPieces[i].getX()/width;
                                    prevMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerPieces[i].moveX(playerPieces[i].getX() + tileWidth);
                                    playerPieces[i].moveY(playerPieces[i].getY() + tileWidth);
                                    newMoveX = 8*playerPieces[i].getX()/width;
                                    newMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerTurn = false;

                                    //Update Firebase entry.
                                    Map<String, Object> game = new HashMap<String, Object>();
                                    if(player1){
                                        game.put("Player1", name);
                                        game.put("Player2", opponentName);
                                    }else{
                                        game.put("Player1", opponentName);
                                        game.put("Player2", name);
                                    }

                                    game.put("prevMoveX", prevMoveX);
                                    game.put("prevMoveY", prevMoveY);
                                    game.put("newMoveX", newMoveX);
                                    game.put("newMoveY", newMoveY);
                                    game.put("Turn", opponentName);
                                    game.put("Stolen", piecePointer);
                                    if(tempPiece != null) {

                                        game.put("JumpedX", tempPiece.getX());
                                        game.put("JumpedY", tempPiece.getY());
                                        tempPiece.makeInactive();
                                    }
                                    thisGameRef.setValue(game);
                                } else if (!neighbourOccupied) {

                                    activePiece = playerPieces[i];
                                    jumpMade = true;
                                    //Store the previous position and new positions.
                                    prevMoveX = 8*playerPieces[i].getX()/width;
                                    prevMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                    playerPieces[i].moveX((float) targetX);
                                    playerPieces[i].moveY((float) targetY);
                                    newMoveX = 8*playerPieces[i].getX()/width;
                                    newMoveY = 8*(playerPieces[i].getY()-gameBoard.getY())/width;
                                }

                                //Reset occupation detection.
                                occupied = false;
                                neighbourOccupied = false;
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkNewKing(int enemyRow) {

        for (int i = 0; i < 12; i++) {

            //King a piece if it reaches the opposite end of the board.
            if (playerPieces[i].getY() == enemyRow) {

                playerPieces[i].setKing();
            }
        }
    }

    public boolean possibleMoves() {

        boolean occupied1 = false;
        boolean occupied2 = false;
        boolean occupied3 = false;
        boolean occupied4 = false;
        boolean neighbourOccupied = false;
        int tileWidth = playerPieces[0].getWidth();
        boolean check1 = false;
        boolean check2 = false;
        boolean check3 = false;
        boolean check4 = false;

        //Check if tile is occupied by friendly piece.
        for (int j = 0; j < 12; j++) {

            if (playerPieces[j].getX() == activePiece.getX() - tileWidth && playerPieces[j].getY() == activePiece.getY() - tileWidth) {

                System.out.println("Tile occupied.");
                check1 = true;
            }
        }

        if (!check1) {
            //Check if tile is occupied by enemy piece.
            for (int j = 0; j < 12; j++) {

                if (enemyPieces[j].getX() == activePiece.getX() - tileWidth && enemyPieces[j].getY() == activePiece.getY() - tileWidth) {

                    System.out.println("Tile occupied by enemy.");
                    //Check for possible jump by checking the next space over.

                    for (int k = 0; k < 12; k++) {

                        if (enemyPieces[k].getX() == enemyPieces[j].getX() - tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {
                            //A possible jump can't be made.
                            neighbourOccupied = true;
                            check1 = true;
                        }

                        if (playerPieces[k].getX() == enemyPieces[j].getX() - tileWidth && playerPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {

                            //Friendly piece in the way.
                            System.out.println("Player piece in the way.");
                            neighbourOccupied = true;
                            check1 = true;
                        }

                        if(enemyPieces[j].getX() == 0){

                            //Can't jump this piece as it would take the player piece off the board.
                            check1 = true;
                        }

                        if(enemyPieces[j].getY() == gameBoard.getY() || enemyPieces[j].getY() == gameBoard.getY() + gameBoard.getHeight() - tileWidth){

                            //Can't jump this piece as it would take the player piece off the board.
                            check1 = true;
                        }
                    }
                    occupied1 = true;
                }
            }
        }

        //Check if tile is occupied by friendly piece.
        for (int j = 0; j < 12; j++) {

            if (playerPieces[j].getX() == activePiece.getX() - tileWidth && playerPieces[j].getY() == activePiece.getY() + tileWidth) {

                System.out.println("Tile occupied.");
                check2 = true;
            }
        }

        if (!check2) {
            //Check if tile is occupied by enemy piece.
            for (int j = 0; j < 12; j++) {

                if (enemyPieces[j].getX() == activePiece.getX() - tileWidth && enemyPieces[j].getY() == activePiece.getY() + tileWidth) {

                    System.out.println("Tile occupied by enemy.");
                    //Check for possible jump by checking the next space over.

                    for (int k = 0; k < 12; k++) {

                        if (enemyPieces[k].getX() == enemyPieces[j].getX() - tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                            //A possible jump can't be made.
                            System.out.println("Neighbour occupied too");
                            neighbourOccupied = true;
                            check2 = true;

                        }

                        if (playerPieces[k].getX() == enemyPieces[j].getX() - tileWidth && playerPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                            //A possible jump can't be made.
                            System.out.println("Player piece in the way.");
                            neighbourOccupied = true;
                            check2 = true;

                        }

                        if(enemyPieces[j].getX() == 0){

                            //Can't jump this piece as it would take the player piece off the board.
                            check2 = true;
                        }

                        if(enemyPieces[j].getY() == gameBoard.getY() || enemyPieces[j].getY() == gameBoard.getY() + gameBoard.getHeight() - tileWidth){

                            //Can't jump this piece as it would take the player piece off the board.
                            check2 = true;
                        }
                    }
                    occupied2 = true;
                }
            }
        }

        //Check if tile is occupied by friendly piece.
        for (int j = 0; j < 12; j++) {

            if (playerPieces[j].getX() == activePiece.getX() + tileWidth && playerPieces[j].getY() == activePiece.getY() - tileWidth) {

                System.out.println("Tile occupied.");
                check3 = true;
            }
        }

        if (!check3) {
            //Check if tile is occupied by enemy piece.
            for (int j = 0; j < 12; j++) {

                if (enemyPieces[j].getX() == activePiece.getX() + tileWidth && enemyPieces[j].getY() == activePiece.getY() - tileWidth) {

                    System.out.println("Tile occupied by enemy.");
                    //Check for possible jump by checking the next space over.

                    for (int k = 0; k < 12; k++) {

                        if (enemyPieces[k].getX() == enemyPieces[j].getX() + tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {
                            //A possible jump can't be made.
                            neighbourOccupied = true;
                            check3 = true;

                        }

                        if (playerPieces[k].getX() == enemyPieces[j].getX() + tileWidth && playerPieces[k].getY() == enemyPieces[j].getY() - tileWidth) {
                            //A possible jump can't be made.
                            System.out.println("Player piece in the way.");
                            neighbourOccupied = true;
                            check3 = true;

                        }

                        if(enemyPieces[j].getX() == width - tileWidth){

                            //Can't jump this piece as it would take the player piece off the board.
                            check3 = true;
                        }

                        if(enemyPieces[j].getY() == gameBoard.getY() || enemyPieces[j].getY() == gameBoard.getY() + gameBoard.getHeight() - tileWidth){

                            //Can't jump this piece as it would take the player piece off the board.
                            check3 = true;
                        }
                    }
                    occupied3 = true;
                }
            }
        }

        //Check if tile is occupied by friendly piece.
        for (int j = 0; j < 12; j++) {

            if (playerPieces[j].getX() == activePiece.getX() + tileWidth && playerPieces[j].getY() == activePiece.getY() + tileWidth) {

                System.out.println("Tile occupied.");
                check4 = true;
            }
        }

        if (!check4) {
            //Check if tile is occupied by enemy piece.
            for (int j = 0; j < 12; j++) {

                if (enemyPieces[j].getX() == activePiece.getX() + tileWidth && enemyPieces[j].getY() == activePiece.getY() + tileWidth) {

                    System.out.println("Tile occupied by enemy.");
                    //Check for possible jump by checking the next space over.

                    for (int k = 0; k < 12; k++) {

                        if (enemyPieces[k].getX() == enemyPieces[j].getX() + tileWidth && enemyPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                            //A possible jump can't be made.
                            neighbourOccupied = true;
                            check4 = true;
                        }

                        if (playerPieces[k].getX() == enemyPieces[j].getX() + tileWidth && playerPieces[k].getY() == enemyPieces[j].getY() + tileWidth) {
                            //A possible jump can't be made.
                            System.out.println("Player piece in the way.");
                            neighbourOccupied = true;
                            check4 = true;
                        }

                        if(enemyPieces[j].getX() == width - tileWidth){

                            //Can't jump this piece as it would take the player piece off the board.
                            check4 = true;
                        }

                        if(enemyPieces[j].getY() == gameBoard.getY() || enemyPieces[j].getY() == gameBoard.getY() + gameBoard.getHeight() - tileWidth){

                            //Can't jump this piece as it would take the player piece off the board.
                            check4 = true;
                        }
                    }
                    occupied4 = true;
                }
            }
        }

        if (activePiece.isKing() && !activePiece.isRed()) {

            //Check for any additional jumps.
            //Check if any spaces around the piece are occupied.
            if (occupied1 || occupied2 || occupied3 || occupied4) {
                //If so, check the next spaces over for a possible jump.
                if (check1 && check2 && check3 && check4) {

                    //All possible moves have been checked and none found.
                    return false;
                } else {
                    //There is a possible extra jump to be made
                    return true;
                }
            } else {

                System.out.println("Enemy occupies no neighbour tiles.");
                return false;
            }
        } else {

            if (occupied2 || occupied4) {
                if (!check2 && !check4) {

                    System.out.println("There is another possible jump");
                    return true;
                } else {

                    return false;
                }
            } else {


                System.out.println("Enemy occupies no neighbour tiles.");
                return false;
            }
        }
    }

    public void simOpponentTurn(int tileWidth){

        for(int i = 0; i < 12; i++) {

            if(enemyPieces[i].getX() == prevMoveX * tileWidth && enemyPieces[i].getY() == gameBoard.getY() + (prevMoveY * tileWidth)){

                System.out.println("Match found; moving enemy piece");
                //An opponent piece as moved so relocate it.
                enemyPieces[i].moveX((float)tileWidth * newMoveX);
                enemyPieces[i].moveY((float)gameBoard.getY() + (tileWidth * newMoveY));
            }else{

                System.out.println("Opponent hasn't moved.");
            }

        }
    }

    public void checkInactive(int tileWidth){

        int counter = 0;

        if(lostPointer != 99) {
            for (int i = 0; i < 12; i++) {

                if(i == lostPointer){

                    playerPieces[i].makeInactive();
                }
            }
        }

        lostPointer = 99;

        for(int i = 0; i < 12; i++){

            if(!playerPieces[i].isActive()){

                //Piece has been jumped. Resize and display on taken board.
                playerPieces[i].moveX((float)counter * 50);
                playerPieces[i].moveY((float)gameBoard.getY() - tileWidth);
            }

            if(!enemyPieces[i].isActive()){

                enemyPieces[i].moveX((float)counter * 50);
                enemyPieces[i].moveY((float)gameBoard.getY() + gameBoard.getHeight() + 5);
            }
        }

    }
}

