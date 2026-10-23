package org.dergigi.madeirawineandrum;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

/** Native, offline island guide; the browser or Maps handles external destinations. */
public final class MainActivity extends Activity {
    private static final int INK=Color.rgb(36,65,55), MUTED=Color.rgb(91,102,92), CREAM=Color.rgb(245,239,227);
    private Guide guide;
    private LinearLayout root, body, nav, placeList;
    private ScrollView scroll;
    private IslandMapView map;
    private String tab="Discover", category="All", query="", taste="All";
    private boolean savedOnly;
    private final Set<String> saved=new HashSet<>();
    private final Map<String,Integer> positions=new HashMap<>();
    private int dp(float n) { return Math.round(n*getResources().getDisplayMetrics().density); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        try { guide=new Guide(this); }
        catch (Exception e) { throw new IllegalStateException("Invalid bundled guide",e); }
        saved.addAll(getPreferences(0).getStringSet("saved",Collections.emptySet()));
        if (state!=null) {
            tab=state.getString("tab","Discover");category=state.getString("category","All");
            query=state.getString("query","");taste=state.getString("taste","All");savedOnly=state.getBoolean("savedOnly");
            for (String name:new String[]{"Discover","Map","One week","Bottles"}) positions.put(name,state.getInt("scroll_"+name));
        }
        root=column();root.setBackgroundColor(CREAM);
        root.setOnApplyWindowInsetsListener((v,insets)->{
            if (android.os.Build.VERSION.SDK_INT>=30) {
                Insets bars=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
                v.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            } else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        TextView brand=text("MADEIRA WINE & RUM",12,INK);brand.setLetterSpacing(.15f);
        brand.setPadding(dp(20),dp(18),dp(20),dp(12));root.addView(brand);
        scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setId(View.generateViewId());
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this);nav.setPadding(dp(8),dp(6),dp(8),dp(8));root.addView(nav);
        setContentView(root);render();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        positions.put(tab,scroll.getScrollY());
        out.putString("tab",tab);out.putString("category",category);out.putString("query",query);out.putString("taste",taste);out.putBoolean("savedOnly",savedOnly);
        for (String name:positions.keySet()) out.putInt("scroll_"+name,positions.get(name));
        super.onSaveInstanceState(out);
    }
    private LinearLayout column() { LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);return c; }
    private TextView text(String s,int size,int color) {
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setLineSpacing(dp(3),1);return t;
    }
    private GradientDrawable background(int color,int border) {
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(18));
        if (border!=0) d.setStroke(dp(1),border);return d;
    }
    private void title(LinearLayout parent,String s,int size) {
        TextView t=text(s,size,INK);t.setTypeface(Typeface.create("serif",Typeface.NORMAL));
        t.setPadding(0,dp(4),0,dp(10));parent.addView(t);
        if (android.os.Build.VERSION.SDK_INT>=28) t.setAccessibilityHeading(true);
    }
    private void para(LinearLayout parent,String s) {
        TextView t=text(s,16,INK);t.setPadding(0,0,0,dp(12));parent.addView(t);
    }
    private void caption(LinearLayout parent,String s) {
        TextView t=text(s,12,MUTED);t.setPadding(0,dp(4),0,dp(10));parent.addView(t);
    }
    private void eyebrow(LinearLayout parent,String s,int color) {
        TextView t=text(s,11,color);t.setTypeface(null,Typeface.BOLD);t.setLetterSpacing(.10f);t.setPadding(0,0,0,dp(6));parent.addView(t);
    }
    private LinearLayout card(LinearLayout parent) {
        LinearLayout c=column();c.setPadding(dp(18),dp(18),dp(18),dp(10));
        c.setBackground(background(Color.rgb(255,252,245),Color.rgb(226,219,204)));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(14);parent.addView(c,lp);return c;
    }
    private Button button(String label,Runnable action) {
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setTextColor(INK);
        b.setMinHeight(dp(48));b.setMinimumHeight(dp(48));b.setPadding(dp(12),dp(8),dp(12),dp(8));
        b.setBackground(background(Color.rgb(231,235,222),0));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(4);lp.bottomMargin=dp(6);b.setLayoutParams(lp);
        b.setOnClickListener(v->action.run());return b;
    }
    private void go(String name) {
        positions.put(tab,scroll.getScrollY());tab=name;render();
    }
    private void render() {
        scroll.removeAllViews();body=column();body.setPadding(dp(20),dp(10),dp(20),dp(20));scroll.addView(body);
        nav.removeAllViews();
        for (String name:new String[]{"Discover","Map","One week","Bottles"}) {
            Button b=button(name,()->go(name));b.setTextSize(12);
            b.setSelected(tab.equals(name));
            b.setBackground(background(tab.equals(name)?INK:CREAM,0));b.setTextColor(tab.equals(name)?Color.WHITE:INK);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(2),0,dp(2),0);nav.addView(b,lp);
        }
        switch(tab) {
            case "Map": renderMap();break;
            case "One week": renderWeek();break;
            case "Bottles": renderBottles();break;
            default: renderDiscover();
        }
        caption(body,"Sources reviewed 23 Oct 2026 · An independent guide\nCheck current opening hours, bookings and bottle availability before travelling.");
        body.addView(button("About this guide & sources",this::about));
        scroll.post(()->scroll.scrollTo(0,positions.getOrDefault(tab,0)));
    }
    private void renderDiscover() {
        eyebrow(body,"32° N · 17° W",MUTED);
        title(body,"An island worth\ntasting slowly.",36);
        para(body,"Madeira Wine, sugarcane rum and the places that make them. Find your style, then follow it around the island.");
        LinearLayout intro=card(body);eyebrow(intro,"YOUR FIRST VISIT",INK);
        title(intro,"Start with a comparison",24);
        para(intro,"A dry-to-sweet Madeira flight in Funchal. A white-versus-aged rum tasting in Porto da Cruz. Two good starting points for discovering what you actually like.");
        intro.addView(button("Explore the island →",()->go("Map")));
        intro.addView(button("Plan one week →",()->go("One week")));
        for (JSONObject a:guide.entries("articles")) {
            LinearLayout c=card(body);eyebrow(c,a.optString("eyebrow"),a.optString("id").startsWith("rum")?IslandMapView.color("Rum"):INK);
            title(c,a.optString("title"),25);para(c,a.optString("body"));source(c,a.optString("source"));
        }
    }
    private void chips(LinearLayout parent,String[] options,String selected,java.util.function.Consumer<String> action) {
        HorizontalScrollView h=new HorizontalScrollView(this);h.setHorizontalScrollBarEnabled(false);
        LinearLayout row=new LinearLayout(this);h.addView(row);parent.addView(h);
        for (String option:options) {
            Button b=button(option,()->action.accept(option));boolean active=option.equals(selected);b.setSelected(active);
            b.setTextColor(active?Color.WHITE:INK);b.setBackground(background(active?INK:Color.rgb(231,235,222),0));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.setMargins(0,dp(4),dp(8),dp(10));row.addView(b,lp);
        }
    }
    private void renderMap() {
        title(body,"Find your next stop",32);
        caption(body,"Drag to pan · Pinch or double-tap to zoom · Tap a numbered pin\nWine: burgundy · Rum: amber · Local: green");
        map=new IslandMapView(this,guide.places,hits->{
            if (hits.size()==1) placeDialog(hits.get(0));
            else new AlertDialog.Builder(this).setTitle("Places close together")
                    .setItems(hits.stream().map(p->p.optString("name")).toArray(String[]::new),(d,i)->placeDialog(hits.get(i))).show();
        });
        body.addView(map,new LinearLayout.LayoutParams(-1,dp(280)));
        LinearLayout controls=new LinearLayout(this);body.addView(controls);
        for (String label:new String[]{"Zoom in","Zoom out","Reset"}) {
            Button b=button(label,()->{if(label.equals("Reset"))map.reset();else map.zoom(label.equals("Zoom in")?1.6f:1/1.6f);});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(2),dp(6),dp(2),0);controls.addView(b,lp);
        }
        caption(body,"Offline island overview · Approximate pins, not entrances. Use Google Maps for roads and precise destinations. Coastline: Natural Earth (public domain).");
        chips(body,new String[]{"All","Wine","Rum","Local"},category,value->{category=value;positions.put("Map",scroll.getScrollY());render();});
        EditText search=new EditText(this);search.setSingleLine(true);search.setTextColor(INK);search.setHintTextColor(MUTED);
        search.setTextSize(16);search.setHint("Search places or towns");search.setContentDescription("Search places or towns");search.setText(query);
        body.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));
        CheckBox check=new CheckBox(this);check.setText("Saved places only");check.setTextColor(INK);check.setChecked(savedOnly);check.setMinHeight(dp(48));body.addView(check);
        placeList=column();body.addView(placeList);
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){query=s.toString();updatePlaces();}
            public void afterTextChanged(Editable e){}
        });
        check.setOnCheckedChangeListener((v,checked)->{savedOnly=checked;updatePlaces();});updatePlaces();
    }
    private void updatePlaces() {
        List<JSONObject> visible=new ArrayList<>();String needle=query.trim().toLowerCase(Locale.ROOT);
        for (JSONObject p:guide.places) if ((category.equals("All")||category.equals(p.optString("type")))
                &&(!savedOnly||saved.contains(p.optString("id")))
                &&(p.optString("name")+" "+p.optString("area")+" "+p.optString("address")).toLowerCase(Locale.ROOT).contains(needle)) visible.add(p);
        map.setPlaces(visible);placeList.removeAllViews();caption(placeList,visible.size()+" places · Tap a card for tasting tips and directions");
        if(visible.isEmpty()) para(placeList,"No places match. Try another category or search, or turn off the saved-only filter.");
        for(JSONObject p:visible){
            LinearLayout c=card(placeList);eyebrow(c,(guide.places.indexOf(p)+1)+" · "+p.optString("type").toUpperCase(Locale.ROOT)+" · "+p.optString("area"),IslandMapView.color(p.optString("type")));
            title(c,p.optString("name"),24);para(c,p.optString("why"));
            c.addView(button("Visit details →",()->placeDialog(p)));
            c.addView(saveButton(p));
        }
    }
    private Button saveButton(JSONObject p) {
        String id=p.optString("id");Button b=button(saved.contains(id)?"Saved ✓ · Remove":"Save this place",()->{});
        b.setContentDescription((saved.contains(id)?"Remove saved place ":"Save place ")+p.optString("name"));
        b.setOnClickListener(v->{
            if(!saved.remove(id))saved.add(id);
            getPreferences(0).edit().putStringSet("saved",new HashSet<>(saved)).apply();
            b.setText(saved.contains(id)?"Saved ✓ · Remove":"Save this place");
            b.setContentDescription((saved.contains(id)?"Remove saved place ":"Save place ")+p.optString("name"));
            if(tab.equals("Map")&&placeList!=null)updatePlaces();
        });return b;
    }
    private void placeDialog(JSONObject p) {
        LinearLayout c=column();c.setPadding(dp(22),dp(18),dp(22),dp(18));c.setBackgroundColor(CREAM);
        eyebrow(c,p.optString("type").toUpperCase(Locale.ROOT)+" · "+p.optString("area"),IslandMapView.color(p.optString("type")));
        title(c,p.optString("name"),28);para(c,p.optString("why"));
        title(c,"What to ask for",21);para(c,p.optString("order"));
        title(c,"Plan the visit",21);para(c,p.optString("planning"));caption(c,p.optString("address"));
        c.addView(button("Open in Google Maps ↗",()->open(mapsUrl(p))));
        c.addView(button("Visit information / booking ↗",()->open(guide.source(p.optString("booking")).optString("url"))));
        c.addView(saveButton(p));source(c,p.optString("source"));
        ScrollView wrap=new ScrollView(this);wrap.addView(c);
        new AlertDialog.Builder(this).setView(wrap).setPositiveButton("Close",null).show();
    }
    private String destination(JSONObject place) { return place.optString("name")+", "+place.optString("address")+", Madeira, Portugal"; }
    private String mapsUrl(JSONObject place) {
        return Uri.parse("https://www.google.com/maps/search/").buildUpon().appendQueryParameter("api","1").appendQueryParameter("query",destination(place)).build().toString();
    }
    private void renderWeek() {
        title(body,"Seven days,\nat island pace.",34);
        para(body,"A suggested week based in Funchal, grouped by area. Swap days to fit opening times, weather and your bookings. One main tasting a day leaves room for the island itself.");
        LinearLayout note=card(body);eyebrow(note,"BEFORE YOU GO",INK);
        para(note,"Book a designated driver, taxi or guided transfer for tasting days. Keep H&H and Borges on weekdays, and reserve Hinton in advance. The order below is a suggestion, not a confirmed itinerary.");
        int day=0;
        for(JSONObject d:guide.entries("days")) {
            LinearLayout c=card(body);eyebrow(c,"DAY "+(++day)+" · "+d.optString("area"),INK);
            title(c,d.optString("title"),25);para(c,d.optString("body"));caption(c,d.optString("transport"));
            JSONArray stops=d.optJSONArray("places");
            for(int i=0;i<stops.length();i++) {JSONObject place=guide.place(stops.optString(i));c.addView(button(place.optString("name")+" →",()->placeDialog(place)));}
            // Route buttons only connect intended paired visits, not alternative tasting venues.
            if(day==4) c.addView(button("See this route in Google Maps ↗",()->open(Uri.parse("https://www.google.com/maps/dir/").buildUpon()
                    .appendQueryParameter("api","1").appendQueryParameter("origin",destination(guide.place("barbusano")))
                    .appendQueryParameter("destination",destination(guide.place("poncha"))).appendQueryParameter("travelmode","driving").build().toString())));
        }
    }
    private void renderBottles() {
        title(body,"Find your favourite",34);
        para(body,"Fame is not a flavour. Malmsey is a classic rich Madeira style; Sercial is the dry direction. For rum, start by comparing 970 Reserva 6 Anos with William Hinton 6 Years on separate visits.");
        caption(body,"Editorial tasting picks, not a league table. Prices and stock change; taste before choosing an expensive bottle.");
        chips(body,new String[]{"All","Dry","Balanced","Rich","Special","Rum"},taste,value->{taste=value;positions.put("Bottles",0);render();});
        for(JSONObject b:guide.entries("bottles")) if(taste.equals("All")||taste.equals(b.optString("style"))) {
            LinearLayout c=card(body);eyebrow(c,b.optString("style").toUpperCase(Locale.ROOT),b.optString("style").equals("Rum")?IslandMapView.color("Rum"):IslandMapView.color("Wine"));
            title(c,b.optString("title"),25);para(c,b.optString("body"));caption(c,b.optString("tip"));
            c.addView(button("Where to try it →",()->placeDialog(guide.place(b.optString("place")))));source(c,b.optString("source"));
        }
    }
    private void source(LinearLayout parent,String id) {
        JSONObject s=guide.source(id);Button b=button("Source: "+s.optString("name")+" ↗",()->open(s.optString("url")));b.setTextSize(12);parent.addView(b);
    }
    private void open(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); }
        catch(ActivityNotFoundException e) { new AlertDialog.Builder(this).setTitle("No browser available").setMessage(url)
                .setPositiveButton("Copy link",(d,w)->{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);clipboard.setPrimaryClip(ClipData.newPlainText("Link",url));Toast.makeText(this,"Link copied",Toast.LENGTH_SHORT).show();})
                .setNegativeButton("Close",null).show(); }
    }
    private void about() {
        LinearLayout c=column();c.setPadding(dp(20),dp(20),dp(20),dp(20));c.setBackgroundColor(CREAM);
        title(c,"About the guide",28);
        para(c,"Independent recommendations researched on 23 October 2026. Producer pages, Madeira’s tourism board and IVBAM support the factual content. Tasting picks and the week plan are editorial suggestions.");
        para(c,"All guide content and the overview map work offline. Saved places stay on this device. There are no analytics or location permissions. External links open Google Maps or your browser and may need a connection.");
        caption(c,"Map pins are approximate area markers, not surveyed entrances. Overlapping markers open a choice of places; the list offers the same places without using the map.");
        c.addView(button("Natural Earth map data & public-domain terms ↗",()->open("https://www.naturalearthdata.com/about/terms-of-use/")));
        JSONObject sources=guide.data.optJSONObject("sources");Iterator<String> keys=sources.keys();
        while(keys.hasNext())source(c,keys.next());
        ScrollView wrap=new ScrollView(this);wrap.addView(c);new AlertDialog.Builder(this).setView(wrap).setPositiveButton("Close",null).show();
    }
}
