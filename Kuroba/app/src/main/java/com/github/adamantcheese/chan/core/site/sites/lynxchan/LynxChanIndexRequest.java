/*
 * Kuroba - *chan browser https://github.com/TheStranjer/Kuroba (fork of https://github.com/Adamantcheese/Kuroba)
 * Copyright (C) 2019 TheStranjer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.adamantcheese.chan.core.site.sites.lynxchan;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import com.github.adamantcheese.chan.core.net.JsonReaderRequest;
import android.util.JsonReader;
import android.util.JsonToken;

import okhttp3.HttpUrl;

public class LynxChanIndexRequest extends JsonReaderRequest<LynxChanIndexResult> {
    private static final String TAG = "LynxChanInstanceCheck";
    public LynxChanIndexRequest(String hostname, Listener<LynxChanIndexResult> listener, ErrorListener errorListener) {
        super(new HttpUrl.Builder().scheme("https").host(hostname).addPathSegment("index.json").build().toString(), listener, errorListener);
    }

    @Override
    public LynxChanIndexResult readJson(JsonReader reader) throws Exception {
        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.peek() != JsonToken.NAME) {
                reader.skipValue();
                continue;
            }

            String key = reader.nextName();
            switch (key) {
                case "version":
                    String nextString = reader.nextString();
                    return new LynxChanIndexResult(nextString);
                default:
                    break;
            }
        }

        return new LynxChanIndexResult();
    }


}
