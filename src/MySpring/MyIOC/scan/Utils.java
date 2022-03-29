package MySpring.MyIOC.scan;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * @MethodName:
 * @Description: 工具类
 * @Author: zzy
 * @Date: 2022/3/29 21:03
 * @Param:
 * @Return:
 */
class Utils {
    /**
     * @MethodName: ScanBeforeList
     * @Description: 扫描配置文件
     * @Author: zzy
     * @Date: 2022/3/29 15:01
     * @Param: [classPath]
     * @Return: org.w3c.dom.NodeList
     */
    static NodeList scanBefore(String classPath) throws Exception {
        InputStream inputStream = new FileInputStream(classPath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputStream);
        Element root = doc.getDocumentElement();
        return root.getChildNodes();
    }

    /**
     * @MethodName: getFileFromPath
     * @Description: 由包路径获取到文件
     * @Author: zzy
     * @Date: 2022/3/29 15:07
     * @Param: [packagePath]
     * @Return: java.io.File
     */
    static File getFileFromPath(String packagePath, ClassLoader classLoader){
        packagePath = packagePath.replace(".", "/");
        URL resource = classLoader.getResource(packagePath);
        assert resource != null;
        String f1 = resource.getFile();
        return new File(f1);
    }
}
