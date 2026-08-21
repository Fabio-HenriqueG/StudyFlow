package com.example.studyflow;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class ShapeDrawableHelper {

    public enum ShapeType {
        RECTANGLE, CIRCLE, SQUARE
    }

    public static GradientDrawable createShape(ShapeType type, int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.TRANSPARENT);
        gd.setStroke(5, color);
        
        switch (type) {
            case RECTANGLE:
            case SQUARE:
                gd.setShape(GradientDrawable.RECTANGLE);
                break;
            case CIRCLE:
                gd.setShape(GradientDrawable.OVAL);
                break;
        }
        return gd;
    }
}
