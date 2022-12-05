package top.yyyhn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("top.yyyhn.mapper")
public class AmazonCrawlApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmazonCrawlApplication.class, args);
    }

}
