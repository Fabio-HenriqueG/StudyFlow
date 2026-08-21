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
    private float mInitialRawX;
    private float mInitialRawY;
    private float mInitialTranslationX;
    private float mInitialTranslationY;
    private float mOldDist = 1f;
    private float mOldDegree = 0f;
    private boolean isZoomEnabled = true;
    private boolean isRotationEnabled = true;
    private boolean isSnapEnabled = true;
    private int gridSize = 60;

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
                mInitialRawX = event.getRawX();
                mInitialRawY = event.getRawY();
                mInitialTranslationX = view.getTranslationX();
                mInitialTranslationY = view.getTranslationY();
                view.bringToFront();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mOldDist = calcularDistancia(event);
                mOldDegree = calcularAngulo(event);
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    float currRawX = event.getRawX();
                    float currRawY = event.getRawY();
                    
                    // Calcula a escala do pai (canvas) para ajustar a velocidade do movimento
                    float scaleFactor = 1.0f;
                    if (view.getParent() instanceof View) {
                        scaleFactor = ((View) view.getParent()).getScaleX();
                    }

                    // Ajusta o delta pelo fator de zoom para o item seguir exatamente o dedo
                    float deltaX = (currRawX - mInitialRawX) / scaleFactor;
                    float deltaY = (currRawY - mInitialRawY) / scaleFactor;

                    float nextX = mInitialTranslationX + deltaX;
                    float nextY = mInitialTranslationY + deltaY;

                    if (isSnapEnabled) {
                        // Snap fluido: ele segue o dedo e "prende" levemente nas linhas
                        view.setTranslationX(Math.round(nextX / gridSize) * gridSize);
                        view.setTranslationY(Math.round(nextY / gridSize) * gridSize);
                    } else {
                        view.setTranslationX(nextX);
                        view.setTranslationY(nextY);
                    }

                    mPrevRawX = currRawX;
                    mPrevRawY = currRawY;
                } else if (event.getPointerCount() == 2) {


                    if (isZoomEnabled) {
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
                    if (isRotationEnabled) {
                        float newDegree = calcularAngulo(event);
                        float deltaDegree = newDegree - mOldDegree;
                        view.setRotation(view.getRotation() + deltaDegree);
                        mOldDegree = newDegree;
                    }
                }
                break;


            case MotionEvent.ACTION_POINTER_UP:
                // SOLUÇÃO PARA O DISPARO:
                // Identifica qual dedo continuará na tela (o que NÃO é o 'actionIndex')
                int pointerIndex = (event.getActionIndex() == 0) ? 1 : 0;
                
                // Atualiza o âncora para o dedo que SOBROU para evitar o "pulo"
                mInitialRawX = event.getX(pointerIndex) + (event.getRawX() - event.getX());
                mInitialRawY = event.getY(pointerIndex) + (event.getRawY() - event.getY());
                mInitialTranslationX = view.getTranslationX();
                mInitialTranslationY = view.getTranslationY();
                
                mPrevRawX = mInitialRawX;
                mPrevRawY = mInitialRawY;
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

    private float calcularAngulo(MotionEvent event) {
        double deltaX = (event.getX(0) - event.getX(1));
        double deltaY = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(deltaY, deltaX);
        return (float) Math.toDegrees(radians);
    }
}
