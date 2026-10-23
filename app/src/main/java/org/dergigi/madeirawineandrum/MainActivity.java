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
    private static final int INK=Color.rgb(25,59,49), MUTED=Color.rgb(98,110,102), CREAM=Color.rgb(248,246,239);
    private Guide guide;
    private JSONObject media;
    private PhotoLoader photos;
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
        try { guide=new Guide(this); media=new JSONObject(Guide.read(this,"media.json"));photos=new PhotoLoader(this); }
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
        LinearLayout masthead=new LinearLayout(this);masthead.setGravity(Gravity.CENTER_VERTICAL);
        masthead.setPadding(dp(22),dp(12),dp(16),dp(12));
        LinearLayout wordmark=column();TextView brand=text("Madeira Wine",25,INK);
        brand.setTypeface(Typeface.create("serif",Typeface.NORMAL));wordmark.addView(brand);
        TextView small=text("& RUM",9,MUTED);small.setLetterSpacing(.2f);wordmark.addView(small);
        masthead.addView(wordmark,new LinearLayout.LayoutParams(0,-2,1));
        Button info=button("i",this::about);info.setContentDescription("About, photography credits and sources");
        masthead.addView(info,new LinearLayout.LayoutParams(dp(48),dp(48)));root.addView(masthead);
        scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setId(View.generateViewId());
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this);nav.setPadding(dp(8),dp(8),dp(8),dp(8));root.addView(nav);
        setContentView(root);render();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        positions.put(tab,scroll.getScrollY());
        out.putString("tab",tab);out.putString("category",category);out.putString("query",query);out.putString("taste",taste);out.putBoolean("savedOnly",savedOnly);
        for (String name:positions.keySet()) out.putInt("scroll_"+name,positions.get(name));
        super.onSaveInstanceState(out);
    }
    @Override protected void onDestroy() { if(photos!=null)photos.close();super.onDestroy(); }
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
    private Button button(String label,Runnable action) {
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setTextColor(INK);
        b.setMinHeight(dp(48));b.setMinimumHeight(dp(48));b.setPadding(dp(12),dp(8),dp(12),dp(8));
        b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x18315F53),background(Color.rgb(235,239,227),0),null));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(4);lp.bottomMargin=dp(6);b.setLayoutParams(lp);
        b.setOnClickListener(v->action.run());return b;
    }
    private void go(String name) {
        positions.put(tab,scroll.getScrollY());tab=name;render();
    }
    private void render() {
        scroll.removeAllViews();body=column();body.setPadding(dp(20),dp(10),dp(20),dp(20));scroll.addView(body);
        nav.removeAllViews();
        String[] tabs={"Discover","Map","One week","Bottles"};
        String[] labels={"Discover","Map","Week","Taste"};
        for(int i=0;i<tabs.length;i++) {
            String name=tabs[i];boolean active=tab.equals(name);
            LinearLayout item=column();item.setGravity(Gravity.CENTER);item.setPadding(dp(4),dp(8),dp(4),dp(6));
            item.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x18315F53),background(active?Color.rgb(230,236,222):CREAM,0),null));
            item.addView(new NavIcon(this,i,active?INK:MUTED),new LinearLayout.LayoutParams(dp(23),dp(23)));
            TextView label=text(labels[i],11,active?INK:MUTED);label.setPadding(0,dp(4),0,0);label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);item.addView(label);
            item.setSelected(active);item.setContentDescription(name);item.setFocusable(true);item.setOnClickListener(v->go(name));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(3),0,dp(3),0);nav.addView(item,lp);
        }
        switch(tab) {
            case "Map": renderMap();break;
            case "One week": renderWeek();break;
            case "Bottles": renderBottles();break;
            default: renderDiscover();
        }
        body.addView(link("Photography & sources",this::about));
        scroll.post(()->scroll.scrollTo(0,positions.getOrDefault(tab,0)));
    }
    private void renderDiscover() {
        photoCard(body,"coast","THE ATLANTIC, IN A GLASS","Madeira,\nby the glass.","Explore the island  →",350,()->go("Map"),false);
        section(body,"What are you in the mood for?");
        LinearLayout rail=rail(body);
        String[][] stories={{"wine","WINE","Madeira Wine","From dry to golden and rich"},{"rum","RUM","Cane. Cask. Coast.","Discover agricultural rum"},{"poncha","LOCAL LIFE","Meet the poncha","The island’s citrus ritual"}};
        for(String[] story:stories) {
            FrameLayout tile=photoCard(rail,story[0],story[1],story[2],story[3],250,()->storyDialog(story[0]),false);railSize(tile,250);
        }
        section(body,"Watch the island");
        LinearLayout films=rail(body);
        for(JSONObject film:Guide.list(media.optJSONArray("videos"))) {
            FrameLayout tile=photoCard(films,film.optString("photo"),"YOUTUBE · "+film.optString("creator"),film.optString("title"),"Watch on YouTube  ↗",215,()->open(film.optString("url")),true);railSize(tile,290);
        }
        section(body,"Make a week of it");
        photoCard(body,"harbour","YOUR ISLAND ITINERARY","Seven days.\nA different flavour.","See the week  →",240,()->go("One week"),false);
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
        title(body,"Around the island",32);
        caption(body,"Tap a pin. Find your next pour.");
        map=new IslandMapView(this,guide.places,hits->{
            if (hits.size()==1) placeDialog(hits.get(0));
            else new AlertDialog.Builder(this).setTitle("Places close together")
                    .setItems(hits.stream().map(p->p.optString("name")).toArray(String[]::new),(d,i)->placeDialog(hits.get(i))).show();
        });
        map.setBackground(background(Color.rgb(223,234,230),0));map.setClipToOutline(true);
        body.addView(map,new LinearLayout.LayoutParams(-1,dp(300)));
        LinearLayout controls=new LinearLayout(this);body.addView(controls);
        for (String label:new String[]{"Zoom in","Zoom out","Reset"}) {
            Button b=button(label,()->{if(label.equals("Reset"))map.reset();else map.zoom(label.equals("Zoom in")?1.6f:1/1.6f);});
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(2),dp(6),dp(2),0);controls.addView(b,lp);
        }
        LinearLayout legend=new LinearLayout(this);body.addView(legend);
        for(String type:new String[]{"Wine","Rum","Local"}) {TextView label=text("●  "+type,12,IslandMapView.color(type));label.setPadding(0,dp(8),dp(20),dp(8));legend.addView(label);}
        caption(body,"Pinch to zoom · Approximate locations");
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
        map.setPlaces(visible);placeList.removeAllViews();caption(placeList,visible.size()+" places to explore");
        if(visible.isEmpty()) para(placeList,"No places match. Try another category or search, or turn off the saved-only filter.");
        for(JSONObject p:visible){
            String image=placePhoto(p);
            FrameLayout tile=photoCard(placeList,image,(guide.places.indexOf(p)+1)+" · "+p.optString("type").toUpperCase(Locale.ROOT),p.optString("name"),p.optString("area")+"  →",210,()->placeDialog(p),false);
            if(saved.contains(p.optString("id"))) tile.setContentDescription(tile.getContentDescription()+", saved place");
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
        photoCard(c,placePhoto(p),media.optJSONObject("photos").optJSONObject(placePhoto(p)).optString("caption"),"","",210,null,false);
        eyebrow(c,p.optString("type").toUpperCase(Locale.ROOT)+" · "+p.optString("area"),IslandMapView.color(p.optString("type")));
        title(c,p.optString("name"),28);para(c,p.optString("why"));
        title(c,"What to ask for",21);para(c,p.optString("order"));
        fold(c,"Plan your visit",()->{LinearLayout detail=column();para(detail,p.optString("planning"));caption(detail,p.optString("address"));return detail;});
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
        title(body,"A week to savour",34);
        caption(body,"One area a day. Tap a day to make it yours.");
        String[] images={"barrels","harbour","calheta","vineyard","rum","borges","wine"};int i=0;
        for(JSONObject day:guide.entries("days")) {
            final int index=i;
            photoCard(body,images[i],"DAY "+(i+1),day.optString("area"),day.optString("title")+"  →",205,()->dayDialog(day,index),false);i++;
        }
    }
    private void dayDialog(JSONObject day,int index) {
        LinearLayout c=detail();eyebrow(c,"DAY "+(index+1),MUTED);title(c,day.optString("title"),29);
        para(c,day.optString("body"));caption(c,day.optString("transport"));
        for(int i=0;i<day.optJSONArray("places").length();i++) {
            JSONObject place=guide.place(day.optJSONArray("places").optString(i));c.addView(button(place.optString("name")+" →",()->placeDialog(place)));
        }
        if(index==3)c.addView(button("Open route in Google Maps ↗",()->open(Uri.parse("https://www.google.com/maps/dir/").buildUpon()
                .appendQueryParameter("api","1").appendQueryParameter("origin",destination(guide.place("barbusano")))
                .appendQueryParameter("destination",destination(guide.place("poncha"))).appendQueryParameter("travelmode","driving").build().toString())));
        fold(c,"Before you go",()->{LinearLayout more=column();para(more,"Swap days to fit opening hours and bookings. Plan a driver or taxi for tasting days. H&H and Borges are weekday stops; book Hinton ahead.");return more;});
        showDetail(c);
    }
    private void renderBottles() {
        title(body,"Find your flavour",34);
        caption(body,"Dry & bright, rich & golden, or a little rum?");
        chips(body,new String[]{"All","Dry","Balanced","Rich","Special","Rum"},taste,value->{taste=value;positions.put("Bottles",0);render();});
        for(JSONObject b:guide.entries("bottles")) if(taste.equals("All")||taste.equals(b.optString("style"))) {
            String image;
            switch(b.optString("style")) {
                case "Balanced":image="bottle";break;
                case "Special":image="oliveiras";break;
                case "Rum":image=b.optString("place").equals("norte")?"rum_casks":b.optString("place").equals("hinton")?"calheta":"poncha";break;
                case "Rich":image="barrels";break;
                default:image="wine";
            }
            photoCard(body,image,b.optString("style").toUpperCase(Locale.ROOT),b.optString("title"),"Tasting notes & where to try  →",240,()->bottleDialog(b),false);
        }
    }
    private void bottleDialog(JSONObject bottle) {
        LinearLayout c=detail();eyebrow(c,bottle.optString("style").toUpperCase(Locale.ROOT),MUTED);title(c,bottle.optString("title"),29);
        para(c,bottle.optString("body"));caption(c,bottle.optString("tip"));
        c.addView(button("Where to try it →",()->placeDialog(guide.place(bottle.optString("place")))));
        source(c,bottle.optString("source"));showDetail(c);
    }
    private void source(LinearLayout parent,String id) {
        JSONObject s=guide.source(id);parent.addView(link("Source · "+s.optString("name")+" ↗",()->open(s.optString("url"))));
    }
    private void open(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); }
        catch(ActivityNotFoundException e) { new AlertDialog.Builder(this).setTitle("No browser available").setMessage(url)
                .setPositiveButton("Copy link",(d,w)->{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);clipboard.setPrimaryClip(ClipData.newPlainText("Link",url));Toast.makeText(this,"Link copied",Toast.LENGTH_SHORT).show();})
                .setNegativeButton("Close",null).show(); }
    }
    private TextView link(String label,Runnable action) {
        TextView v=text(label,12,MUTED);v.setMinHeight(dp(48));v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(0,dp(10),0,dp(10));v.setFocusable(true);v.setOnClickListener(w->action.run());return v;
    }
    private void section(LinearLayout parent,String label) {
        TextView v=text(label,23,INK);v.setTypeface(Typeface.create("serif",Typeface.NORMAL));v.setPadding(0,dp(20),0,dp(14));
        if(android.os.Build.VERSION.SDK_INT>=28)v.setAccessibilityHeading(true);parent.addView(v);
    }
    private LinearLayout rail(LinearLayout parent) {
        HorizontalScrollView scroller=new HorizontalScrollView(this);scroller.setHorizontalScrollBarEnabled(false);scroller.setClipToPadding(false);
        LinearLayout row=new LinearLayout(this);scroller.addView(row);parent.addView(scroller);return row;
    }
    private void railSize(View tile,int width) {
        LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)tile.getLayoutParams();
        lp.width=dp(Math.min(width,getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density-64));lp.rightMargin=dp(14);tile.setLayoutParams(lp);
    }
    private FrameLayout photoCard(LinearLayout parent,String key,String kicker,String heading,String subtitle,int height,Runnable action,boolean play) {
        FrameLayout frame=new FrameLayout(this);frame.setBackground(background(Color.rgb(198,205,187),0));frame.setClipToOutline(true);
        int extra=Math.round(Math.max(0,getResources().getConfiguration().fontScale-1)*150);
        LinearLayout.LayoutParams size=new LinearLayout.LayoutParams(-1,dp(height+extra));size.bottomMargin=dp(14);parent.addView(frame,size);
        ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        frame.addView(image,new FrameLayout.LayoutParams(-1,-1));JSONObject photo=media.optJSONObject("photos").optJSONObject(key);photos.load(photo.optString("file"),image);
        View shade=new View(this);shade.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x100F201B,0x180F201B,0xF00F201B}));frame.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout words=column();words.setPadding(dp(20),dp(20),dp(20),dp(20));words.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        FrameLayout.LayoutParams textSize=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);frame.addView(words,textSize);
        if(!kicker.isEmpty()){TextView tag=text(kicker,10,0xFFF0E7CF);tag.setLetterSpacing(.13f);words.addView(tag);}
        if(!heading.isEmpty()){TextView name=text(heading,height>=300?38:26,Color.WHITE);name.setTypeface(Typeface.create("serif",Typeface.NORMAL));name.setPadding(0,dp(7),0,dp(5));words.addView(name);}
        if(!subtitle.isEmpty())words.addView(text(subtitle,12,0xFFF2EFDF));
        if(play){TextView icon=text("▶",21,Color.WHITE);icon.setGravity(Gravity.CENTER);icon.setBackground(background(0x8A193B31,0));
            icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);FrameLayout.LayoutParams pl=new FrameLayout.LayoutParams(dp(50),dp(50),Gravity.TOP|Gravity.LEFT);pl.setMargins(dp(18),dp(18),0,0);frame.addView(icon,pl);}
        Button credit=button("i",()->photoCredit(key));credit.setTextSize(12);credit.setTextColor(Color.WHITE);credit.setBackground(background(0x8A193B31,0));
        credit.setContentDescription("Photo: "+photo.optString("caption")+". View photographer and license");
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.TOP|Gravity.RIGHT);cp.setMargins(0,dp(8),dp(8),0);frame.addView(credit,cp);
        if(action!=null){frame.setFocusable(true);frame.setContentDescription(kicker+". "+heading+". "+subtitle);frame.setOnClickListener(v->action.run());
            frame.setForeground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x35FFFFFF),null,background(Color.WHITE,0)));}
        return frame;
    }
    private String placePhoto(JSONObject place) {
        switch(place.optString("id")) {
            case "blandys":return "barrels";case "borges":return "borges";case "oliveiras":return "oliveiras";
            case "hh":case "barbeito":return "harbour";case "calheta":return "calheta_mill";
            case "hinton":return "calheta";case "norte":return "rum";case "barbusano":return "vineyard";default:return "poncha";
        }
    }
    private LinearLayout detail(){LinearLayout c=column();c.setPadding(dp(22),dp(20),dp(22),dp(20));c.setBackgroundColor(CREAM);return c;}
    private AlertDialog showDetail(LinearLayout c){ScrollView wrap=new ScrollView(this);wrap.addView(c);return new AlertDialog.Builder(this).setView(wrap).setPositiveButton("Close",null).show();}
    private void fold(LinearLayout parent,String label,java.util.function.Supplier<View> content) {
        Button toggle=button(label+"  +",()->{});parent.addView(toggle);LinearLayout expanded=column();expanded.setVisibility(View.GONE);parent.addView(expanded);
        toggle.setContentDescription(label+", collapsed");toggle.setOnClickListener(v->{boolean opening=expanded.getVisibility()!=View.VISIBLE;
            if(opening&&expanded.getChildCount()==0)expanded.addView(content.get());expanded.setVisibility(opening?View.VISIBLE:View.GONE);
            toggle.setText(label+(opening?"  −":"  +"));toggle.setContentDescription(label+(opening?", expanded":", collapsed"));});
    }
    private void storyDialog(String story) {
        LinearLayout c=detail();photoCard(c,story,"", "", "",180,null,false);
        String[] ids=story.equals("wine")?new String[]{"wine","styles","ageing","labels","contrast"}:story.equals("rum")?new String[]{"rum","rum_styles"}:new String[]{"local","nikita","tips"};
        boolean first=true;
        for(String id:ids) for(JSONObject article:guide.entries("articles"))if(id.equals(article.optString("id"))) {
            if(first){title(c,article.optString("title"),27);para(c,article.optString("body"));source(c,article.optString("source"));first=false;}
            else fold(c,article.optString("title"),()->{LinearLayout more=column();para(more,article.optString("body"));source(more,article.optString("source"));return more;});
        }
        Button explore=button(story.equals("poncha")?"Find a poncha stop →":"Find a tasting →",()->{});c.addView(explore);
        AlertDialog dialog=showDetail(c);
        explore.setOnClickListener(v->{dialog.dismiss();category=story.equals("wine")?"Wine":story.equals("rum")?"Rum":"Local";query="";savedOnly=false;positions.put("Map",0);go("Map");});
    }
    private void photoCredit(String key) {
        JSONObject p=media.optJSONObject("photos").optJSONObject(key);LinearLayout c=detail();title(c,p.optString("caption"),26);
        para(c,p.optString("artist"));caption(c,p.optString("changes"));
        c.addView(button("Original photograph ↗",()->open(p.optString("source"))));c.addView(button(p.optString("license")+" ↗",()->open(p.optString("licenseUrl"))));showDetail(c);
    }
    private void photoCredits() {
        LinearLayout c=detail();title(c,"Behind the photographs",28);caption(c,"Real Madeira, photographed by the people credited here. Images retain their Creative Commons licenses.");
        Iterator<String> keys=media.optJSONObject("photos").keys();while(keys.hasNext()){String key=keys.next();JSONObject p=media.optJSONObject("photos").optJSONObject(key);c.addView(button(p.optString("caption")+" · "+p.optString("artist"),()->photoCredit(key)));}showDetail(c);
    }
    private void about() {
        LinearLayout c=column();c.setPadding(dp(20),dp(20),dp(20),dp(20));c.setBackgroundColor(CREAM);
        title(c,"About the guide",28);
        para(c,"Independent recommendations researched on 23 October 2026. Producer pages, Madeira’s tourism board and IVBAM support the factual content. Tasting picks and the week plan are editorial suggestions.");
        para(c,"All guide content and the overview map work offline. Saved places stay on this device. There are no analytics or location permissions. External links open Google Maps or your browser and may need a connection.");
        caption(c,"Map pins are approximate area markers, not surveyed entrances. Overlapping markers open a choice of places; the list offers the same places without using the map.");
        c.addView(button("Natural Earth map data & public-domain terms ↗",()->open("https://www.naturalearthdata.com/about/terms-of-use/")));
        c.addView(button("Photography credits",this::photoCredits));
        caption(c,"Regional photos set the scene where a venue photo is unavailable. Video cards use illustrative photos, not footage. Videos open on YouTube after a tap; nothing autoplays.");
        fold(c,"Guide sources",()->{LinearLayout links=column();JSONObject sources=guide.data.optJSONObject("sources");Iterator<String> keys=sources.keys();while(keys.hasNext())source(links,keys.next());return links;});
        ScrollView wrap=new ScrollView(this);wrap.addView(c);new AlertDialog.Builder(this).setView(wrap).setPositiveButton("Close",null).show();
    }
}
