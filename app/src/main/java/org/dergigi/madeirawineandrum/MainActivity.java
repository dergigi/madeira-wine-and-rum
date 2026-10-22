package org.dergigi.madeirawineandrum;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.widget.*;

/** The initial release intentionally stays on this quiet welcome screen. */
public final class MainActivity extends Activity {
    private static final int INK = Color.rgb(42, 70, 62);
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(245, 239, 227));
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(28), dp(28), dp(28), dp(28));
        scroll.addView(content, new ScrollView.LayoutParams(-1, -1));
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        TextView island = text(R.string.island, 13);
        island.setLetterSpacing(0.3f);
        content.addView(island);
        TextView title = text(R.string.title, 42);
        title.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        title.setPadding(0, dp(10), 0, dp(8));
        content.addView(title);
        TextView subtitle = text(R.string.subtitle, 15);
        content.addView(subtitle);
        View art = new IslandArt();
        art.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams artSize = new LinearLayout.LayoutParams(-1, dp(310));
        artSize.topMargin = dp(26); artSize.bottomMargin = dp(24);
        content.addView(art, artSize);
        TextView footer = text(R.string.footer, 11);
        footer.setLetterSpacing(0.15f);
        footer.setLineSpacing(dp(8), 1);
        content.addView(footer);
        setContentView(scroll);
    }

    private TextView text(int resource, int size) {
        TextView view = new TextView(this);
        view.setText(resource); view.setTextColor(INK); view.setTextSize(size);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    /** Original vector artwork: Atlantic hills, terraced vines, wine and rum. */
    private final class IslandArt extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        IslandArt() { super(MainActivity.this); }
        private void color(String hex) { p.setColor(Color.parseColor(hex)); p.setStyle(Paint.Style.FILL); }
        private void shape(Canvas c, String hex, float... points) {
            color(hex); Path path = new Path(); path.moveTo(points[0], points[1]);
            for (int i = 2; i < points.length; i += 2) path.lineTo(points[i], points[i + 1]);
            path.close(); c.drawPath(path, p);
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float scale = Math.min(getWidth() / 320f, getHeight() / 310f);
            c.save(); c.translate((getWidth() - 320 * scale) / 2, (getHeight() - 310 * scale) / 2);
            c.scale(scale, scale);
            color("#E6E3D2"); c.drawRoundRect(8, 0, 312, 300, 152, 152, p);
            c.save(); Path clip = new Path(); clip.addRoundRect(8, 0, 312, 300, 152, 152, Path.Direction.CW); c.clipPath(clip);
            color("#D7A95F"); c.drawCircle(225, 64, 27, p);
            color("#92B5B2"); c.drawRect(8, 147, 312, 300, p);
            shape(c, "#7C9680", 0,174, 79,75, 121,129, 165,96, 251,195);
            shape(c, "#345D54", 0,189, 66,142, 111,162, 170,120, 277,226, 0,264);
            shape(c, "#22483F", 0,215, 109,181, 202,220, 320,210, 320,310, 0,310);
            p.setColor(Color.parseColor("#819775")); p.setStrokeWidth(2); p.setStyle(Paint.Style.STROKE);
            for (int i=0;i<4;i++) { Path vine=new Path(); vine.moveTo(0,224+i*13); vine.quadTo(92,188+i*12,174,229+i*13); c.drawPath(vine,p); }
            c.restore();
            color("#D8CDB8"); c.drawOval(66,273,266,292,p);
            // A glass of Madeira wine.
            color("#F9F5EC"); c.drawRoundRect(88,184,139,241,13,13,p);
            color("#8A403F"); c.drawRoundRect(93,204,134,236,10,10,p);
            color("#F9F5EC"); c.drawRect(111,238,116,276,p); c.drawOval(94,273,133,280,p);
            // An amber rum bottle with a simple cream label.
            color("#A96B30"); c.drawRoundRect(184,168,215,203,5,5,p);
            c.drawRoundRect(168,195,231,279,12,12,p);
            color("#345D54"); c.drawRoundRect(182,161,217,178,3,3,p);
            color("#F5EFE3"); c.drawRect(174,220,225,257,p);
            color("#D7A95F"); c.drawCircle(199.5f,238.5f,10,p);
            c.restore();
        }
    }
}
