package com.nverrbug.wxjizan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Title: BootTestMain
 * Description: 用于单元测试 </p>
 * Email is Wenbo.Xie@b-and-qchina.com<
 * Company: http://www.bthome.com
 *
 * @author xie.wenbo
 * @date 2019-04-02 09:32
 */
@EnableSwagger2
@SpringBootApplication
public class WxJiZanApplication {
    public static void main(String[] args) {
        SpringApplication.run(WxJiZanApplication.class, args);
    }
}
