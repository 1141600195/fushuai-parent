/**
 * Copyright (C), 2015-2019, XXX有限公司
 * FileName: SwaggerConfig
 * Author:   康鸿
 * Date:     2019/8/13 7:35
 * Description: TODO
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.manager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

/**
 * Description: TODO
 *
 * @author 康鸿
 * @create 2019/8/13
 * @since 1.0.0
 * Description 
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Autowired
    private Environment environment;

    @Bean
    public Docket getDocket2(){

        Docket docket=new Docket(DocumentationType.SWAGGER_2);

        //配置接口的过滤start
        docket.select()
                .apis(RequestHandlerSelectors.basePackage("com.manager.web"))//根据报名匹配接口的展示
                .paths(PathSelectors.ant("/**"))//根据路径的正则去匹配
                .build();

        //根据包名配置接口的过滤end

        //配置忽略参数-start
        //docket.ignoredParameterTypes(String.class,UserInfo.class);
        //配置忽略参数-end

        //配置动态的显示接口文档start 大家注意一下SpringBoot的版本需要时2.1.0以后的版本
//        Profiles of = Profiles.of("dev", "test" );
////        boolean b = environment.acceptsProfiles(of);
////        docket.enable(b);
        //配置动态的显示接口文档end

        //配置API的分组
        docket.groupName("manager");

        docket.apiInfo(setApiInfo());
        return docket;
    }


    public ApiInfo setApiInfo(){

        Contact contact=new Contact("kh","http://www.baidu.com","1141600195@qq.com");
        ApiInfo apiInfo=new ApiInfo(
                "这是文档的标题",
                "这是文档信息的描述信息",
                "v-1.0",
                "http://www.baidu.com",
                contact,"监听信息",
                "http://www.baidu.com",
                new ArrayList<VendorExtension>()
        );

        return apiInfo;
    }

}