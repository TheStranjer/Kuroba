/*
 * Kuroba - *chan browser https://github.com/Adamantcheese/Kuroba/
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
package com.github.adamantcheese.chan.core.site;

import com.github.adamantcheese.chan.core.model.orm.SiteModel;
import com.github.adamantcheese.chan.core.repository.SiteRepository;
import com.github.adamantcheese.chan.core.settings.json.JsonSettings;

import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;

import com.github.adamantcheese.chan.core.site.sites.lynxchan.LynxChanIndexRequest;
import com.github.adamantcheese.chan.core.site.sites.lynxchan.LynxChanIndexResult;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;

import javax.inject.Inject;

public class SiteService {
    private SiteRepository siteRepository;
    private SiteResolver resolver;

    private boolean initialized = false;

    RequestQueue requestQueue;

    @Inject
    public SiteService(SiteRepository siteRepository, SiteResolver resolver) {
        this.siteRepository = siteRepository;
        this.resolver = resolver;
    }

    public boolean areSitesSetup() {
        return !siteRepository.all().getAll().isEmpty();
    }

    public void addSite(String url, Context ctx, SiteAddCallback callback) {
        Site existing = resolver.findSiteForUrl(url);
        if (existing != null) {
            callback.onSiteAddFailed("site already added");
            return;
        }

        SiteResolver.SiteResolverResult resolve = resolver.resolveSiteForUrl(url);

        Class<? extends Site> siteClass;
        if (resolve.match == SiteResolver.SiteResolverResult.Match.BUILTIN) {
            siteClass = resolve.builtinResult;
        } else if (resolve.match == SiteResolver.SiteResolverResult.Match.EXTERNAL) {
            callback.onSiteAddFailed("not detected in predefined list, checking if LynxChan instance...");

            Cache cache = new DiskBasedCache(ctx.getCacheDir(), 1024 * 1024); // 1MB cap
            Network network = new BasicNetwork(new HurlStack());
            requestQueue = new RequestQueue(cache, network);
            requestQueue.start();
            try {
                URI uri = new URI(url);

                // hacky little trick that gets around the fact that new URI("16chan.xyz")
                // sets the host to null and the path to "16chan.xyz" while
                // new URI("https://16chan.xyz/") sets the host to "16chan.xyz" and the
                // path to null
                String host = uri.getHost() != null ? uri.getHost() : uri.getPath(); 
                requestQueue.add(new LynxChanIndexRequest(host, response -> { lynxChanIndexResponse(response, callback); }, error -> { lynxChanIndexFailure(error, callback); }));
            } catch (URISyntaxException e) {
                callback.onSiteAddFailed("not a url");
            }

            return;
        } else {
            callback.onSiteAddFailed("not a url");
            return;
        }

        Site site = siteRepository.createFromClass(siteClass);

        callback.onSiteAdded(site);
    }

    public void lynxChanIndexResponse(LynxChanIndexResult response, SiteAddCallback callback) {
        if (response.IsLynxChanInstance()) {
            callback.onSiteAddFailed("detected LynxChan instance, but not implemented yet");
        } else {
            callback.onSiteAddFailed("not a valid LynxChan instance");
        }
    }

    public void lynxChanIndexFailure(VolleyError error, SiteAddCallback callback) {
        callback.onSiteAddFailed("not in premade list and can't detect chansite instance");
    }

    public void updateUserSettings(Site site, JsonSettings jsonSettings) {
        SiteModel siteModel = siteRepository.byId(site.id());
        if (siteModel == null) throw new NullPointerException("siteModel == null");
        siteRepository.updateSiteUserSettingsAsync(siteModel, jsonSettings);
    }

    public void updateOrdering(List<Site> sitesInNewOrder) {
        siteRepository.updateSiteOrderingAsync(sitesInNewOrder);
    }

    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("Already initialized");
        }
        initialized = true;
        siteRepository.initialize();
    }

    public interface SiteAddCallback {
        void onSiteAdded(Site site);

        void onSiteAddFailed(String message);
    }
}
