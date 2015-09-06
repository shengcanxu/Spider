package web.cano.spider.scheduler.component;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import web.cano.spider.Page;
import web.cano.spider.Task;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BloomFilterDuplicateRemover for huge number of urls.
 *
 * @author code4crafer@gmail.com
 * @since 0.5.1
 */
public class BloomFilterDuplicateRemover implements DuplicateRemover {

    private int expectedInsertions;

    private double fpp;

    private AtomicInteger counter;

    public BloomFilterDuplicateRemover(int expectedInsertions) {
        this(expectedInsertions, 0.01);
    }

    /**
     *
     * @param expectedInsertions the number of expected insertions to the constructed
     * @param fpp the desired false positive probability (must be positive and less than 1.0)
     */
    public BloomFilterDuplicateRemover(int expectedInsertions, double fpp) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        this.bloomFilter = rebuildBloomFilter();
    }

    protected BloomFilter<CharSequence> rebuildBloomFilter() {
        counter = new AtomicInteger(0);
        return BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), expectedInsertions, fpp);
    }

    private final BloomFilter<CharSequence> bloomFilter;

    @Override
    public boolean isDuplicate(Page page, Task task) {
        boolean isDuplicate = bloomFilter.mightContain(page.getUrl());
        if (!isDuplicate) {
            bloomFilter.put(page.getUrl());
            counter.incrementAndGet();
        }
        return isDuplicate;
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        return counter.get();
    }
}
