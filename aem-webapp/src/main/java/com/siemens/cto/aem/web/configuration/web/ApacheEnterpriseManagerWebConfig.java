package com.siemens.cto.aem.web.configuration.web;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.web.controller.IndexController;
import com.siemens.cto.aem.web.controller.SamlController;
import com.siemens.cto.aem.web.javascript.variable.CompositeJavaScriptVariableSource;
import com.siemens.cto.aem.web.javascript.variable.JavaScriptVariableSource;
import com.siemens.cto.aem.web.javascript.variable.dynamic.ContextPathSource;
import com.siemens.cto.aem.web.javascript.variable.dynamic.LoginStatusSource;
import com.siemens.cto.aem.web.javascript.variable.property.ApplicationPropertySource;
import com.siemens.cto.security.saml.service.SamlIdentityProviderService;
import com.siemens.cto.security.saml.service.impl.SamlIdentityProviderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = {IndexController.class, SamlController.class})
// This scans the package within which IndexController is located (type/compile-safe, as opposed to plain Strings)
public class ApacheEnterpriseManagerWebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private HttpServletRequest request;

    @Bean(name = "samlIdentityProviderService")
    public SamlIdentityProviderService samlIdentityProviderService() {
        return new SamlIdentityProviderServiceImpl();
    }

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

    @Bean(name = "variableSource")
    public JavaScriptVariableSource variableSource() {
        final JavaScriptVariableSource compositeSource = new CompositeJavaScriptVariableSource(applicationPropertySource(),
                                                                                               contextPathSource(),
                                                                                               loginStatusSource());
        return compositeSource;
    }

    @Bean
    JavaScriptVariableSource applicationPropertySource() {
        return new ApplicationPropertySource(ApplicationProperties.getInstance());
    }

    @Bean
    JavaScriptVariableSource contextPathSource() {
        return new ContextPathSource(servletContext);
    }

    @Bean(name = "loginVariableSource")
    public JavaScriptVariableSource loginVariableSource() {
        final JavaScriptVariableSource compositeSource = new CompositeJavaScriptVariableSource(loginStatusSource());
        return compositeSource;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    JavaScriptVariableSource loginStatusSource() {
        return new LoginStatusSource(request);
    }
}
