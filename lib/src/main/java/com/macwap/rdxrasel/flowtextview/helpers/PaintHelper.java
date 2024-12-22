package com.macwap.rdxrasel.flowtextview.helpers;

import android.graphics.Paint;
import android.text.TextPaint;

import java.util.ArrayList;


public class PaintHelper {
    private final ArrayList<TextPaint> mPaintHeap = new ArrayList<>();

    public TextPaint getPaintFromHeap(){
        if(mPaintHeap.size()>0){
            return mPaintHeap.remove(0);
        }else{
            return new TextPaint(Paint.ANTI_ALIAS_FLAG);
        }
    }

    public void setColor(int color){
        for (TextPaint paint : mPaintHeap) {
            paint.setColor(color);
        }
    }

    public void recyclePaint(TextPaint paint){
        mPaintHeap.add(paint);
    }
}
