package web.cano.spider.processor;

import web.cano.spider.Page;
import web.cano.spider.selector.*;

import java.util.List;

/**
 * Created by cano on 2015/9/13.
 */
public abstract class DefaultPageProcessor implements  PageProcessor{

    public static enum PageProcessType {XPath,Regex,JsonPath,Css}

    //从url中获得内容
    protected int extractByUrl(Page page, String regex){
        RegexSelector selector = new RegexSelector(regex);
        String url = selector.select(page.getUrl().toString());
        page.addTargetPage(url);
        return 1;
    }

    //判断page的url是否符合urlRegex规范
    protected boolean isPageMatchByUrl(Page page, String urlRegex){
        return new PlainText(page.getUrl()).regex(urlRegex).match();
    }

    //获取下一步要爬取的urls
    protected int parseUrls(Page page, String pattern, PageProcessType type ){
        Selector selector = getSelector(pattern,type);
        List<String> urls = page.getHtml().selectDocumentForList(selector);
        page.addTargetPages(urls);
        return urls.size();
    }

    protected void putItem(Page page, PageProcessorItem item){
        if(item != null && page != null) {
            page.putField(item.getItemName(), item.getItemType());
            page.putItem(item.getItemName(), item.getItemValue());
        }
    }

    //爬取一个字段
    protected PageProcessorItem extratBy(Page page, String pattern, PageProcessType type,PageProcessorItem item){
        if(item == null) return null;

        Selector selector = getSelector(pattern,type);
        if(item.isMultiple()){
            String separator = this.getSite().getMultiValueSeparator();
            List<String> values = page.getHtml().selectDocumentForList(selector);
            if(values == null || values.size() ==0){
                return null;
            }
            String v = values.get(0);
            for(int i=1; i<values.size();i++){
                v = v + separator + values;
            }
            item.setItemValue(v);
        }else{
            String value = page.getHtml().selectDocument(selector);
            item.setItemValue(value);
        }

        return item;
    }

    private Selector getSelector(String pattern, PageProcessType type){
        Selector selector;
        switch(type){
            case XPath:
                type = PageProcessType.XPath;
                selector = new XpathSelector(pattern);
                break;
            case Regex:
                type = PageProcessType.Regex;
                selector = new RegexSelector(pattern);
                break;
            case JsonPath:
                type = PageProcessType.JsonPath;
                selector = new JsonPathSelector(pattern);
                break;
            case Css:
                type = PageProcessType.Css;
                selector = new CssSelector(pattern);
                break;
            default:
                type = PageProcessType.XPath;
                selector = new XpathSelector(pattern);
        }
        return selector;
    }

    protected PageProcessorItem formatValue(PageProcessorItem item, String regex){
        String value = item.getItemValue();
        value = new PlainText(value).regex(regex).toString();
        item.setItemValue(value);
        return item;
    }

}
