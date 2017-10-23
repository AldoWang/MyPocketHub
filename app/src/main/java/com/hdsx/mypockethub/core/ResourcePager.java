package com.hdsx.mypockethub.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class ResourcePager<E> {

    private PageIterator<E> iterator;
    private int page = 1;
    private int count = 1;
    private Map<Object, E> resources = new LinkedHashMap<>();
    protected boolean hasMore;

    public void reset() {
        page = 1;
        clear();
    }

    public ResourcePager<E> clear() {
        count = Math.max(1, page - 1);
        iterator = null;
        page = 1;
        resources.clear();
        hasMore = true;
        return this;
    }

    public boolean next() {
        boolean emptyPage = false;
        if (iterator == null) {
            iterator = createIterator(page, -1);
        }

        try {
            for (int i = 0; i < count && iterator.hasNext(); i++) {
                List<E> resourcePage = iterator.next();
                emptyPage = resourcePage.isEmpty();
                if (emptyPage)
                    break;

                for (E resource : resourcePage) {
                    resource = register(resource);
                    if (resource == null)
                        continue;
                    resources.put(getId(resource), resource);
                }
            }

            if (count > 1) {
                page = count;
                count = 1;
            }

            page++;
        } catch (NoSuchElementException e) {
            hasMore = false;
            e.printStackTrace();
        }

        hasMore = iterator.hasNext() && !emptyPage;
        return hasMore;
    }

    protected abstract Object getId(E resource);

    protected E register(E resource) {
        return resource;
    }

    public List<E> getResources() {
        return new ArrayList<>(resources.values());
    }

    public boolean hasMore() {
        return hasMore;
    }

    public int size() {
        return resources.size();
    }

    protected abstract PageIterator<E> createIterator(int page, int size);

}
