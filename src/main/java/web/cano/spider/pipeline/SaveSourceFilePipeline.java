package web.cano.spider.pipeline;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 用于保存所爬取的程序的源文件
 */
public class SaveSourceFilePipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    private String filePath;

    //如果存储的文件存在，true就更新，false就算了
    private boolean refreshStoreFile = false;

    private SaveSourceFilePipeline() {
    }

    public SaveSourceFilePipeline setRefreshStoreFile(boolean refreshStoreFile) {
        this.refreshStoreFile = refreshStoreFile;
        return this;
    }

    public SaveSourceFilePipeline(String filePath) {
        this.filePath = filePath;

        try {
            FileUtils.forceMkdir(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(Page page, Task task) {
        if (page.isResource() || task.getSite().isLocalSite()) return;

        String content = page.getRawText();
        String url = page.getUrl();
        String fileName = url.replace(".","").replace("/","").replace(":","").replace("?","").replace("&","").replace("-", "");
        if(fileName.length() >=300){
            fileName = fileName.substring(fileName.length()-300,fileName.length());
        }

        try {
            File storeFile = new File(filePath + fileName);
            if(storeFile.exists() && !refreshStoreFile){
                return;
            }

            FileOutputStream output = FileUtils.openOutputStream(storeFile);
            output.write(content.getBytes());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ;
        }
    }
}
