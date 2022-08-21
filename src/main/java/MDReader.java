import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class MDReader {
    static int cnt = 79;
    public static void main(String[] args) {
        String name = "Java-IO.md";
//        try {
//            reader("C:\\Users\\蔡艺\\Downloads\\evernote2md_0.17.1_Windows_64-bit\\notes\\"+name
//            ,"C:\\Users\\蔡艺\\Downloads\\evernote2md_0.17.1_Windows_64-bit\\notes\\image"
//            ,"C:\\Users\\蔡艺\\IdeaProjects\\mdreader\\src\\"+name);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
         log.debug("success");
    }
    public  static void reader(String filePath,String imagePath,String out) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        BufferedWriter writer = new BufferedWriter(new FileWriter(out));
        String str = "";
        while((str = reader.readLine())!=null){
            int ix = str.indexOf("![");
            if(ix == -1){
              //  System.out.println(str);
                writer.write(str);
                writer.newLine();
                writer.flush();
                continue;
            }
            System.out.println(str);
            String imageName = str.substring(ix+2,str.indexOf("]"));
            String accessUrl = uploadOSS(imagePath+"//"+imageName,imageName);
            if(accessUrl == null) break;
            str = str.substring(0,str.indexOf("](")+2) + accessUrl + str.substring(str.lastIndexOf(")"));
            System.out.println(str);
            System.out.println();
            writer.write(str);
            writer.newLine();
            writer.flush();
        }
    }
    public static String uploadOSS(String imagePath,String imageName){
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "https://oss-cn-beijing.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5t7rReLmcoPk6ktVGHKM";
        String accessKeySecret = "uSgYngW8HfxfimNNP5zqPyGzcxK7BB";
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "sssjody";
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = "img"+cnt+"/"+imageName;

        String accessUrlHead = "https://sssjody.oss-cn-beijing.aliyuncs.com/";
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, new File(imagePath));
            // 上传文件。
            ossClient.putObject(putObjectRequest);
            return accessUrlHead+objectName;
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
        } catch (ClientException ce) {
            ce.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
