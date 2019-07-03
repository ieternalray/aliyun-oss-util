import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.eternalray.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Eternalray
 * Oss工具包
 */
@Slf4j
public class OssUtils {

    private static String endpoint=OssProperties.ENDPOINT;
    private static String accessKeyId=OssProperties.ACCESSKEYID;
    private static String accessKeySecret=OssProperties.ACCESSKEYSECRET;
    private static String bucketName=OssProperties.BUCKETNAME;

    /**
     * 上传file文件
     * @param multipartFile 文件
     * @param saveFolder 文件夹名称(如果文件夹名称为空，则文件保存在该bucket根目录下)
     * @param saveName  保存到OSS自定义名称(如果自定义名称为空，则生成UUID为文件名称)
     * @param isImage   文件是否为图片，如果为图片返回一个带时效的图片预览URL
     * @return
     * @Author: Eternalray
     */
    public static Object uploadFile(MultipartFile multipartFile,String saveFolder,String saveName,boolean isImage){
        String key=saveFolder;
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try{
            String fileName=multipartFile.getOriginalFilename();
            String extensionName = CustomUtils.getExtensionName(fileName);
            saveName=StringUtils.isNotBlank(saveName)?saveName:CustomUtils.getUUID();
            key+=StringUtils.isBlank(key)?"":"/";
            key+=saveName+"."+extensionName;
            client.putObject(new PutObjectRequest(bucketName, key, multipartFile.getInputStream()));
            Map<String,String> resultMap=new HashMap<>(16);
            resultMap.put("bucketName",bucketName);
            resultMap.put("key",key);
            if(isImage){
                // 设置URL过期时间为10年  3600l* 1000*24*365*10
                Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
                // 生成URL
                URL imgUrl = client.generatePresignedUrl(bucketName, key, expiration);
                if(imgUrl!=null){
                    resultMap.put("imgUrl",imgUrl.toString());
                }
            }
            return Result.set(true,resultMap);
        }catch (Exception e){
            log.error("OSS文件上传失败！",e);
        }finally {
            client.shutdown();
        }
        return Result.set(false,"OSS文件上传失败！");
    }

    /**
     * 复制文件
     * @param key
     * @param copyToKey
     * @return
	 * @Author: Eternalray
     */
    public static Object copyFile(String key,String copyToKey){
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try{
            client.copyObject(bucketName,key,bucketName,copyToKey);
            return Result.set(true,"OSS文件复制成功！");
        }catch (Exception e){
            log.error("OSS删除失败！",e);
        }finally {
            client.shutdown();
        }
        return Result.set(false,"OSS文件复制失败！");
    }
    /**
     * 删除文件对象
     * @param key folder or file  name
     * @return
     * @Author: Eternalray
     */
    public static Object deleteFile(String key){
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try{
            client.deleteObject(bucketName,key);
            return Result.set(true,"OSS文件删除成功！");
        }catch (Exception e){
            log.error("OSS删除失败！",e);
        }finally {
            client.shutdown();
        }
        return Result.set(false,"OSS文件删除失败！");
    }

    /**
     * 单文件下载
     * @param key 文件完整路径
     * @param response
     * @Author: Eternalray
     */
    public static void downloadFile(String key, HttpServletResponse response){
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try{
            OSSObject object = client.getObject(bucketName,key);
            String[] keys = key.split("\\/");
            //通知浏览器以附件形式下载
            response.setHeader("Content-Disposition","attachment;filename="+URLEncoder.encode(keys[keys.length-1],"utf-8"));
            response.setHeader("Content-Type", object.getObjectMetadata().getContentType());
            BufferedInputStream in=new BufferedInputStream(object.getObjectContent());
            BufferedOutputStream out=new BufferedOutputStream(response.getOutputStream());
            byte[] car=new byte[1024];
            int L=0;
            while((L=in.read(car))!=-1){
                out.write(car, 0,L);
            }
            if(out!=null){
                out.flush();
                out.close();
            }
            if(in!=null){
                in.close();
            }
        }catch (Exception e){
            log.error("文件无法下载！",e);
        }finally {
            client.shutdown();
        }
    }
}
