package com.civilizer.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

public class ResourceHttpRequestHandlerReplacer
    implements BeanFactoryPostProcessor {
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
            throws BeansException {

        String[] names = factory.getBeanNamesForType(ResourceHttpRequestHandler.class);

        for (String name: names) {
            BeanDefinition bd = factory.getBeanDefinition(name);
            bd.setBeanClassName("com.civilizer.web.handler.ResourceHttpRequestHandler");
        }            
    }       

}
