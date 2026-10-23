package org.dergigi.madeirawineandrum;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bundled editorial content: usable without a connection, with per-entry sources. */
final class Guide {
    final JSONObject data;
    final List<JSONObject> places;
    Guide(Context context, String language) throws IOException, JSONException {
        data = new JSONObject(read(context, language.equals("de")?"guide-de.json":"guide.json"));
        places = list(data.getJSONArray("places"));
    }
    static String read(Context context, String name) throws IOException {
        try (InputStream in = context.getAssets().open(name);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096]; int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
    static List<JSONObject> list(JSONArray array) {
        List<JSONObject> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) result.add(array.optJSONObject(i));
        return result;
    }
    List<JSONObject> entries(String key) { return list(data.optJSONArray(key)); }
    JSONObject place(String id) {
        for (JSONObject place : places) if (id.equals(place.optString("id"))) return place;
        throw new IllegalArgumentException("Unknown place: " + id);
    }
    JSONObject source(String id) { return data.optJSONObject("sources").optJSONObject(id); }
}
