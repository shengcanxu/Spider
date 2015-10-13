package web.cano.spider.pipeline;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.cano.spider.Page;
import web.cano.spider.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 用于保存资源文件（图片等）
 */
public class SaveResourcePipeline implements Pipeline {
    Logger logger = LoggerFactory.getLogger(getClass());

    private String filePath;

    //是否将不同页面的资源存储在不同的文件夹
    private boolean separateFolder = false;

    //如果存储的文件存在，true就更新，false就算了
    private boolean refreshStoreFile = false;


    private SaveResourcePipeline() {
    }

    public SaveResourcePipeline(String filePath) {
        this.filePath = filePath;

        try {
            FileUtils.forceMkdir(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SaveResourcePipeline setSeparateFolder(boolean separateFolder) {
        this.separateFolder = separateFolder;
        return this;
    }

    public SaveResourcePipeline setRefreshStoreFile(boolean refreshStoreFile) {
        this.refreshStoreFile = refreshStoreFile;
        return this;
    }

    private String getResourceFileName(String url){
        if(url.contains("?")){
            url = url.substring(0,url.indexOf('?'));
        }
        int pos = url.lastIndexOf(".");
        if(url.length() - pos >= 6){
            return DigestUtils.md5Hex(url);
        }else{
            return DigestUtils.md5Hex(url) + url.substring(pos);
        }
    }

    @Override
    public void process(Page page, Task task){
        if (!page.isResource()) return;

        byte[] bytes = page.getResourceBytes();
        String url = page.getUrl();
        String fileName = getResourceFileName(url);

        String path;
        if(separateFolder){
            String folderName = page.getFatherPage().getUrl().replace(".", "").replace("/","").replace(":","").replace("?","").replace("&","").replace("-", "");
            if(folderName.length() >=300){
                folderName = folderName.substring(folderName.length()-300,folderName.length());
            }
            path = filePath + folderName + "/" + fileName;
        }else{
            path = filePath +  fileName;
        }

        try {
            File storeFile = new File(path);
            if(storeFile.exists() && ! refreshStoreFile){
                return;
            }

            FileOutputStream output = FileUtils.openOutputStream(storeFile);
            output.write(bytes);
            output.close();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ;
        }
    }
}
