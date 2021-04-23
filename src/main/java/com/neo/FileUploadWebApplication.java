package com.neo;

import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author Anonymous
 */
@SpringBootApplication
@MapperScan("com.neo.mapper")
public class FileUploadWebApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(FileUploadWebApplication.class, args);
    }

    /**
     * 配置TomCat
     *
     * @return TomCat
     */
    @Bean
    public TomcatServletWebServerFactory tomcatEmbedded() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(connector -> {
            if ((connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?>)) {
                //-1 means unlimited
                ((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSwallowSize(-1);
            }
        });
        return tomcat;
    }

}