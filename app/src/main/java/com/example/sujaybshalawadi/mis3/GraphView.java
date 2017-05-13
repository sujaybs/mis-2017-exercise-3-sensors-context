package com.example.sujaybshalawadi.mis3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {

    private static final float TOP_MAGNITUDE = 150.0f;

    private Paint redLine;
    private Paint greenLine;
    private Paint blueLine;
    private Paint whiteLine;
    private Paint blackBackground;

    private RectF backgroundRect;

    private float[] pointBufferX;
    private float[] pointBufferY;
    private float[] pointBufferZ;
    private float[] pointBufferM;

    public GraphView(Context context) {
        super(context, null, 0);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        redLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        redLine.setColor(Color.RED);
        redLine.setStrokeWidth(4.0f);

        blueLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        blueLine.setColor(Color.BLUE);
        blueLine.setStrokeWidth(4.0f);

        greenLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenLine.setColor(Color.GREEN);
        greenLine.setStrokeWidth(4.0f);

        whiteLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteLine.setColor(Color.WHITE);
        whiteLine.setStrokeWidth(4.0f);

        blackBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackBackground.setColor(Color.BLACK);

    }

//    public void pushValues(float x, float y, float z, float m) {
//        if (backgroundRect == null) {
//            return;
//        }
//
//        float coefficientHeight = backgroundRect.height() / TOP_MAGNITUDE;
//        float coefficientWidth = backgroundRect.width() / POINT_WINDOW;
//
//        pointVecs.x.push(x * coefficientHeight);
//        pointVecs.x.push(pointVecs.x.size() / 2 * coefficientWidth);
//        pointVecs.y.push(y * coefficientHeight);
//        pointVecs.y.push(pointVecs.y.size() / 2 * coefficientWidth);
//        pointVecs.z.push(z * coefficientHeight);
//        pointVecs.z.push(pointVecs.z.size() / 2 * coefficientWidth);
//        pointVecs.m.push(m * coefficientHeight);
//        pointVecs.m.push(pointVecs.m.size() / 2 * coefficientWidth);
//
//        if (pointVecs.x.size() > POINT_WINDOW) {
//            pointVecs.x.removeLast();
//            pointVecs.x.removeLast();
//            pointVecs.y.removeLast();
//            pointVecs.y.removeLast();
//            pointVecs.z.removeLast();
//            pointVecs.z.removeLast();
//            pointVecs.m.removeLast();
//            pointVecs.m.removeLast();
//        }
//
//        float coefficientHeight = backgroundRect.height() / TOP_MAGNITUDE;
//        float coefficientWidth = backgroundRect.width() / POINT_WINDOW;
//
//        pointVecs.x.push(x * coefficientHeight);
//        pointVecs.x.push(pointVecs.x.size() / 2 * coefficientWidth);
//        pointVecs.y.push(y * coefficientHeight);
//        pointVecs.y.push(pointVecs.y.size() / 2 * coefficientWidth);
//        pointVecs.z.push(z * coefficientHeight);
//        pointVecs.z.push(pointVecs.z.size() / 2 * coefficientWidth);
//        pointVecs.m.push(m * coefficientHeight);
//        pointVecs.m.push(pointVecs.m.size() / 2 * coefficientWidth);
//
//        adjustValues();
//        invalidate();
//    }

//    private void adjustValues() {
//        pointBufferX = ArrayUtils.toPrimitive(pointVecs.x.toArray(new Float[POINT_WINDOW]), 0.0F);
//        pointBufferY = ArrayUtils.toPrimitive(pointVecs.y.toArray(new Float[POINT_WINDOW]), 0.0F);
//        pointBufferZ = ArrayUtils.toPrimitive(pointVecs.z.toArray(new Float[POINT_WINDOW]), 0.0F);
//        pointBufferM = ArrayUtils.toPrimitive(pointVecs.m.toArray(new Float[POINT_WINDOW]), 0.0F);
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        backgroundRect = new RectF(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (backgroundRect != null)
            canvas.drawRect(backgroundRect, blackBackground);

        if (pointBufferX != null)
            canvas.drawLines(pointBufferX, redLine);
        if (pointBufferY != null)
            canvas.drawLines(pointBufferY, blueLine);
        if (pointBufferZ != null)
            canvas.drawLines(pointBufferZ, greenLine);
        if (pointBufferM != null)
            canvas.drawLines(pointBufferM, whiteLine);
    }

    public void setBuffers(float[] pointBufferX, float[] pointBufferY, float[] pointBufferZ, float[] pointBufferM) {
        // TODO
        invalidate();
    }
}
