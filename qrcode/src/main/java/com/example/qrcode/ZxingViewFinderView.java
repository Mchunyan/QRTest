package com.example.qrcode;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.util.ArrayList;
import java.util.List;

public class ZxingViewFinderView extends ViewfinderView {
    private int scannerBoundColor;
    private float scannerBoundWidth; // viewfinder width
    private int scannerBoundCornerColor; // viewfinder corner color
    private float scannerBoundCornerWidth; // viewfinder corner width
    private float scannerBoundCornerHeight; // viewfinder corner height
    private int scannerLaserResId; // laser resource
    private String scannerTipText; // tip text
    private float scannerTipTextSize; // tip text size
    private int scannerTipTextColor; // tip text color
    private float scannerTipTextMargin; // tip text margin between viewfinder
    private boolean tipTextGravityBottom; // tip text gravity
    private Bitmap scannerLaserBitmap; // laser bitmap
    private int scannerLaserTop; // laser top position
    private final static int LASER_MOVE_DISTANCE_PER_UNIT_TIME = 10;
    private int LASER_MOVE_DIRECTION = 1;

    public ZxingViewFinderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Resources resources = getResources(); // Get attributes on view
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.ZxingViewFinderView);
        scannerBoundColor = attributes.getColor(R.styleable.ZxingViewFinderView_scannerBoundColor, resources.getColor(R.color.colorAccent));
        scannerBoundWidth = attributes.getDimension(R.styleable.ZxingViewFinderView_scannerBoundWidth, 0.5f);
        scannerBoundCornerColor = attributes.getColor(R.styleable.ZxingViewFinderView_scannerBoundCornerColor, resources.getColor(R.color.colorPrimaryDark));
        scannerBoundCornerWidth = attributes.getDimension(R.styleable.ZxingViewFinderView_scannerBoundCornerWith, 1.5f);
        scannerBoundCornerHeight = attributes.getDimension(R.styleable.ZxingViewFinderView_scannerBoundCornerHeight, 24f);
        scannerLaserResId = attributes.getResourceId(R.styleable.ZxingViewFinderView_scannerLaserResId, 0);
        scannerTipText = attributes.getString(R.styleable.ZxingViewFinderView_scannerTipText);
        scannerTipTextSize = attributes.getDimension(R.styleable.ZxingViewFinderView_scannerTipTextSize, 14f);
        scannerTipTextColor = attributes.getColor(R.styleable.ZxingViewFinderView_scannerTipTextColor, resources.getColor(R.color.colorAccent));
        scannerTipTextMargin = attributes.getDimension(R.styleable.ZxingViewFinderView_scannerTipTextMargin, 40f);
        tipTextGravityBottom = attributes.getBoolean(R.styleable.ZxingViewFinderView_scannerTipTextGravity, true);
        attributes.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        refreshSizes();
        if (framingRect == null || previewFramingRect == null) {
            return;
        }
        Rect frame = framingRect;
        Rect previewFrame = previewFramingRect;
        int width = canvas.getWidth();
        int height = canvas.getHeight(); // Draw the exterior
        drawExteriorDarkened(canvas, frame, width, height);
        if (resultBitmap != null) { // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            drawFrameBound(canvas, frame);
            drawFrameCorner(canvas, frame);
            drawLaserLine(canvas, frame);
            drawTipText(canvas, frame, width);
            drawResultPoint(canvas, frame, previewFrame); // Request another update at the animation interval,
            // but only repaint the laser line, // not the entire viewfinder mask.
            postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE, frame.top - POINT_SIZE, frame.right + POINT_SIZE, frame.bottom + POINT_SIZE);
        }
    }

    /**
     * Draw a "laser scanner" line to show decoding is active
     */
    private void drawLaserLine(Canvas canvas, Rect frame) {
        if (scannerLaserResId == 0) {
            paint.setColor(laserColor);
            paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
            int middle = frame.height() / 2 + frame.top;
            canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1, middle + 2, paint);
        } else {
            if (scannerLaserBitmap == null) {
                scannerLaserBitmap = BitmapFactory.decodeResource(getResources(), scannerLaserResId);
            }
            if (scannerLaserBitmap != null) {
                int LaserHeight = scannerLaserBitmap.getHeight();
                if (scannerLaserTop < frame.top) {
                    scannerLaserTop = frame.top;
                    LASER_MOVE_DIRECTION = 1;
                }
                if (scannerLaserTop > frame.bottom - LaserHeight) {
                    scannerLaserTop = frame.bottom - LaserHeight;
                    LASER_MOVE_DIRECTION = -1;
                }
                Rect laserBitmapRect = new Rect(frame.left, scannerLaserTop, frame.right, scannerLaserTop + LaserHeight);
                canvas.drawBitmap(scannerLaserBitmap, null, laserBitmapRect, paint);
                scannerLaserTop = scannerLaserTop + LASER_MOVE_DISTANCE_PER_UNIT_TIME * LASER_MOVE_DIRECTION;
            }
        }
    }

    /**
     * Draw result points
     */
    private void drawResultPoint(Canvas canvas, Rect frame, Rect previewFrame) {
        float scaleX = frame.width() / (float) previewFrame.width();
        float scaleY = frame.height() / (float) previewFrame.height();
        List<ResultPoint> currentPossible = possibleResultPoints;
        List<ResultPoint> currentLast = lastPossibleResultPoints;
        int frameLeft = frame.left;
        int frameTop = frame.top;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new ArrayList<>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(CURRENT_POINT_OPACITY);
            paint.setColor(resultPointColor);
            for (ResultPoint point : currentPossible) {
                canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX), frameTop + (int) (point.getY() * scaleY), POINT_SIZE, paint);
            }
        }
        if (currentLast != null) {
            paint.setAlpha(CURRENT_POINT_OPACITY / 2);
            paint.setColor(resultPointColor);
            float radius = POINT_SIZE / 2.0f;
            for (ResultPoint point : currentLast) {
                canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX), frameTop + (int) (point.getY() * scaleY), radius, paint);
            }
        }
    }

    /**
     * Draw tip text
     */
    private void drawTipText(Canvas canvas, Rect frame, int width) {
        if (TextUtils.isEmpty(scannerTipText)) {
            scannerTipText = "提示";
        }
        paint.setColor(scannerTipTextColor);
        paint.setTextSize(scannerTipTextSize);
        float textWidth = paint.measureText(scannerTipText);
        float x = (width - textWidth) / 2; //根据 drawTextGravityBottom 文字在扫描框上方还是下文，默认下方
        float y = tipTextGravityBottom ? frame.bottom + scannerTipTextMargin : frame.top - scannerTipTextMargin;
        canvas.drawText(scannerTipText, x, y, paint);
    }

    /**
     * Draw scanner frame bound * Note: draw inside frame
     */
    private void drawFrameBound(Canvas canvas, Rect frame) {
        if (scannerBoundWidth <= 0) {
            return;
        }
        paint.setColor(scannerBoundColor); // top
        canvas.drawRect(frame.left, frame.top, frame.right, frame.top + scannerBoundWidth, paint); // left
        canvas.drawRect(frame.left, frame.top, frame.left + scannerBoundWidth, frame.bottom, paint); // right
        canvas.drawRect(frame.right - scannerBoundWidth, frame.top, frame.right, frame.bottom, paint); // bottom
        canvas.drawRect(frame.left, frame.bottom - scannerBoundWidth, frame.right, frame.bottom, paint);
    }

    /**
     * Draw scanner frame corner
     */
    private void drawFrameCorner(Canvas canvas, Rect frame) {
        if (scannerBoundCornerWidth <= 0 || scannerBoundCornerHeight <= 0) {
            return;
        }
        paint.setColor(scannerBoundCornerColor); // left top
        canvas.drawRect(frame.left - scannerBoundCornerWidth, frame.top - scannerBoundCornerWidth, frame.left + scannerBoundCornerHeight, frame.top, paint);
        canvas.drawRect(frame.left - scannerBoundCornerWidth, frame.top - scannerBoundCornerWidth, frame.left, frame.top + scannerBoundCornerHeight, paint); // left bottom
        canvas.drawRect(frame.left - scannerBoundCornerWidth, frame.bottom + scannerBoundCornerWidth - scannerBoundCornerHeight, frame.left, frame.bottom + scannerBoundCornerWidth, paint);
        canvas.drawRect(frame.left - scannerBoundCornerWidth, frame.bottom, frame.left - scannerBoundCornerWidth + scannerBoundCornerHeight, frame.bottom + scannerBoundCornerWidth, paint); // right top
        canvas.drawRect(frame.right + scannerBoundCornerWidth - scannerBoundCornerHeight, frame.top - scannerBoundCornerWidth, frame.right + scannerBoundCornerWidth, frame.top, paint);
        canvas.drawRect(frame.right, frame.top - scannerBoundCornerWidth, frame.right + scannerBoundCornerWidth, frame.top - scannerBoundCornerWidth + scannerBoundCornerHeight, paint); // right bottom
        canvas.drawRect(frame.right + scannerBoundCornerWidth - scannerBoundCornerHeight, frame.bottom, frame.right + scannerBoundCornerWidth, frame.bottom + scannerBoundCornerWidth, paint);
        canvas.drawRect(frame.right, frame.bottom + scannerBoundCornerWidth - scannerBoundCornerHeight, frame.right + scannerBoundCornerWidth, frame.bottom + scannerBoundCornerWidth, paint);
    }

    /**
     * Draw the exterior (i.e. outside the framing rect) darkened
     */
    private void drawExteriorDarkened(Canvas canvas, Rect frame, int width, int height) {
        paint.setColor(resultBitmap != null ? resultColor : maskColor); //top
        canvas.drawRect(0, 0, width, frame.top, paint); //left
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint); //right
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint); //bottom
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
    }
}