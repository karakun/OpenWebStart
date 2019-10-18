package com.openwebstart.app;

import net.sourceforge.jnlp.cache.cache.Cache;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationManager {

    private final static ApplicationManager INSTANCE = new ApplicationManager();

    private ApplicationManager() {
    }

    public List<Application> getAllApplications() {
        return Cache.getJnlpCacheIds().stream().map(c -> new Application(c)).collect(Collectors.toList());
    }

    public static ApplicationManager getInstance() {
        return INSTANCE;
    }
}
