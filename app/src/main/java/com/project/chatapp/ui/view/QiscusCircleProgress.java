/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.project.chatapp.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.project.chatapp.R;
import com.project.chatapp.util.QiscusConverterUtil;

public class QiscusCircleProgress extends View implements QiscusProgressView {
    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_TEXT_COLOR = "text_color";
    private static final String INSTANCE_TEXT_SIZE = "text_size";
    private static final String INSTANCE_FINISHED_STROKE_COLOR = "finished_stroke_color";
    private static final String INSTANCE_UNFINISHED_STROKE_COLOR = "unfinished_stroke_color";
    private static final String INSTANCE_MAX = "max";
    private static final String INSTANCE_PROGRESS = "progress";
    private static final String INSTANCE_SUFFIX = "suffix";
    private static final String INSTANCE_PREFIX = "prefix";
    private final int defaultFinishedColor = Color.rgb(66, 145, 241);
    private final int defaultUnfinishedColor = Color.rgb(204, 204, 204);
    private final int defaultTextColor = Color.WHITE;
    private final int defaultMax = 100;
    private final float defaultTextSize;
    private final int minSize;
    private Paint textPaint;
    private RectF rectF = new RectF();
    private float textSize;
    private int textColor;
    private int progress = 0;
    private int max;
    private int finishedColor;
    private int unfinishedColor;
    private String prefixText = "";
    private String suffixText = "%";
    private Paint paint = new Paint();

    public QiscusCircleProgress(Context context) {
        this(context, null);
    }

    public QiscusCircleProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QiscusCircleProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        defaultTextSize = QiscusConverterUtil.sp2px(getResources(), 18);
        minSize = (int) QiscusConverterUtil.dp2px(getResources(), 100);

        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.QiscusCircleProgress, defStyleAttr, 0);
        initByAttributes(attributes);
        attributes.recycle();

        initPainters();
    }

    protected void initByAttributes(TypedArray attributes) {
        finishedColor = attributes.getColor(R.styleable.QiscusCircleProgress_qcircle_finished_color, defaultFinishedColor);
        unfinishedColor = attributes.getColor(R.styleable.QiscusCircleProgress_qcircle_unfinished_color, defaultUnfinishedColor);
        textColor = attributes.getColor(R.styleable.QiscusCircleProgress_qcircle_text_color, defaultTextColor);
        textSize = attributes.getDimension(R.styleable.QiscusCircleProgress_qcircle_text_size, defaultTextSize);

        setMax(attributes.getInt(R.styleable.QiscusCircleProgress_qcircle_max, defaultMax));
        setProgress(attributes.getInt(R.styleable.QiscusCircleProgress_qcircle_progress, 0));

        if (attributes.getString(R.styleable.QiscusCircleProgress_qcircle_prefix_text) != null) {
            setPrefixText(attributes.getString(R.styleable.QiscusCircleProgress_qcircle_prefix_text));
        }
        if (attributes.getString(R.styleable.QiscusCircleProgress_qcircle_suffix_text) != null) {
            setSuffixText(attributes.getString(R.styleable.QiscusCircleProgress_qcircle_suffix_text));
        }
    }

    protected void initPainters() {
        textPaint = new TextPaint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);

        paint.setAntiAlias(true);
    }

    @Override
    public void invalidate() {
        initPainters();
        super.invalidate();
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public void setProgress(int progress) {
        this.progress = progress;
        if (this.progress > getMax()) {
            this.progress %= getMax();
        }
        invalidate();
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        if (max > 0) {
            this.max = max;
            invalidate();
        }
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        this.invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        this.invalidate();
    }

    @Override
    public int getFinishedColor() {
        return finishedColor;
    }

    @Override
    public void setFinishedColor(int finishedColor) {
        this.finishedColor = finishedColor;
        this.invalidate();
    }

    @Override
    public int getUnfinishedColor() {
        return unfinishedColor;
    }

    @Override
    public void setUnfinishedColor(int unfinishedColor) {
        this.unfinishedColor = unfinishedColor;
        this.invalidate();
    }

    public String getPrefixText() {
        return prefixText;
    }

    public void setPrefixText(String prefixText) {
        this.prefixText = prefixText;
        this.invalidate();
    }

    public String getSuffixText() {
        return suffixText;
    }

    public void setSuffixText(String suffixText) {
        this.suffixText = suffixText;
        this.invalidate();
    }

    public String getDrawText() {
        return getPrefixText() + getProgress() + getSuffixText();
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return minSize;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return minSize;
    }

    public float getProgressPercentage() {
        return getProgress() / (float) getMax();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        rectF.set(0, 0, MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float yHeight = getProgress() / (float) getMax() * getHeight();
        float radius = getWidth() / 2f;
        float angle = (float) (Math.acos((radius - yHeight) / radius) * 180 / Math.PI);
        float startAngle = 90 + angle;
        float sweepAngle = 360 - angle * 2;
        paint.setColor(getUnfinishedColor());
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint);

        canvas.save();
        canvas.rotate(180, getWidth() / 2, getHeight() / 2);
        paint.setColor(getFinishedColor());
        canvas.drawArc(rectF, 270 - angle, angle * 2, false, paint);
        canvas.restore();

        // Also works.
//        paint.setColor(getFinishedColor());
//        canvas.drawArc(rectF, 90 - angle, angle * 2, false, paint);

        String text = getDrawText();
        if (!TextUtils.isEmpty(text)) {
            float textHeight = textPaint.descent() + textPaint.ascent();
            canvas.drawText(text, (getWidth() - textPaint.measureText(text)) / 2.0f, (getWidth() - textHeight) / 2.0f, textPaint);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
        bundle.putFloat(INSTANCE_TEXT_SIZE, getTextSize());
        bundle.putInt(INSTANCE_FINISHED_STROKE_COLOR, getFinishedColor());
        bundle.putInt(INSTANCE_UNFINISHED_STROKE_COLOR, getUnfinishedColor());
        bundle.putInt(INSTANCE_MAX, getMax());
        bundle.putInt(INSTANCE_PROGRESS, getProgress());
        bundle.putString(INSTANCE_SUFFIX, getSuffixText());
        bundle.putString(INSTANCE_PREFIX, getPrefixText());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            textColor = bundle.getInt(INSTANCE_TEXT_COLOR);
            textSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
            finishedColor = bundle.getInt(INSTANCE_FINISHED_STROKE_COLOR);
            unfinishedColor = bundle.getInt(INSTANCE_UNFINISHED_STROKE_COLOR);
            initPainters();
            setMax(bundle.getInt(INSTANCE_MAX));
            setProgress(bundle.getInt(INSTANCE_PROGRESS));
            prefixText = bundle.getString(INSTANCE_PREFIX);
            suffixText = bundle.getString(INSTANCE_SUFFIX);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }
}
