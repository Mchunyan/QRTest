package com.example.qrcode;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShareBitmapBean implements Serializable {

    private Bitmap bg;

    private List<QRCodeBean> qrCodeList;

    private List<TextBean> textList;

    private List<BitmapBean> bitmapList;

    public Bitmap getBg() {
        return bg;
    }

    public void setBg(Bitmap bg) {
        this.bg = bg;
    }

    public List<QRCodeBean> getQrCodeList() {
        return qrCodeList;
    }

    public void setQrCodeList(List<QRCodeBean> qrCodeList) {
        this.qrCodeList = qrCodeList;
    }

    public List<TextBean> getTextList() {
        return textList;
    }

    public void setTextList(List<TextBean> textList) {
        this.textList = textList;
    }

    public List<BitmapBean> getBitmapList() {
        return bitmapList;
    }

    public void setBitmapList(List<BitmapBean> bitmapList) {
        this.bitmapList = bitmapList;
    }

    public static class QRCodeBean implements Serializable{

        //二维码内的信息
        private String codeMsg;
        //二维码的宽度
        private int codeWidth;
        //二维码的高度
        private int codeHeight;
        //二维码的X坐标
        private int codeX;
        //二维码的Y坐标
        private int codeY;

        public QRCodeBean(String codeMsg, int codeWidth, int codeHeight, int codeX, int codeY) {
            this.codeMsg = codeMsg;
            this.codeWidth = codeWidth;
            this.codeHeight = codeHeight;
            this.codeX = codeX;
            this.codeY = codeY;
        }

        public String getCodeMsg() {
            return codeMsg;
        }

        public void setCodeMsg(String codeMsg) {
            this.codeMsg = codeMsg;
        }

        public int getCodeWidth() {
            return codeWidth;
        }

        public void setCodeWidth(int codeWidth) {
            this.codeWidth = codeWidth;
        }

        public int getCodeHeight() {
            return codeHeight;
        }

        public void setCodeHeight(int codeHeight) {
            this.codeHeight = codeHeight;
        }

        public int getCodeX() {
            return codeX;
        }

        public void setCodeX(int codeX) {
            this.codeX = codeX;
        }

        public int getCodeY() {
            return codeY;
        }

        public void setCodeY(int codeY) {
            this.codeY = codeY;
        }

        public List<QRCodeBean> toList() {
            List<QRCodeBean> list = new ArrayList<>();
            list.add(this);
            return list;
        }
    }


    public static class TextBean implements Serializable{
        //文字信息
        private String text;
        //字号大小，单位像素
        private int sizePX;
        //文字的X坐标
        private int textX;
        //文字的Y坐标
        private int textY;
        //文字颜色
        private int textColor;
        private boolean isCenter=false;

        public boolean isCenter() {
            return isCenter;
        }

        public void setCenter(boolean center) {
            isCenter = center;
        }

        public TextBean(String text, int sizePX, int textX, int textY, int textColor) {
            this.text = text;
            this.sizePX = sizePX;
            this.textX = textX;
            this.textY = textY;
            this.textColor = textColor;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getSizePX() {
            return sizePX;
        }

        public void setSizePX(int sizePX) {
            this.sizePX = sizePX;
        }

        public int getTextX() {
            return textX;
        }

        public void setTextX(int textX) {
            this.textX = textX;
        }

        public int getTextY() {
            return textY;
        }

        public void setTextY(int textY) {
            this.textY = textY;
        }

        public int getTextColor() {
            return textColor;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public List<TextBean> toList() {
            List<TextBean> list = new ArrayList<>();
            list.add(this);
            return list;
        }
    }


    public static class BitmapBean implements Serializable{
        //bitmap
        private Bitmap bitmap;

        private int bitmapWidth;

        private int bitmapHeight;

        private int bitmapX;

        private int bitmapY;

        public BitmapBean(Bitmap bitmap, int bitmapWidth, int bitmapHeight, int bitmapX, int bitmapY) {
            this.bitmap = bitmap;
            this.bitmapWidth = bitmapWidth;
            this.bitmapHeight = bitmapHeight;
            this.bitmapX = bitmapX;
            this.bitmapY = bitmapY;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public int getBitmapWidth() {
            return bitmapWidth;
        }

        public void setBitmapWidth(int bitmapWidth) {
            this.bitmapWidth = bitmapWidth;
        }

        public int getBitmapHeight() {
            return bitmapHeight;
        }

        public void setBitmapHeight(int bitmapHeight) {
            this.bitmapHeight = bitmapHeight;
        }

        public int getBitmapX() {
            return bitmapX;
        }

        public void setBitmapX(int bitmapX) {
            this.bitmapX = bitmapX;
        }

        public int getBitmapY() {
            return bitmapY;
        }

        public void setBitmapY(int bitmapY) {
            this.bitmapY = bitmapY;
        }

        public List<BitmapBean> toList() {
            List<BitmapBean> list = new ArrayList<>();
            list.add(this);
            return list;
        }
    }


}