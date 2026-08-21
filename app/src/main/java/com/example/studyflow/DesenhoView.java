package com.example.studyflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom View que permite ao usuário desenhar com o dedo.
 */
public class DesenhoView extends View {

    private Path drawPath;
    private Paint drawPaint, canvasPaint, previewPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private int paintColor = Color.BLACK;
    private float strokeWidth = 10f;
    private boolean eraserMode = false;
    private boolean drawingEnabled = false;

    private List<Stroke> strokes = new ArrayList<>();
    private List<Stroke> undoneStrokes = new ArrayList<>();

    private static class Stroke {
        Path path;
        int color;
        float width;
        boolean isEraser;

        Stroke(Path path, int color, float width, boolean isEraser) {
            this.path = path;
            this.color = color;
            this.width = width;
            this.isEraser = isEraser;
        }
    }

    public DesenhoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // Essencial para a borracha funcionar em Bitmaps transparentes
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(strokeWidth);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        previewPaint = new Paint();
        previewPaint.setAntiAlias(true);
        previewPaint.setStyle(Paint.Style.STROKE);
        previewPaint.setStrokeJoin(Paint.Join.ROUND);
        previewPaint.setStrokeCap(Paint.Cap.ROUND);
        previewPaint.setColor(Color.LTGRAY);
        previewPaint.setAlpha(120);
    }

    public void setCor(int novaCor) {
        eraserMode = false;
        paintColor = novaCor;
        drawPaint.setColor(paintColor);
        drawPaint.setXfermode(null);
        invalidate();
    }

    public void setBorracha(boolean ativa) {
        eraserMode = ativa;
        if (eraserMode) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
            drawPaint.setColor(paintColor);
        }
    }

    public void setDrawingEnabled(boolean enabled) {
        this.drawingEnabled = enabled;
    }

    public void setTamanhoPincel(float novoTamanho) {
        strokeWidth = novoTamanho;
        drawPaint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        // Só desenha o path atual na View se NÃO for borracha.
        // Se for borracha, desenha um rastro semitransparente para visibilidade.
        if (!eraserMode) {
            canvas.drawPath(drawPath, drawPaint);
        } else {
            previewPaint.setStrokeWidth(strokeWidth);
            canvas.drawPath(drawPath, previewPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!drawingEnabled) return false;
        
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                undoneStrokes.clear();
                drawPath.moveTo(touchX, touchY);
                // Se for borracha, já começa a apagar no ponto do toque
                if (eraserMode) {
                    drawPath.lineTo(touchX, touchY);
                    drawCanvas.drawPath(drawPath, drawPaint);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                // Aplica a borracha em tempo real no bitmap para feedback visual
                if (eraserMode) {
                    drawCanvas.drawPath(drawPath, drawPaint);
                }
                break;
            case MotionEvent.ACTION_UP:
                // Se não for borracha, desenha o path final no bitmap agora.
                // Se for borracha, já foi desenhado durante o MOVE.
                if (!eraserMode) {
                    drawCanvas.drawPath(drawPath, drawPaint);
                }
                
                // Salva o stroke para undo/redo
                strokes.add(new Stroke(new Path(drawPath), paintColor, strokeWidth, eraserMode));
                
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void desfazer() {
        if (strokes.size() > 0) {
            Stroke removed = strokes.remove(strokes.size() - 1);
            undoneStrokes.add(removed);
            redesenharTudo();
        }
    }

    public void refazer() {
        if (undoneStrokes.size() > 0) {
            Stroke recovered = undoneStrokes.remove(undoneStrokes.size() - 1);
            strokes.add(recovered);
            redesenharTudo();
        }
    }

    private void redesenharTudo() {
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        
        Paint tempPaint = new Paint(drawPaint);
        for (Stroke s : strokes) {
            tempPaint.setColor(s.color);
            tempPaint.setStrokeWidth(s.width);
            if (s.isEraser) {
                tempPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            } else {
                tempPaint.setXfermode(null);
            }
            drawCanvas.drawPath(s.path, tempPaint);
        }
        invalidate();
    }

    public void limpar() {
        strokes.clear();
        undoneStrokes.clear();
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public Bitmap getBitmap() {
        return canvasBitmap;
    }
}
