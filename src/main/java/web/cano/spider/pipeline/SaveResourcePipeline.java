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

    private boolean separateFolder = false;

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

    @Override
    public void process(Page page, Task task) {
        if (!page.isResource()) return;

        byte[] bytes = page.getResourceBytes();
        String url = page.getUrl();
        String fileName = DigestUtils.md5Hex(url) +  url.substring(url.lastIndexOf("."));

        String path;
        if(separateFolder){
            path = filePath +  fileName;
        }else{
            String folderName = page.getUrl().replace(".", "").replace("/","").replace(":","").replace("?","").replace("&","").replace("-", "");
            if(folderName.length() >=300){
                folderName = folderName.substring(folderName.length()-300,folderName.length());
            }
            path = filePath + folderName + "/" + fileName;
        }

        try {
            File storeFile = new File(path);
            FileOutputStream output = FileUtils.openOutputStream(storeFile);
            output.write(bytes);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ;
        }
    }
}
