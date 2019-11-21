package com.openwebstart.app;

import com.openwebstart.func.Result;
import net.adoptopenjdk.icedteaweb.resources.cache.Cache;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationManager {

    private final static ApplicationManager INSTANCE = new ApplicationManager();

    private ApplicationManager() {
    }

    public List<Result<Application>> getAllApplications() {
        return Cache.getJnlpCacheIds().stream().map(Result.of(Application::new)).collect(Collectors.toList());
    }

    public static ApplicationManager getInstance() {
        return INSTANCE;
    }
}
