package com.example.studyflow;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Desenha a folha de caderno com grade cinza, sem cantos arredondados.
 */
public class GridDrawable extends Drawable {

    private final Paint paintGrid = new Paint();
    private final Paint paintBackground = new Paint();
    private final int cellSize = 60;

    public GridDrawable() {
        paintGrid.setColor(Color.parseColor("#E0E0E0"));
        paintGrid.setStrokeWidth(1.2f);
        paintGrid.setStyle(Paint.Style.STROKE);

        paintBackground.setColor(Color.WHITE);
        paintBackground.setStyle(Paint.Style.FILL);
        paintBackground.setAntiAlias(true);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        
        // 1. Desenha o fundo branco (sem cantos arredondados)
        canvas.drawRect(bounds, paintBackground);

        // 2. Desenha as linhas da grade
        for (int x = bounds.left; x < bounds.right; x += cellSize) {
            canvas.drawLine(x, bounds.top, x, bounds.bottom, paintGrid);
        }
        for (int y = bounds.top; y < bounds.bottom; y += cellSize) {
            canvas.drawLine(bounds.left, y, bounds.right, y, paintGrid);
        }
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
