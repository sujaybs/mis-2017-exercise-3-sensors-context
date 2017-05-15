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

public class SpectrumView extends View {

    private Paint whiteLine;
    private Paint blackBackground;

    private RectF backgroundRect;

    private Path pathM;

    private float coefficientWidth;

    private float maximum = -1f;

    private Paint whiteText;

    public SpectrumView(Context context) {
        super(context, null, 0);
        init();
    }

    public SpectrumView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public SpectrumView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        whiteLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteLine.setColor(Color.WHITE);
        whiteLine.setStrokeWidth(4.0f);
        whiteLine.setStyle(Paint.Style.STROKE);
        whiteLine.setStrokeJoin(Paint.Join.ROUND);

        whiteText = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteText.setColor(Color.WHITE);
        whiteText.setTextSize(36f);

        blackBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackBackground.setColor(Color.BLACK);
    }

    public void adjustMaximum(float maxM) {
        maximum = maxM;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        backgroundRect = new RectF(0, 0, w, h);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (backgroundRect == null)
            return;

        canvas.drawRect(backgroundRect, blackBackground);

        if (pathM != null)
            canvas.drawPath(pathM, whiteLine);
        if (maximum != -1f) {
            drawText(canvas,
                    String.format("Max: %f", maximum),
                    backgroundRect.width() * 0.95f,
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

    public void setBuffer(float[] pointBufferM) {
        if (backgroundRect == null)
            return;


        coefficientWidth = backgroundRect.height() / ActivityHome.getPointWindow() * 2;

        pathM = getPath(pointBufferM);

        invalidate();
    }

    private Path getPath(float[] pointBuffer) {
        Path _path = new Path();

        _path.moveTo(1f / 2f * backgroundRect.width(), -1f * coefficientWidth);

        float span = maximum != -1 ? maximum : 1f;

        for (int i = 0; i < pointBuffer.length / 2; i++) {
            float relValue = pointBuffer[i] / span;
            _path.lineTo((relValue * 0.8f + 0.1f) * backgroundRect.width(), i * coefficientWidth);
        }

        return _path;
    }
}
