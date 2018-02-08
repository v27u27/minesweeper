package com.example.vinayak.minesweeper;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;

public class MyButton extends AppCompatButton {
    int value = 0;
    boolean isFlagged = false;
    boolean isVisible = false;
    int validFlagCounter = 0;
    int row;
    int col;
    Context c ;

    public MyButton(Context context) {

        super(context);
        c = context ;
    }

    public void applyChanges(int state)  {
        switch (state)  {
            case ButtonConstants.DEFAULT :
                super.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.button));
                break ;
            case ButtonConstants.FALSEFLAG :
            case ButtonConstants.ZERO_VALUE :
                super.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.null_box));
                break ;
            case ButtonConstants.FLAG :
                super.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.flag));
                break ;
            case ButtonConstants.BOMB :
                super.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.bombed));
                break ;
        }

    }
}