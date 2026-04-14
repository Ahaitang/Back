package org.hospital.neuroimmune.config;

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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "org.hospital.neuroimmune.mapper", sqlSessionTemplateRef = "neuroimmuneSqlSessionTemplate")
public class NeuroimmuneDataSourceConfig {

    @Bean(name = "neuroimmuneDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.neuroimmune")
    public DataSource neuroimmuneDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "neuroimmuneSqlSessionFactory")
    public SqlSessionFactory neuroimmuneSqlSessionFactory(@Qualifier("neuroimmuneDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mappers/neuroimmune/*.xml"));
        bean.setTypeAliasesPackage("org.hospital.neuroimmune.entity");

        GlobalConfig globalConfig = new GlobalConfig();
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setTablePrefix("");
        globalConfig.setDbConfig(dbConfig);
        bean.setGlobalConfig(globalConfig);

        return bean.getObject();
    }

    @Bean(name = "neuroimmuneSqlSessionTemplate")
    public SqlSessionTemplate neuroimmuneSqlSessionTemplate(@Qualifier("neuroimmuneSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "neuroimmuneTransactionManager")
    public DataSourceTransactionManager neuroimmuneTransactionManager(@Qualifier("neuroimmuneDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}