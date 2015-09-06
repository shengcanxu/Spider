package web.cano.spider.scheduler.component;

import com.google.common.collect.Sets;
import web.cano.spider.Page;
import web.cano.spider.Task;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author code4crafer@gmail.com
 */
public class HashSetDuplicateRemover implements DuplicateRemover {

    private Set<String> urls = Sets.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Override
    public boolean isDuplicate(Page page, Task task) {
        return !urls.add(page.getUrl());
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        return urls.size();
    }
}
