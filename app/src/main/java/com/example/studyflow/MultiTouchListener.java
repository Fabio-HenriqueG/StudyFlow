package com.example.studyflow;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Listener avançado que corrige o "disparo" (pulo) dos itens ao soltar o dedo.
 */
public class MultiTouchListener implements OnTouchListener {

    private final GestureDetector mGestureDetector;
    private View mView;
    private float mPrevRawX;
    private float mPrevRawY;
    private float mOldDist = 1f;
    private boolean isZoomEnabled = true;

    public MultiTouchListener(android.content.Context context) {
        this(context, true);
    }

    public MultiTouchListener(android.content.Context context, boolean allowZoom) {
        this.isZoomEnabled = allowZoom;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (mView != null) mView.performLongClick();
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        this.mView = view;
        mGestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mPrevRawX = event.getRawX();
                mPrevRawY = event.getRawY();
                view.bringToFront();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mOldDist = calcularDistancia(event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    float currRawX = event.getRawX();
                    float currRawY = event.getRawY();
                    float deltaX = currRawX - mPrevRawX;
                    float deltaY = currRawY - mPrevRawY;

                    if (Math.abs(deltaX) > 0.1f || Math.abs(deltaY) > 0.1f) {
                        view.setTranslationX(view.getTranslationX() + deltaX);
                        view.setTranslationY(view.getTranslationY() + deltaY);
                    }

                    mPrevRawX = currRawX;
                    mPrevRawY = currRawY;
                } else if (event.getPointerCount() == 2 && isZoomEnabled) {
                    float newDist = calcularDistancia(event);
                    if (newDist > 10f) {
                        float scaleFactor = newDist / mOldDist;
                        float currentScale = view.getScaleX() * scaleFactor;
                        if (currentScale > 0.2f && currentScale < 5.0f) {
                            view.setScaleX(currentScale);
                            view.setScaleY(currentScale);
                        }
                        mOldDist = newDist;
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // SOLUÇÃO PARA O DISPARO:
                // Identifica qual dedo continuará na tela (o que NÃO é o 'actionIndex')
                int pointerIndex = (event.getActionIndex() == 0) ? 1 : 0;
                
                // Atualiza a posição anterior usando as coordenadas do dedo que SOBROU
                // Como não podemos usar getRawX(index) em APIs < 29 de forma direta, 
                // usamos a diferença entre a coordenada local e a absoluta.
                mPrevRawX = event.getX(pointerIndex) + (event.getRawX() - event.getX());
                mPrevRawY = event.getY(pointerIndex) + (event.getRawY() - event.getY());
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                view.performClick();
                break;
        }
        return true;
    }

    private float calcularDistancia(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
}
