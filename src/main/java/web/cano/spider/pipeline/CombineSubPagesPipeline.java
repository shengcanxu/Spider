package web.cano.spider.pipeline;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombineSubPagesPipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String,Page[]> deferalMap = new HashMap<String, Page[]>();

    @Override
    public void process(Page page, Task task) {
        if (!page.hasSubPages() && !page.isSubPage()) return;
        page.setSkip(true);

        if(page.isSubPage()){
            String key = page.getParentPageKey();
            Page[] pages = deferalMap.get(key);
            if(pages == null){
                return;
            }else{
                int index = 1;
                for(;index < pages.length; index++){
                    if(pages[index] == null){
                        pages[index] = page;
                    }
                }
                if(index >= pages.length){  //所有subpages都爬取完了
                    combineSubpages(pages,page);
                    page.setSkip(false);
                }
            }

        }else if(page.hasSubPages()){
            int subPagesNumber = page.getSubPagesNumber();
            Page[] pages = new Page[subPagesNumber+1];
            pages[0] = page;

            String key = DigestUtils.md5Hex(page.getUrl());
            deferalMap.put(key,pages);
        }
    }

    private void combineSubpages(Page[] pages,Page pageInPipeline){
        List<PageItem> pageItemList = new ArrayList<PageItem>();
        for(Page page : pages){
            pageItemList.addAll(page.getPageItems().getItems());
        }

        pageInPipeline.getPageItems().setItems(pageItemList);
    }
}
