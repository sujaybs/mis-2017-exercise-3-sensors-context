package com.example.sujaybshalawadi.mis3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayDeque;

public class GraphView extends View {

    private static final int POINT_WINDOW = 1024;

    private Paint redLine;
    private Paint greenLine;
    private Paint blueLine;
    private Paint whiteLine;
    private Paint blackBackground;

    private RectF backgroundRect;

    private PointVec pointVecs;

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

        pointVecs = new PointVec();
    }

    public void pushValues(float x, float y, float z, float m) {
        pointVecs.x.push(x);
        pointVecs.y.push(y);
        pointVecs.z.push(z);
        pointVecs.m.push(m);
        if (pointVecs.x.size() > POINT_WINDOW) {
            pointVecs.x.removeLast();
            pointVecs.y.removeLast();
            pointVecs.z.removeLast();
            pointVecs.m.removeLast();
        }
        adjustValues();
        invalidate();
    }

    private void adjustValues() {
        pointBufferX = ArrayUtils.toPrimitive(pointVecs.x.toArray(new Float[POINT_WINDOW]), 0.0F);
        pointBufferY = ArrayUtils.toPrimitive(pointVecs.y.toArray(new Float[POINT_WINDOW]), 0.0F);
        pointBufferZ = ArrayUtils.toPrimitive(pointVecs.z.toArray(new Float[POINT_WINDOW]), 0.0F);
        pointBufferM = ArrayUtils.toPrimitive(pointVecs.m.toArray(new Float[POINT_WINDOW]), 0.0F);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        backgroundRect = new RectF(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(backgroundRect, blackBackground);

        canvas.drawLines(pointBufferX, redLine);
        canvas.drawLines(pointBufferY, blueLine);
        canvas.drawLines(pointBufferZ, greenLine);
        canvas.drawLines(pointBufferM, whiteLine);

    }

    private static final class PointVec {
        ArrayDeque<Float> x;
        ArrayDeque<Float> y;
        ArrayDeque<Float> z;
        ArrayDeque<Float> m;

        PointVec() {
            x = new ArrayDeque<>();
            y = new ArrayDeque<>();
            z = new ArrayDeque<>();
            m = new ArrayDeque<>();
        }
    }
}
