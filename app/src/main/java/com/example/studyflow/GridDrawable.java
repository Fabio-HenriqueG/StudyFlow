package com.example.studyflow;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Desenha a folha de caderno com grade cinza, otimizado e visível em todos os níveis de zoom.
 */
public class GridDrawable extends Drawable {

    public enum Style { BLANK, GRID, LINES, DOTTED }

    private final Paint paintGrid = new Paint();
    private final Paint paintBackground = new Paint();
    private final int cellSize = 60;
    private Style style = Style.GRID;

    public GridDrawable() {
        this(Style.GRID);
    }

    public GridDrawable(Style style) {
        this.style = style;
        paintGrid.setColor(Color.parseColor("#E0E0E0"));
        paintGrid.setStrokeWidth(1.5f); // Aumentado levemente para não sumir no zoom out
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setAntiAlias(true); // Reativado para manter visibilidade em escalas pequenas

        paintBackground.setColor(Color.WHITE);
        paintBackground.setStyle(Paint.Style.FILL);
    }

    public void setStyle(Style style) {
        this.style = style;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        
        // 1. Desenha o fundo branco
        canvas.drawRect(bounds, paintBackground);

        if (style == Style.BLANK) return;

        // 2. Desenha o estilo selecionado
        // Adicionamos uma pequena margem de segurança no loop para garantir cobertura total
        if (style == Style.GRID || style == Style.DOTTED) {
            for (int x = bounds.left; x <= bounds.right; x += cellSize) {
                if (style == Style.GRID) {
                    canvas.drawLine(x, bounds.top, x, bounds.bottom, paintGrid);
                } else {
                    for (int y = bounds.top; y <= bounds.bottom; y += cellSize) {
                        canvas.drawPoint(x, y, paintGrid);
                    }
                }
            }
        }

        if (style == Style.GRID || style == Style.LINES) {
            for (int y = bounds.top; y <= bounds.bottom; y += cellSize) {
                canvas.drawLine(bounds.left, y, bounds.right, y, paintGrid);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paintGrid.setAlpha(alpha);
        paintBackground.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paintGrid.setColorFilter(colorFilter);
        paintBackground.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() { return PixelFormat.OPAQUE; }
}
