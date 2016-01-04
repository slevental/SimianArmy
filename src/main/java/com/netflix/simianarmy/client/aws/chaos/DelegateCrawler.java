package com.netflix.simianarmy.client.aws.chaos;

import com.netflix.simianarmy.chaos.ChaosCrawler;

import java.util.EnumSet;
import java.util.List;

/**
 * Simple wrapper for crawler, used by docker crawler,
 * docker containers might be available on machines crawled by auto scaling groups or
 * tag-based crawler or any other way.
 */
public class DelegateCrawler implements ChaosCrawler {

    protected final ChaosCrawler delegate;

    public DelegateCrawler(ChaosCrawler delegate) {
        this.delegate = delegate;
    }

    @Override
    public EnumSet<?> groupTypes() {
        return delegate.groupTypes();
    }

    @Override
    public List<InstanceGroup> groups() {
        return delegate.groups();
    }

    @Override
    public List<InstanceGroup> groups(String... names) {
        return delegate.groups(names);
    }
}
