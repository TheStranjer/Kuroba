package com.github.adamantcheese.chan.core.site.sites.lynxchan;

public class LynxChanIndexResult {
    public String version;

    public LynxChanIndexResult(String version) {
        this.version = version;
    }

    public LynxChanIndexResult() {}

    public boolean IsLynxChanInstance() {
        return version != null;
    }
}