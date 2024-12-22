package com.macwap.rdxrasel.flowtextview.helpers;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

import com.macwap.rdxrasel.flowtextview.FlowTextView;
import com.macwap.rdxrasel.flowtextview.models.HtmlLink;
import com.macwap.rdxrasel.flowtextview.models.HtmlObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dean on 24/06/2014.
 */
@SuppressWarnings("SuspiciousMethodCalls")
public class SpanParser {

    public SpanParser(FlowTextView flowTextView, PaintHelper paintHelper) {
        this.mFlowTextView = flowTextView;
        this.mPaintHelper = paintHelper;
    }

    private final PaintHelper mPaintHelper;
    private final FlowTextView mFlowTextView;
    private final List<HtmlLink> mLinks = new ArrayList<>();
    private int mTextLength = 0;
    private Spannable mSpannable;

    private final HashMap<Integer, HtmlObject> sorterMap = new HashMap<>();

    public float parseSpans(List<HtmlObject> objects, Object[] spans, int lineStart, int lineEnd, float baseXOffset){

        sorterMap.clear();
        int charFlagSize = lineEnd - lineStart;
        boolean[] charFlags = new boolean[charFlagSize];

        String tempString;
        int spanStart;
        int spanEnd;
        int charCounter;

        for (Object span : spans) {

            spanStart = mSpannable.getSpanStart(span);
            spanEnd = mSpannable.getSpanEnd(span);

            if(spanStart <lineStart) spanStart = lineStart;
            if(spanEnd >lineEnd) spanEnd = lineEnd;

            for(charCounter = spanStart; charCounter < spanEnd; charCounter++){ // mark these characters as rendered
                int charFlagIndex = charCounter - lineStart;
                charFlags[charFlagIndex] = true;
            }

            tempString = extractText(spanStart, spanEnd);
            sorterMap.put(spanStart, parseSpan(span, tempString, spanStart, spanEnd));
        }

        charCounter = 0;

        while(!isArrayFull(charFlags)){
            while (charCounter < charFlagSize) {


                if (charFlags[charCounter]) {
                    charCounter++;
                    continue;
                }

                int temp1 = charCounter;
                while (true) {
                    if (charCounter > charFlagSize) break;

                    if (charCounter < charFlagSize) {
                        if (!charFlags[charCounter]) {
                            charFlags[charCounter] = true;// mark as filled
                            charCounter++;
                            continue;
                        }
                    }
                    int temp2 = charCounter;
                    spanStart = lineStart + temp1;
                    spanEnd = lineStart + temp2;
                    tempString = extractText(spanStart, spanEnd);
                    sorterMap.put(spanStart, parseSpan(null, tempString, spanStart, spanEnd));
                    break;

                }
            }
        }

        Object[] sorterKeys = sorterMap.keySet().toArray();
        Arrays.sort(sorterKeys);

        float thisXoffset = baseXOffset;

        for(charCounter =0; charCounter < sorterKeys.length; charCounter++){
            HtmlObject thisObj = sorterMap.get(sorterKeys[charCounter]);
            thisObj.xOffset = thisXoffset;
            thisXoffset += thisObj.paint.measureText(thisObj.content);
            objects.add(thisObj);
        }

        return (thisXoffset - baseXOffset);
    }

    private HtmlObject parseSpan(Object span, String content, int start, int end){

        if(span instanceof URLSpan){
            return getHtmlLink((URLSpan) span, content, start, end);
        }else if(span instanceof StyleSpan){
            return getStyledObject((StyleSpan) span, content, start, end);
        }else{
            return getHtmlObject(content, start, end);
        }
    }

    private HtmlObject getStyledObject(StyleSpan span, String content, int start, int end){
        TextPaint paint = mPaintHelper.getPaintFromHeap();
        paint.setTypeface(Typeface.defaultFromStyle(span.getStyle()));
        paint.setTextSize(mFlowTextView.getTextsize());
        paint.setColor(mFlowTextView.getColor());

        span.updateDrawState(paint);
        span.updateMeasureState(paint);
        HtmlObject  obj = new HtmlObject(content, start, end, (float) 0, paint);
        obj.recycle = true;
        return obj;
    }

    private HtmlObject getHtmlObject(String content, int start, int end){
        return new HtmlObject(content, start, end, (float) 0, mFlowTextView.getTextPaint());
    }

    public void reset(){
        mLinks.clear();
    }

    private HtmlLink getHtmlLink(URLSpan span, String content, int start, int end){
        HtmlLink  obj = new HtmlLink(content, start, end, (float) 0, mFlowTextView.getLinkPaint(), span.getURL());
        mLinks.add(obj);
        return obj;
    }

    public void addLink(HtmlLink thisLink, float yOffset, float width, float height){
        thisLink.yOffset = yOffset - 20;
        thisLink.width = width;
        thisLink.height = height + 20;
        mLinks.add(thisLink);
    }

    private String extractText(int start, int end){
        if(start<0) start = 0;
        if(end > mTextLength-1) end = mTextLength-1;
        return mSpannable.subSequence(start, end).toString();
    }

    private static boolean isArrayFull(boolean[] array){
        for (boolean b : array) {
            if (!b) return false;
        }
        return true;
    }

    // GETTERS AND SETTERS
    public List<HtmlLink> getLinks() {
        return mLinks;
    }

    public Spannable getSpannable() {
        return mSpannable;
    }

    public void setSpannable(Spannable mSpannable) {
        this.mSpannable = mSpannable;
        mTextLength = mSpannable.length();
    }
}
