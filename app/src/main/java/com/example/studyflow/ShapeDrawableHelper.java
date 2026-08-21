package com.example.studyflow;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class ShapeDrawableHelper {

    public enum ShapeType {
        RECTANGLE, CIRCLE, SQUARE, ARROW
    }

    public static android.graphics.drawable.Drawable createShape(ShapeType type, int color, boolean filled) {
        if (type == ShapeType.ARROW) {
            return new ArrowDrawable(color);
        }
        
        GradientDrawable gd = new GradientDrawable();
        if (filled) {
            gd.setColor(color);
            gd.setStroke(0, android.graphics.Color.TRANSPARENT);
        } else {
            gd.setColor(android.graphics.Color.TRANSPARENT);
            gd.setStroke(8, color);
        }
        
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
