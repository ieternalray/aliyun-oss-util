
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Eternalray
 * Oss配置信息读取类
 */
@Configuration
public class OssProperties implements InitializingBean {
    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.accessKeyId}")
    private String accessKeyId;
    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${oss.bucketName}")
    private String bucketName;

    public static String ENDPOINT;
    public static String ACCESSKEYID;
    public static String ACCESSKEYSECRET;
    public static String BUCKETNAME;

    @Override
    public void afterPropertiesSet(){
        ENDPOINT=this.endpoint;
        ACCESSKEYID=this.accessKeyId;
        ACCESSKEYSECRET=this.accessKeySecret;
        BUCKETNAME =this.bucketName;
    }
}
