package Utils;

import org.junit.Test;
import web.cano.spider.utils.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by cano on 2015/10/9.
 */
public class FileHelperTest {

    @Test
    public void testReadUrlsFromFile(){
        File file = new File( this.getClass().getClassLoader().getResource("fileHelperTest0.txt").getFile());
        List<String>  list = FileHelper.readUrlsFromFile(file);

        assertThat(list.size()).isEqualTo(10);
        assertThat(list.get(0)).isEqualTo("http://www.douguo.com/cookbook/178440.html");
        assertThat(list.get(9)).isEqualTo("http://www.douguo.com/cookbook/771172.html");
    }

    @Test
    public void testWriteUrlsToFile(){
        List<String> list = new ArrayList<String>();
        list.add("test1");
        list.add("test2");
        File file = new File("d:/test.txt");
        FileHelper.writeUrlsToFile(list,file);
        List<String> inList = FileHelper.readUrlsFromFile(file);

        assertThat(inList.size()).isEqualTo(2);
        assertThat(inList.get(0)).isEqualTo("test1");
        assertThat(inList.get(1)).isEqualTo("test2");
        file.delete();
    }

    @Test
    public void testRemoveDuplicateandSort(){
        List<String> list = new ArrayList<String>();
        list.add("http://www.douguo.com/cookbook/771172.html");
        list.add("http://www.douguo.com/cookbook/771172.html");
        list.add("http://www.douguo.com/cookbook/372132.html");
        File file = new File("d:/test.txt");
        FileHelper.writeUrlsToFile(list, file);
        FileHelper.removeDuplicate(file);
        FileHelper.sortFile(file);
        List<String> inList = FileHelper.readUrlsFromFile(file);

        assertThat(inList.size()).isEqualTo(2);
        assertThat(inList.get(0)).isEqualTo("http://www.douguo.com/cookbook/372132.html");
        assertThat(inList.get(1)).isEqualTo("http://www.douguo.com/cookbook/771172.html");
        file.delete();
    }

    @Test
    public void testDiffFile(){
        File file0 = new File( this.getClass().getClassLoader().getResource("fileHelperTest0.txt").getFile());
        File file1 = new File( this.getClass().getClassLoader().getResource("fileHelperTest1.txt").getFile());
        List<String>[] listArray = FileHelper.diffFile(file0,file1);

        assertThat(listArray.length).isEqualTo(2);
        assertThat(listArray[0].size()).isEqualTo(1);
        assertThat(listArray[1].size()).isEqualTo(1);
        assertThat(listArray[0].get(0)).isEqualTo("http://www.douguo.com/cookbook/709905.html");
        assertThat(listArray[1].get(0)).isEqualTo("http://www.douguo.com/cookbook/709900.html");

    }
}
