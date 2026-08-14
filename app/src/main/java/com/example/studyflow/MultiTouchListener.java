package com.example.studyflow;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Listener avançado com Zoom e Arraste estabilizados.
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
                // Quando o segundo dedo toca, reseta a distância inicial de referência
                mOldDist = calcularDistancia(event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    // ARRASTAR (1 dedo) - Movimento livre
                    float currRawX = event.getRawX();
                    float currRawY = event.getRawY();
                    float deltaX = currRawX - mPrevRawX;
                    float deltaY = currRawY - mPrevRawY;

                    view.setTranslationX(view.getTranslationX() + deltaX);
                    view.setTranslationY(view.getTranslationY() + deltaY);

                    mPrevRawX = currRawX;
                    mPrevRawY = currRawY;
                } else if (event.getPointerCount() == 2 && isZoomEnabled) {
                    // ZOOM (Apenas se habilitado)
                    float newDist = calcularDistancia(event);
                    if (newDist > 10f) {
                        float scaleFactor = newDist / mOldDist;
                        
                        // Aplica a escala gradualmente para não perder o controle
                        float currentScale = view.getScaleX() * scaleFactor;
                        
                        // Limites: não deixa ficar menor que 20% nem maior que 500%
                        if (currentScale > 0.2f && currentScale < 5.0f) {
                            view.setScaleX(currentScale);
                            view.setScaleY(currentScale);
                        }
                        mOldDist = newDist;
                    }
                    
                    // Rotação: Removida do fundo para evitar que a folha inteira gire por acidente
                    // mas mantida nos objetos individuais se necessário.
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // Sincroniza ao levantar um dos dedos para evitar "pulos"
                mPrevRawX = event.getRawX();
                mPrevRawY = event.getRawY();
                break;
                
            case MotionEvent.ACTION_UP:
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
