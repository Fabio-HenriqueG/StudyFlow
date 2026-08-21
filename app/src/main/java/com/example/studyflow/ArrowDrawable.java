package com.example.studyflow;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ArrowDrawable extends Drawable {
    private final Paint paint = new Paint();
    private final Path path = new Path();

    public ArrowDrawable(int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        float w = bounds.width();
        float h = bounds.height();

        path.reset();
        // Desenha uma seta apontando para a direita no espaço do bound
        // Corpo da seta
        path.moveTo(0, h * 0.4f);
        path.lineTo(w * 0.6f, h * 0.4f);
        // Cabeça da seta
        path.lineTo(w * 0.6f, h * 0.2f);
        path.lineTo(w, h * 0.5f);
        path.lineTo(w * 0.6f, h * 0.8f);
        path.lineTo(w * 0.6f, h * 0.6f);
        // Fecha o corpo
        path.lineTo(0, h * 0.6f);
        path.close();

        canvas.drawPath(path, paint);
    }

    @Override
    public void setAlpha(int alpha) { paint.setAlpha(alpha); }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) { paint.setColorFilter(colorFilter); }

    @Override
    public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
