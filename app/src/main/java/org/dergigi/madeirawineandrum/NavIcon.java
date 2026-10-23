package org.dergigi.madeirawineandrum;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Four quiet, consistent line icons for the native navigation bar. */
final class NavIcon extends View {
    private final int index,color;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    NavIcon(Context c,int index,int color) { super(c);this.index=index;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
    @Override protected void onDraw(Canvas c) {
        c.save();c.scale(getWidth()/24f,getHeight()/24f);
        p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);
        Path path=new Path();
        switch(index) {
            case 0:
                c.drawCircle(12,12,9,p);path.moveTo(16,7);path.lineTo(14,14);path.lineTo(7,17);path.lineTo(10,10);path.close();c.drawPath(path,p);break;
            case 1:
                path.moveTo(3,5);path.lineTo(9,3);path.lineTo(15,6);path.lineTo(21,4);path.lineTo(21,19);path.lineTo(15,21);path.lineTo(9,18);path.lineTo(3,20);path.close();c.drawPath(path,p);c.drawLine(9,3,9,18,p);c.drawLine(15,6,15,21,p);break;
            case 2:
                c.drawRoundRect(3,5,21,21,3,3,p);c.drawLine(3,10,21,10,p);c.drawLine(8,3,8,7,p);c.drawLine(16,3,16,7,p);c.drawLine(8,15,10,17,p);c.drawLine(10,17,16,13,p);break;
            default:
                path.moveTo(7,3);path.lineTo(17,3);path.lineTo(18,10);path.cubicTo(18,17,6,17,6,10);path.close();c.drawPath(path,p);c.drawLine(12,15,12,21,p);c.drawLine(8,21,16,21,p);c.drawLine(7,9,17,9,p);
        }
        c.restore();
    }
}
