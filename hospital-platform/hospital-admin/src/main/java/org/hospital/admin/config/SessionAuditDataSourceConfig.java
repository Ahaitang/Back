package org.hospital.admin.config;

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

/**
 * Session Audit 数据源配置
 * 用于审计日志和黑名单管理
 */
@Configuration
@MapperScan(basePackages = "org.hospital.admin.mapper", sqlSessionTemplateRef = "sessionAuditSqlSessionTemplate")
public class SessionAuditDataSourceConfig {

    @Bean(name = "sessionAuditDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.session-audit")
    public DataSource sessionAuditDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "sessionAuditSqlSessionFactory")
    public SqlSessionFactory sessionAuditSqlSessionFactory(@Qualifier("sessionAuditDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setTypeAliasesPackage("org.hospital.admin.entity");

        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setDbConfig(new GlobalConfig.DbConfig());
        bean.setGlobalConfig(globalConfig);

        return bean.getObject();
    }

    @Bean(name = "sessionAuditSqlSessionTemplate")
    public SqlSessionTemplate sessionAuditSqlSessionTemplate(@Qualifier("sessionAuditSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean(name = "sessionAuditTransactionManager")
    public DataSourceTransactionManager sessionAuditTransactionManager(@Qualifier("sessionAuditDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}