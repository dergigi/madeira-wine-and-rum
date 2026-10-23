package org.dergigi.madeirawineandrum;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import org.json.*;
import java.util.*;
import java.util.function.Consumer;

/** Offline geographic overview. External navigation uses venue names, never these approximate pins. */
final class IslandMapView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<double[]> coast = new ArrayList<>();
    private List<JSONObject> places = new ArrayList<>();
    private final List<JSONObject> all;
    private final Consumer<List<JSONObject>> select;
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestures;
    private float zoom = 1, panX, panY;
    private boolean scaled;
    private float downX, downY, startX, startY;
    private float density;

    IslandMapView(Context context, List<JSONObject> all, Consumer<List<JSONObject>> select) {
        super(context); this.all = all; this.select = select;
        density = getResources().getDisplayMetrics().density;
        try {
            JSONArray polygons = new JSONArray(Guide.read(context, "coastline.json"));
            JSONArray ring = polygons.getJSONArray(0).getJSONArray(0);
            for (int i=0; i<ring.length(); i++) {
                JSONArray point=ring.getJSONArray(i); coast.add(new double[]{point.getDouble(0),point.getDouble(1)});
            }
        } catch (Exception e) { throw new IllegalStateException("Missing bundled coastline", e); }
        // The equivalent numbered place list supplies accessible controls for every pin.
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector detector) {
                scaled = true;
                scaleAt(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                return true;
            }
        });
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(android.view.MotionEvent event) { return true; }
            @Override public boolean onDoubleTap(android.view.MotionEvent event) {
                scaled = true; scaleAt(1.8f,event.getX(),event.getY()); return true;
            }
        });
    }
    void setPlaces(List<JSONObject> value) { places = value; invalidate(); }
    void reset() { zoom=1; panX=panY=0; invalidate(); }
    void zoom(float factor) { scaleAt(factor,getWidth()/2f,getHeight()/2f); }
    private void scaleAt(float factor,float x,float y) {
        float next = Math.max(1,Math.min(16,zoom*factor));
        float ratio=next/zoom;
        panX=x-getWidth()/2f-(x-getWidth()/2f-panX)*ratio;
        panY=y-getHeight()/2f-(y-getHeight()/2f-panY)*ratio;
        zoom=next; constrain(); invalidate();
    }
    private void constrain() {
        float limitX=getWidth()*(zoom-1)/2f,limitY=getHeight()*(zoom-1)/2f;
        panX=Math.max(-limitX,Math.min(limitX,panX));
        panY=Math.max(-limitY,Math.min(limitY,panY));
    }
    private PointF point(double lon,double lat) {
        // Equirectangular projection with longitude corrected for Madeira's latitude.
        float unit=Math.min((getWidth()-48*density)/(.68f*.842f),(getHeight()-64*density)/.29f);
        return new PointF(getWidth()/2f+panX+(float)(lon+16.985)*.842f*unit*zoom,
                getHeight()/2f+panY-(float)(lat-32.755)*unit*zoom);
    }
    static int labelColor(Context context,String type) {
        return context.getColor(type.equals("Wine")?R.color.wine_label:type.equals("Rum")?R.color.rum_label:R.color.local_label);
    }
    private int colorResource(int resource) { return getContext().getColor(resource); }
    static int color(String type) {
        return Color.parseColor(type.equals("Wine")?"#843F4A":type.equals("Rum")?"#986014":"#315F53");
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); canvas.drawColor(colorResource(R.color.map_water));
        Path land=new Path();
        for (int i=0;i<coast.size();i++) {
            PointF p=point(coast.get(i)[0],coast.get(i)[1]);
            if (i==0) land.moveTo(p.x,p.y); else land.lineTo(p.x,p.y);
        }
        land.close(); paint.setStyle(Paint.Style.FILL); paint.setColor(colorResource(R.color.map_land)); canvas.drawPath(land,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(density);paint.setColor(colorResource(R.color.map_coast));canvas.drawPath(land,paint);
        paint.setStyle(Paint.Style.FILL);paint.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
        paint.setTextSize(11*density);paint.setTextAlign(Paint.Align.LEFT);paint.setColor(colorResource(R.color.map_label));
        canvas.drawText("N ↑",16*density,24*density,paint);
        paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(12*density);
        if (zoom<2) canvas.drawText("M A D E I R A",getWidth()/2f,getHeight()/2f,paint);
        for (JSONObject place:places) {
            PointF p=point(place.optDouble("lon"),place.optDouble("lat"));
            paint.setColor(Color.WHITE);canvas.drawCircle(p.x,p.y,14*density,paint);
            paint.setColor(color(place.optString("type")));canvas.drawCircle(p.x,p.y,12*density,paint);
            paint.setColor(Color.WHITE);paint.setTextSize(11*density);
            canvas.drawText(String.valueOf(all.indexOf(place)+1),p.x,p.y+4*density,paint);
        }
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked()==MotionEvent.ACTION_DOWN) {
            scaled=false;downX=startX=event.getX();downY=startY=event.getY();
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        scaleDetector.onTouchEvent(event);gestures.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress() && event.getPointerCount()==1) {
                    float dx=event.getX()-downX,dy=event.getY()-downY;
                    if (Math.hypot(event.getX()-startX,event.getY()-startY)>8*density) scaled=true;
                    panX+=dx;panY+=dy;constrain();invalidate();
                }
                downX=event.getX();downY=event.getY();break;
            case MotionEvent.ACTION_POINTER_UP:
                int remaining=event.getActionIndex()==0?1:0;
                downX=event.getX(remaining);downY=event.getY(remaining);break;
            case MotionEvent.ACTION_UP:
                if (!scaled) {
                    performClick(); List<JSONObject> hits=new ArrayList<>();
                    for (JSONObject p:places) {
                        PointF at=point(p.optDouble("lon"),p.optDouble("lat"));
                        if (Math.hypot(at.x-event.getX(),at.y-event.getY())<24*density) hits.add(p);
                    }
                    if (!hits.isEmpty()) select.accept(hits);
                }
                getParent().requestDisallowInterceptTouchEvent(false);break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);break;
        }
        return true;
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
