package org.dergigi.madeirawineandrum;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.InputStream;
import java.util.concurrent.*;

/** Small, offline image pipeline. Decode away from the UI thread and reuse bitmaps. */
final class PhotoLoader {
    private final Context context;
    private final ExecutorService executor=Executors.newFixedThreadPool(2);
    private final LruCache<String,Bitmap> cache=new LruCache<String,Bitmap>(16*1024*1024) {
        @Override protected int sizeOf(String key,Bitmap bitmap) { return bitmap.getAllocationByteCount(); }
    };
    PhotoLoader(Context context) { this.context=context.getApplicationContext(); }
    void load(String file,ImageView view) {
        view.setTag(file);
        Bitmap cached=cache.get(file);
        if(cached!=null) { view.setImageBitmap(cached);return; }
        executor.execute(()->{
            Bitmap image=cache.get(file);
            if(image==null) {
                try(InputStream input=context.getAssets().open(file)) {
                    image=BitmapFactory.decodeStream(input);
                    if(image!=null) cache.put(file,image);
                } catch(java.io.IOException ignored) { /* The warm placeholder remains if an asset cannot load. */ }
            }
            Bitmap ready=image;
            if(ready!=null) view.post(()->{if(file.equals(view.getTag()))view.setImageBitmap(ready);});
        });
    }
    void close() { executor.shutdownNow();cache.evictAll(); }
}
