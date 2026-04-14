package org.hospital.qmg.config;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "org.hospital.qmg.mapper", sqlSessionTemplateRef = "qmgSqlSessionTemplate")
public class QmgDataSourceConfig {

    @Bean(name = "qmgDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.qmg")
    @Primary
    public DataSource qmgDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "qmgSqlSessionFactory")
    @Primary
    public SqlSessionFactory qmgSqlSessionFactory(@Qualifier("qmgDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mappers/qmg/*.xml"));
        bean.setTypeAliasesPackage("org.hospital.qmg.entity");

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setDbConfig(new GlobalConfig.DbConfig());
        bean.setGlobalConfig(globalConfig);

        return bean.getObject();
    }

    @Bean(name = "qmgSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate qmgSqlSessionTemplate(@Qualifier("qmgSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "qmgTransactionManager")
    @Primary
    public DataSourceTransactionManager qmgTransactionManager(@Qualifier("qmgDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}