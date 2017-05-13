package com.example.sujaybshalawadi.mis3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import static com.example.sujaybshalawadi.mis3.ActivityHome.POINT_WINDOW;

public class GraphView extends View {

    private Paint redLine;
    private Paint greenLine;
    private Paint blueLine;
    private Paint whiteLine;
    private Paint blackBackground;

    private RectF backgroundRect;

    private Path pathX;
    private Path pathY;
    private Path pathZ;
    private Path pathM;

    private float coefficientWidth;

    private float[] maxima;
    private float[] minima;

    private Paint redText;
    private Paint greenText;
    private Paint blueText;
    private Paint whiteText;

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
        redLine.setStyle(Paint.Style.STROKE);
        redLine.setStrokeJoin(Paint.Join.ROUND);

        blueLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        blueLine.setColor(Color.BLUE);
        blueLine.setStrokeWidth(4.0f);
        blueLine.setStyle(Paint.Style.STROKE);
        blueLine.setStrokeJoin(Paint.Join.ROUND);

        greenLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenLine.setColor(Color.GREEN);
        greenLine.setStrokeWidth(4.0f);
        greenLine.setStyle(Paint.Style.STROKE);
        greenLine.setStrokeJoin(Paint.Join.ROUND);

        whiteLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteLine.setColor(Color.WHITE);
        whiteLine.setStrokeWidth(4.0f);
        whiteLine.setStyle(Paint.Style.STROKE);
        whiteLine.setStrokeJoin(Paint.Join.ROUND);

        whiteText = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteText.setColor(Color.WHITE);
        whiteText.setTextSize(36f);

        redText = new Paint(Paint.ANTI_ALIAS_FLAG);
        redText.setColor(Color.RED);
        redText.setTextSize(36f);

        greenText = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenText.setColor(Color.GREEN);
        greenText.setTextSize(36f);

        blueText = new Paint(Paint.ANTI_ALIAS_FLAG);
        blueText.setColor(Color.BLUE);
        blueText.setTextSize(36f);

        blackBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackBackground.setColor(Color.BLACK);
    }

    public void adjustMaxima(float maxX, float maxY, float maxZ, float maxM) {
        maxima = new float[]{
                maxX > 0 ? maxX : 0,
                maxY > 0 ? maxY : 0,
                maxZ > 0 ? maxZ : 0,
                maxM > 0 ? maxM : 0};
    }

    public void adjustMinima(float minX, float minY, float minZ, float minM) {
        minima = new float[]{minX, minY, minZ, minM};
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        backgroundRect = new RectF(0, 0, w, h);
        coefficientWidth = backgroundRect.height() / POINT_WINDOW;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (backgroundRect == null)
            return;

        canvas.drawRect(backgroundRect, blackBackground);

        if (pathX != null)
            canvas.drawPath(pathX, redLine);
        if (pathY != null)
            canvas.drawPath(pathY, greenLine);
        if (pathZ != null)
            canvas.drawPath(pathZ, blueLine);
        if (pathM != null)
            canvas.drawPath(pathM, whiteLine);
        if (maxima != null) {
            drawText(canvas,
                    String.format("Max: %f", maxima[0]),
                    backgroundRect.width() * 0.95f,
                    backgroundRect.height() * 0.2f,
                    redText);
            drawText(canvas,
                    String.format("Max: %f", maxima[1]),
                    backgroundRect.width() * 0.95f,
                    backgroundRect.height() * 0.4f,
                    greenText);
            drawText(canvas,
                    String.format("Max: %f", maxima[2]),
                    backgroundRect.width() * 0.95f,
                    backgroundRect.height() * 0.6f,
                    blueText);
            drawText(canvas,
                    String.format("Max: %f", maxima[3]),
                    backgroundRect.width() * 0.95f,
                    backgroundRect.height() * 0.8f,
                    whiteText);
        }
        if (minima != null) {
            drawText(canvas,
                    String.format("Min: %f", minima[0]),
                    backgroundRect.width() * 0.05f,
                    backgroundRect.height() * 0.2f,
                    redText);
            drawText(canvas,
                    String.format("Min: %f", minima[1]),
                    backgroundRect.width() * 0.05f,
                    backgroundRect.height() * 0.4f,
                    greenText);
            drawText(canvas,
                    String.format("Min: %f", minima[2]),
                    backgroundRect.width() * 0.05f,
                    backgroundRect.height() * 0.6f,
                    blueText);
            drawText(canvas,
                    String.format("Min: %f", minima[3]),
                    backgroundRect.width() * 0.05f,
                    backgroundRect.height() * 0.8f,
                    whiteText);
        }
    }

    private void drawText(Canvas canvas, String text, float x, float y, Paint paint) {
        canvas.save();
        canvas.rotate(90, x, y);
        canvas.drawText(text, x, y, paint);
        canvas.restore();
    }

    public void setBuffers(float[] pointBufferX, float[] pointBufferY, float[] pointBufferZ, float[] pointBufferM) {
        if (backgroundRect == null)
            return;

        pathX = getPath(pointBufferX, 0);
        pathY = getPath(pointBufferY, 1);
        pathZ = getPath(pointBufferZ, 2);
        pathM = getPath(pointBufferM, 3);

        invalidate();
    }

    private Path getPath(float[] pointBuffer, int indice) {
        Path _path = new Path();

        _path.moveTo(1f / 2f * backgroundRect.width(), -1f * coefficientWidth);

        float span = 1.0f;

        if (maxima[indice] > 0 && minima[indice] < 0) {
            span = maxima[indice] - minima[indice];
        } else if (minima[indice] > 0) {
            span = maxima[indice];
        } else if (maxima[indice] < 0) {
            span = -minima[indice];
        }

        for (int i = 0; i < pointBuffer.length; i++) {
            float relValue = pointBuffer[i] * 0.5f / span + 0.5f;
            _path.lineTo((relValue * 0.8f + 0.1f) * backgroundRect.width(), i * coefficientWidth);
        }

        return _path;
    }
}
