package com.munjie.omni.utils;


import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

/**
 * @Date 2023/11/4 16:33
 * @Author mwj
 **/
public class MySqlGenerator {

    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入" + tip + "：");
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    public static void main(String[] args) {
        //全局配置
        GlobalConfig config = new GlobalConfig.Builder()
                //作者
                .author(scanner("请输入作者:"))
                // 生成路径，最好使用绝对路径，window路径是不一样的
                .outputDir("C:\\7758258")
                // 文件覆盖
                .fileOverride()
                //设置时间对应类型
                .dateType(DateType.ONLY_DATE)
                .build();

        //包名策略配置
        PackageConfig packageConfig = new PackageConfig.Builder()
                .parent("com.munjie.blog")
                .mapper("mapper")
                .service("service")
                .controller("controller")
                .entity("entity")
                .xml("mapper")
                .build();

        //策略配置
        StrategyConfig strategyConfig = new StrategyConfig.Builder()
                //设置需要映射的表名
                .addInclude(scanner("请输入表名，多个表以逗号隔开"))
                //策略开启⼤写命名
                .enableCapitalMode()
                .entityBuilder()
                //添加后缀
                .formatFileName("%sEntity")
                //添加lombock的getter、setter注解
                .enableLombok()
                // 数据库表映射到实体的命名策略
                .columnNaming(NamingStrategy.underline_to_camel)
                .naming(NamingStrategy.underline_to_camel)
                .mapperBuilder()//mapper类添加@Mapper
                //生成基本的SQL片段
                .enableBaseColumnList()
                //生成基本的resultMap
                .enableBaseResultMap()
                .serviceBuilder()
                //添加后缀
                .formatServiceFileName("%sService")
                //使用restcontroller注解
                .controllerBuilder().enableRestStyle()
                .build();

        // 数据源配置
        DataSourceConfig.Builder dataSourceConfigBuilder = new DataSourceConfig
                .Builder(
                "jdbc:mysql://124.223.164.197:3306/blog?useSSL=false",
                "mwj",
                "Mwj.2329336");

        // 创建代码生成器对象，加载配置
        AutoGenerator autoGenerator = new AutoGenerator(dataSourceConfigBuilder.build());
        autoGenerator.global(config);
        autoGenerator.packageInfo(packageConfig);
        autoGenerator.strategy(strategyConfig);

        //执行操作
        autoGenerator.execute();
        System.out.println("=======  Done 代码生成完毕  ========");

    }

}
