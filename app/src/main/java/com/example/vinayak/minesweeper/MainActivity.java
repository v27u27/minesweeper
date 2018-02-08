package com.example.vinayak.minesweeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    MyButton[][] grid;
    LinearLayout horizontalLayout[]; ;
    LinearLayout mainLayout;
    TextView minesBoard, scoreBoard ;
    MediaPlayer open, flag, unflag, won, lose, newgame ;
    Toast message;
    MenuItem rotationP ;
    SharedPreferences sharedpreferences ;
    SharedPreferences.Editor editor ;
    boolean muted ;
    boolean rotation ;
    int valuesX[] = {-1, -1, 0, 1, 1, 1, 0, -1};
    int valuesY[] = {0, 1, 1, 1, 0, -1, -1, -1};
    int MINES ;
    final static int MINED = -1 ;
    final static int EASY = 0 ;
    final static int MEDIUM = 1 ;
    final static int HARD = 2 ;
    int xSIZE = 8 ;
    int ySIZE = 10 ;
    int trueFlags = 0 ;
    int firstClick = 0 ;
    int flagCounter = MINES ;
    int visibleBlocks = 0 ;
    int score = 0 ;
    boolean isGameOver = false ;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = sharedpreferences.edit() ;

        Boolean rotation = !sharedpreferences.getBoolean("rotation", false) ;
        if(!rotation)    {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else  {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER) ;
        }

        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        minesBoard = (TextView) findViewById(R.id.mineBoard);
        scoreBoard = (TextView) findViewById(R.id.scoreBoard) ;
        scoreBoard.setText(score+" scored");
        minesBoard.setText(MINES + " left");

        //sounds
        newgame = MediaPlayer.create(this, R.raw.newgame) ;
        won = MediaPlayer.create(this, R.raw.won) ;
        lose = MediaPlayer.create(this, R.raw.lose) ;
        unflag = MediaPlayer.create(this, R.raw.unflag) ;
        open = MediaPlayer.create(this, R.raw.open);
        flag = MediaPlayer.create(this, R.raw.flag);

        setDimensions();
        newGame();

        muted = sharedpreferences.getBoolean("sound", false) ;
        if(!muted) newgame.start();
    }

    private void newGame()  {
        if(!muted) newgame.start();
        setUpBoard(xSIZE, ySIZE);
        minesBoard.setText(MINES+" left");
        setUpValues(MINES);
        flagCounter = MINES;
        isGameOver = false;
        visibleBlocks = 0 ;
        trueFlags = 0 ;
        score = 0 ;
        scoreBoard.setText(score+" scored");
        firstClick = 0 ;
    }

    public void setDimensions() {
        final OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int pWidth = mainLayout.getWidth();
                int pHeight = mainLayout.getHeight();
                xSIZE = pHeight / 80;
                ySIZE = pWidth / 80;
                newGame() ;
                if (Build.VERSION.SDK_INT < 16)
                    mainLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                else
                    mainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        };
        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    public void setUpBoard(int x, int y) {

        int level = sharedpreferences.getInt("level", EASY) ;
        if(level == EASY)   {
            //15 percent
            MINES = Math.round((xSIZE * ySIZE) * 15/100) ;
        } else if(level == MEDIUM)  {
            //22 percent
            MINES = Math.round((xSIZE * ySIZE) * 22/100) ;
        } else if(level == HARD)    {
            // 35 percent
            MINES = Math.round((xSIZE * ySIZE) * 35/100) ;
        }

        grid = new MyButton[x][y];
        mainLayout.removeAllViews();
        minesBoard.setText(MINES+" left");
        horizontalLayout = new LinearLayout[x];
        for (int i = 0; i < x; i++) {
            horizontalLayout[i] = new LinearLayout(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 5f/x);
            params.setMargins(0,0,0,0);
            horizontalLayout[i].setLayoutParams(params);
            horizontalLayout[i].setOrientation(LinearLayout.HORIZONTAL);
            mainLayout.addView(horizontalLayout[i]);
        }
        Log.i("Logs", "Layout Constructed ");
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                grid[i][j] = new MyButton(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                params.setMargins(1,1,1,1);
                grid[i][j].setLayoutParams(params);
                horizontalLayout[i].addView(grid[i][j]);
                grid[i][j].row = i;
                grid[i][j].col = j;
                grid[i][j].setPadding(0,0,0,2);
                grid[i][j].setTextSize(20f);
                grid[i][j].setTextColor(ContextCompat.getColor(this, R.color.textColor));
                grid[i][j].setGravity(Gravity.CENTER);
                grid[i][j].applyChanges(ButtonConstants.DEFAULT) ;
                grid[i][j].setOnClickListener(this);
                grid[i][j].setOnLongClickListener(this);
            }
        }
        Log.i("Logs", "buttons added ");
    }

    private void setUpValues(int n) {
        //adding mines
        int row, col;
        Random num = new Random();
        while (n != 0) {
            row = num.nextInt(xSIZE);
            col = num.nextInt(ySIZE);
            if (grid[row][col].value != MINED) {
                grid[row][col].value = MINED;
                n--;
                for (int i = 0; i < valuesX.length; i++) {
                    int tempR = row + valuesX[i];
                    int tempC = col + valuesY[i];
                    if (tempC < 0 || tempR < 0 || tempC >= ySIZE || tempR >= xSIZE)
                        continue;
                    if (grid[tempR][tempC].value == MINED)
                        continue;

                    grid[tempR][tempC].validFlagCounter = ++grid[tempR][tempC].value;

                }
            }
        }

        Log.i("Logs", "mines added");
    }

    private void reveal(MyButton currentButton) {
        if (!muted) open.start();
        score += currentButton.value ;

        if(currentButton.value == 0)    {
            currentButton.setText("");
            currentButton.applyChanges(ButtonConstants.ZERO_VALUE);
        }
        if(currentButton.value != MINED && currentButton.value != 0) {
            currentButton.setText("" + currentButton.value);
            currentButton.applyChanges(ButtonConstants.SOME_VALUE) ;
        }
        scoreBoard.setText(score + " scored");
        currentButton.isVisible = true ;
        visibleBlocks++ ;

        if (currentButton.value == 0) {
            for (int i = 0; i < valuesX.length; i++) {
                int tempR = currentButton.row + valuesX[i];
                int tempC = currentButton.col + valuesY[i];
                if (tempC < 0 || tempR < 0 || tempC >= ySIZE || tempR >= xSIZE)
                    continue;
                if (grid[tempR][tempC].value == MINED)
                    continue;
                if (grid[tempR][tempC].isFlagged)
                    continue;

                if(!grid[tempR][tempC].isVisible)
                    reveal(grid[tempR][tempC]);
            }
        }
    }

    private void redundantBlocks(MyButton currentButton) {
        for(int i = 0; i < valuesX.length; i++)  {
            int tempR = currentButton.row + valuesX[i];
            int tempC = currentButton.col + valuesY[i];
            if (tempC < 0 || tempR < 0 || tempC >= ySIZE || tempR >= xSIZE)
                continue;

            if(currentButton.validFlagCounter == 0 && !currentButton.isVisible)
                reveal(grid[tempR][tempC]);
        }
    }

    private void trueFlagValuesUpdater(MyButton currentButton, boolean doIt) {
        for(int i = 0; i < valuesX.length; i++) {
            int tempR = currentButton.row + valuesX[i];
            int tempC = currentButton.col + valuesY[i];
            if (tempC < 0 || tempR < 0 || tempC >= ySIZE || tempR >= xSIZE)
                continue;
            if (grid[tempR][tempC].value == MINED)
                continue;

            if (doIt)
                grid[tempR][tempC].validFlagCounter--;
            else
                grid[tempR][tempC].validFlagCounter++;
        }
    }

    public void onClick(View v) {
        MyButton currentButton = (MyButton) v;

        //first click handler
        if (firstClick == 0 && currentButton.value == MINED) {
            firstClick++;
            return;
        } else
            firstClick++;

        if (trueFlags == MINES || visibleBlocks == (xSIZE * ySIZE - MINES)) {
            if (!muted) won.start();
            message = Toast.makeText(this, "Game Won", Toast.LENGTH_SHORT);
            message.show();
            isGameOver = true;
        }

        if (currentButton.isFlagged || currentButton.isVisible)
            return;

        //redundant revealer
        if(currentButton.isVisible) {
            redundantBlocks(currentButton) ;
        }else
            reveal(currentButton);

        //game over
        if (currentButton.value == MINED) {
            isGameOver = true;
            for (int i = 0; i < xSIZE; i++) {
                for (int j = 0; j < ySIZE; j++) {
                    if (grid[i][j].isFlagged && grid[i][j].value == MINED)
                        continue;
                    if (grid[i][j].value == MINED) {
                        grid[i][j].applyChanges(ButtonConstants.BOMB) ;
                    } else {
                        if (grid[i][j].isFlagged) {
                            grid[i][j].applyChanges(ButtonConstants.FALSEFLAG);
                        }
                        if(grid[i][j].value == 0) {
                            grid[i][j].setText("");
                            grid[i][j].applyChanges(ButtonConstants.ZERO_VALUE);
                        } else
                            grid[i][j].setText("" + grid[i][j].value);
                    }
                    grid[i][j].setEnabled(false);
                }
            }
            if (!muted) lose.start();
            message = Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT);
            scoreBoard.setText("Lose");
            message.show();
        }
    }

    public boolean onLongClick(View v) {
        MyButton currentButton = (MyButton) v;
        if(currentButton.isVisible)
            return false;

        if (currentButton.isFlagged == false) {
            if(flagCounter > 0) {
                currentButton.isFlagged = true;
                currentButton.applyChanges(ButtonConstants.FLAG);
                if (!muted) flag.start();
                flagCounter--;
                if (currentButton.value == MINED)
                    trueFlags++;
            }
        }
        else    {
            currentButton.isFlagged = false ;
            flagCounter++ ;
            if(!muted) unflag.start();
            currentButton.applyChanges(ButtonConstants.DEFAULT) ;
            if(currentButton.value == MINED)
                trueFlags-- ;
        }
        trueFlagValuesUpdater(currentButton, currentButton.isFlagged) ;
        minesBoard.setText(flagCounter + " left");

        if(trueFlags == MINES || visibleBlocks == (xSIZE*ySIZE - MINES)) {
            message = Toast.makeText(this, "Game Won", Toast.LENGTH_SHORT);
            message.show();
            isGameOver = true;
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //// TODO: 26-06-2017 buggy
        Log.i("Log", "onPrepareOptionsMenu: ");
        int currLevel = sharedpreferences.getInt("level", EASY) ;
        MenuItem level = menu.findItem(R.id.levelEasy) ;
        if(currLevel == EASY)   {
            level = menu.findItem(R.id.levelEasy) ;
        } else if (currLevel == MEDIUM) {
            level = menu.findItem(R.id.levelMed) ;
        } else if (currLevel == HARD)   {
            level = menu.findItem(R.id.levelHard) ;
        }
        level.setChecked(true) ;

        MenuItem rotation = menu.findItem(R.id.rotation) ;
        rotation.setChecked(!sharedpreferences.getBoolean("rotation", false)) ;

        MenuItem mute = menu.findItem(R.id.sound) ;
        mute.setChecked(!sharedpreferences.getBoolean("sound", true)) ;

        return true ;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.newGame) {
            newGame();
        }

        if (id == R.id.sound)    {
            if(item.isChecked()) {
                muted = true ;
            } else  {
                muted = false ;
            }
//            item.setChecked(!muted) ;
            editor.putBoolean("sound", muted) ;
            editor.commit() ;
        }

        //solve with shared preference // TODO: 21-06-2017   buggy
        if (id == R.id.rotation)    {
            if(item.isChecked()) {
                rotation = false ;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else  {
                rotation = true ;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }
            newGame();
//            item.setChecked(!rotation) ;
            editor.putBoolean("rotationPrefs", rotation) ;
            editor.commit() ;
        }

        if (id == R.id.help)    {
            String url = "https://www.cs.cmu.edu/~rvirga/MineSweeper_instructions.html";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }

        if(id == R.id.levelEasy)    {
            MINES = (xSIZE * ySIZE) * 15/100 ;
            editor.putInt("level", EASY) ;
            editor.commit() ;
//            item.setChecked(true) ;
            newGame();
        } else if (id == R.id.levelMed) {
            MINES = (xSIZE * ySIZE) * 22/100 ;
            editor.putInt("level", MEDIUM) ;
            editor.commit() ;
            newGame();
//            item.setChecked(true) ;
        } else if (id == R.id.levelHard) {
            MINES = (xSIZE * ySIZE) * 35/100 ;
            editor.putInt("level", HARD) ;
            editor.commit() ;
            newGame();
//            item.setChecked(true) ;
        }
        return true ;
    }
}