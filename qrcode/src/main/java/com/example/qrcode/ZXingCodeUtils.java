package com.example.qrcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class ZXingCodeUtils {
    private volatile static ZXingCodeUtils instance;

    public static ZXingCodeUtils getInstance() {
        if (instance == null) {
            synchronized (ZXingCodeUtils.class) {
                instance = new ZXingCodeUtils();
            }
        }
        return instance;
    }

    //不带logo
    public Bitmap createQRCode(String msg, int width, int height) {
        return createQRCode(msg, width, height, null);
    }

    //带logo
    public Bitmap createQRCode(String msg, int width, int height, Bitmap logo) {
        if (!TextUtils.isEmpty(msg)) {
            Log.e("default", "url is ------" + msg);
        }
        try {
            int[] tempSize = new int[]{width, height};//要求生成的二维码的宽高
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = new QRCodeWriter().encode(msg, BarcodeFormat.QR_CODE, width, height, hints);
            //调用去除白边方法
            matrix = deleteWhite(matrix);
            width = matrix.getWidth();
            height = matrix.getHeight();

            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK;
                    } else {
                        pixels[y * width + x] = Color.WHITE;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            bitmap = addLogoToQRCode(bitmap, logo);
            bitmap = zoomImg(bitmap, tempSize[0], tempSize[1]);//处理去掉白边后，二维码的尺寸变化问题。
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }


    //带文字无logo
    public Bitmap createQRCodeWithText(ShareBitmapBean dataBean) {
        return createQRCodeWithText(dataBean, null);
    }

    //带文字有logo


    public Bitmap createQRCodeWithText(ShareBitmapBean dataBean, Bitmap logo) {

        if (dataBean == null) {
            throw new IllegalArgumentException("shareBitmap data can't be null");
        }

        Bitmap bg = dataBean.getBg();
        if (bg == null) {
            throw new IllegalArgumentException("background bitmap can't be null");
        }
        bg = bg.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bg);
        Paint paint = new Paint();

        //绘制二维码
        if (dataBean.getQrCodeList() != null) {
            for (ShareBitmapBean.QRCodeBean qrCodeBean : dataBean.getQrCodeList()) {
                Bitmap qrCode = createQRCode(qrCodeBean.getCodeMsg(), qrCodeBean.getCodeWidth(), qrCodeBean.getCodeHeight(), logo);
                canvas.drawBitmap(qrCode, qrCodeBean.getCodeX(), qrCodeBean.getCodeY(), paint);
            }
        }

        //绘制文字
        if (dataBean.getTextList() != null) {
            for (ShareBitmapBean.TextBean textBean : dataBean.getTextList()) {
                int textTop = textBean.getSizePX() * 7 / 8 + textBean.getTextY();
                paint.setColor(textBean.getTextColor());
                paint.setTextSize(textBean.getSizePX());
                if (textBean.isCenter()) {
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(textBean.getText(), 360, textTop, paint);
                } else {
                    paint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(textBean.getText(), textBean.getTextX(), textTop, paint);
                }

            }
        }

        //绘制bitmap图像
        if (dataBean.getBitmapList() != null) {
            for (ShareBitmapBean.BitmapBean bitmapBean : dataBean.getBitmapList()) {
                canvas.drawBitmap(bitmapBean.getBitmap(), bitmapBean.getBitmapX(), bitmapBean.getBitmapY(), paint);
            }
        }
        return bg;
    }


    //添加logo到二维码图片上
    private Bitmap addLogoToQRCode(Bitmap src, Bitmap logo) {
        if (src == null || logo == null) {
            return src;
        }
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
        }
        return bitmap;
    }

    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }

    /**
     * 按照指定的尺寸缩放Bitmap
     *
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
    public Bitmap zoomImg(Bitmap bm, float newWidth, float newHeight) {
        if (bm == null) {
            return null;
        }
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片   www.2cto.com
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }
}