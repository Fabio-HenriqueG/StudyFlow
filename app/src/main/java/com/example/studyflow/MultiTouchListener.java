package com.example.studyflow;

import android.content.Context;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Listener avançado para manipulação de views (imagens, textos, formas) com gestos multi-touch.
 * Oferece translação, escala e rotação fluida sem pulos ou disparos ao soltar os dedos.
 */
public class MultiTouchListener implements OnTouchListener {

    private final GestureDetector mGestureDetector;
    private View mView;

    // Rastreamento para 1 dedo
    private float mPrevRawX;
    private float mPrevRawY;

    // Rastreamento para 2 dedos
    private float mPrevMidX;
    private float mPrevMidY;
    private float mPrevDist = 1f;
    private float mPrevDegree = 0f;

    private boolean isZoomEnabled = true;
    private boolean isRotationEnabled = true;
    private boolean isTranslateEnabled = true;
    private boolean isSnapEnabled = false; // Desativado por padrão para movimento livre e suave
    private int gridSize = 60;

    private float minimumScale = 0.2f;
    private float maximumScale = 5.0f;

    public MultiTouchListener(Context context) {
        this(context, true);
    }

    public MultiTouchListener(Context context, boolean allowZoom) {
        this.isZoomEnabled = allowZoom;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (mView != null) {
                    mView.performLongClick();
                }
            }
        });
    }

    public void setSnapEnabled(boolean snapEnabled) {
        this.isSnapEnabled = snapEnabled;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public void setZoomEnabled(boolean zoomEnabled) {
        this.isZoomEnabled = zoomEnabled;
    }

    public void setRotationEnabled(boolean rotationEnabled) {
        this.isRotationEnabled = rotationEnabled;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        this.mView = view;
        mGestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mPrevRawX = getRawX(event, 0, view);
                mPrevRawY = getRawY(event, 0, view);
                view.bringToFront();
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                if (event.getPointerCount() >= 2) {
                    float x0 = getRawX(event, 0, view);
                    float y0 = getRawY(event, 0, view);
                    float x1 = getRawX(event, 1, view);
                    float y1 = getRawY(event, 1, view);

                    mPrevMidX = (x0 + x1) / 2f;
                    mPrevMidY = (y0 + y1) / 2f;

                    float dx = x0 - x1;
                    float dy = y0 - y1;
                    mPrevDist = (float) Math.sqrt(dx * dx + dy * dy);
                    mPrevDegree = (float) Math.toDegrees(Math.atan2(dy, dx));
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                int pointerCount = event.getPointerCount();
                float parentScale = getParentScale(view);

                if (pointerCount == 1) {
                    float currRawX = getRawX(event, 0, view);
                    float currRawY = getRawY(event, 0, view);

                    float deltaX = (currRawX - mPrevRawX) / parentScale;
                    float deltaY = (currRawY - mPrevRawY) / parentScale;

                    if (isTranslateEnabled) {
                        float nextX = view.getTranslationX() + deltaX;
                        float nextY = view.getTranslationY() + deltaY;

                        if (isSnapEnabled) {
                            view.setTranslationX(Math.round(nextX / gridSize) * (float) gridSize);
                            view.setTranslationY(Math.round(nextY / gridSize) * (float) gridSize);
                        } else {
                            view.setTranslationX(nextX);
                            view.setTranslationY(nextY);
                        }
                    }

                    mPrevRawX = currRawX;
                    mPrevRawY = currRawY;

                } else if (pointerCount >= 2) {
                    float x0 = getRawX(event, 0, view);
                    float y0 = getRawY(event, 0, view);
                    float x1 = getRawX(event, 1, view);
                    float y1 = getRawY(event, 1, view);

                    float currMidX = (x0 + x1) / 2f;
                    float currMidY = (y0 + y1) / 2f;

                    // 1. Translação pelo ponto médio dos dedos
                    if (isTranslateEnabled) {
                        float deltaMidX = (currMidX - mPrevMidX) / parentScale;
                        float deltaMidY = (currMidY - mPrevMidY) / parentScale;

                        view.setTranslationX(view.getTranslationX() + deltaMidX);
                        view.setTranslationY(view.getTranslationY() + deltaMidY);
                    }

                    // 2. Zoom / Escala proporcional à distância entre os dedos em pixels de tela
                    if (isZoomEnabled) {
                        float dx = x0 - x1;
                        float dy = y0 - y1;
                        float newDist = (float) Math.sqrt(dx * dx + dy * dy);

                        if (newDist > 10f && mPrevDist > 10f) {
                            float scaleFactor = newDist / mPrevDist;
                            float currentScale = view.getScaleX() * scaleFactor;

                            if (currentScale >= minimumScale && currentScale <= maximumScale) {
                                view.setScaleX(currentScale);
                                view.setScaleY(currentScale);
                            }
                            mPrevDist = newDist;
                        }
                    }

                    // 3. Rotação baseada no ângulo entre os dois pontos
                    if (isRotationEnabled) {
                        float dx = x0 - x1;
                        float dy = y0 - y1;
                        float newDegree = (float) Math.toDegrees(Math.atan2(dy, dx));
                        float deltaDegree = newDegree - mPrevDegree;

                        view.setRotation(view.getRotation() + deltaDegree);
                        mPrevDegree = newDegree;
                    }

                    mPrevMidX = currMidX;
                    mPrevMidY = currMidY;
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                // Quando um dos dedos é levantado, transiciona suavemente para o dedo restante sem pulo.
                int actionIndex = event.getActionIndex();
                int remainingIndex = (actionIndex == 0) ? 1 : 0;

                if (remainingIndex < event.getPointerCount()) {
                    mPrevRawX = getRawX(event, remainingIndex, view);
                    mPrevRawY = getRawY(event, remainingIndex, view);
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (isSnapEnabled) {
                    float nextX = view.getTranslationX();
                    float nextY = view.getTranslationY();
                    view.setTranslationX(Math.round(nextX / gridSize) * (float) gridSize);
                    view.setTranslationY(Math.round(nextY / gridSize) * (float) gridSize);
                }
                view.performClick();
                break;
            }
        }
        return true;
    }

    private float getRawX(MotionEvent event, int pointerIndex, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return event.getRawX(pointerIndex);
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        float[] pts = new float[]{event.getX(pointerIndex), event.getY(pointerIndex)};
        view.getMatrix().mapPoints(pts);
        return location[0] + pts[0];
    }

    private float getRawY(MotionEvent event, int pointerIndex, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return event.getRawY(pointerIndex);
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        float[] pts = new float[]{event.getX(pointerIndex), event.getY(pointerIndex)};
        view.getMatrix().mapPoints(pts);
        return location[1] + pts[1];
    }

    private float getParentScale(View view) {
        if (view != null && view.getParent() instanceof View) {
            float scale = ((View) view.getParent()).getScaleX();
            return scale > 0f ? scale : 1.0f;
        }
        return 1.0f;
    }
}
