package com.ohuang.hotupdate;

import com.ohuang.patchtinker.tinker.TinkerApplication;

public class TkApp extends TinkerApplication {
    @Override
    public String getApplicationLikeClassName() {
        return "com.ohuang.hotupdate.AppImpl";
    }
}
