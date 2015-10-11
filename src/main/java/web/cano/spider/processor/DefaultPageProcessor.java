package web.cano.spider.processor;

import web.cano.spider.Page;
import web.cano.spider.PageItem;
import web.cano.spider.selector.*;

import java.util.List;

/**
 * Created by cano on 2015/9/13.
 */
public abstract class DefaultPageProcessor implements  PageProcessor{

    public static enum PageProcessType {XPath,Regex,JsonPath,Css}

    //判断page的url是否符合urlRegex规范
    protected boolean isPageMatchByUrl(Page page, String urlRegex){
        return new PlainText(page.getUrl()).regex(urlRegex).match();
    }

    //获取下一步要爬取的urls
    protected int parseUrls(Page page, String pattern, PageProcessType type ){
        Selector selector = getSelector(pattern,type);
        List<String> urls = page.getHtml().selectDocumentForList(selector);
        page.addTargetPages(urls);
        page.setSkip(true);
        return urls.size();
    }

    //在分页中获取下一步要爬取的urls
    protected int parseNextUrls(Page page, String pattern, PageProcessType type ){
        Selector selector = getSelector(pattern,type);
        List<String> urls = page.getHtml().selectDocumentForList(selector);
        page.addNextPages(urls);
        page.setSkip(true);
        return urls.size();
    }

    protected void putItem(Page page, PageItem item){
        if(item != null && page != null) {
            page.getPageItems().getItems().add(item);
        }
    }

    //从url中获得内容
    protected PageItem extractByUrl(Page page, String regex,PageItem item){
        RegexSelector selector = new RegexSelector(regex);
        String url = selector.select(page.getUrl().toString());
        item.setItemValue(url);
        return item;
    }

    //爬取一个字段
    protected PageItem extractBy(Page page, String pattern, PageProcessType type, PageItem item){
        if(item == null) return null;

        Selector selector = getSelector(pattern,type);
        if(item.isMultiple()){
            String separator = this.getSite().getMultiValueSeparator();
            List<String> values = page.getHtml().selectDocumentForList(selector);
            if(values == null || values.size() ==0){
                return null;
            }
            item.setItemValue(values);
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

    protected PageItem formatValue(PageItem item, String regex){
        if(item.getItemType() == PageItem.PageItemType.STRING) { //只是format string
            if(item.isMultiple()){
                List<String> list = (List<String>) item.getItemValue();
                for(int i=0; i<list.size(); i++){
                    String str = new PlainText(list.get(i)).regex(regex).toString();
                    list.set(i,str);
                }
                item.setItemValue(list);
            }else {
                String value = item.getItemValue().toString();
                value = new PlainText(value).regex(regex).toString();
                item.setItemValue(value);
            }
        }
        return item;
    }

}
