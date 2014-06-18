package com.siemens.cto.aem.web.configuration.web;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.siemens.cto.aem.web.controller.IndexController;
import com.siemens.cto.aem.web.controller.SamlController;

@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = {IndexController.class, SamlController.class})
// This scans the package within which IndexController is located (type/compile-safe, as opposed to plain Strings)
public class ApacheEnterpriseManagerWebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false).favorParameter(true).ignoreAcceptHeader(false);
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/public-resources/**").addResourceLocations("/resources/");
        registry.addResourceHandler("/gen-public-resources/**").addResourceLocations("/gen/resources/");
    }

    @Bean
    public ViewResolver viewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Bean
    public MessageSource messageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("/messages/aem/ApacheEnterpriseManager");
        return messageSource;
    }

    @Bean
    LocaleResolver localeResolver() {
        final AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        return resolver;
    }
}
