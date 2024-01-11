package com.csrd.pims.tools;

import com.csrd.pims.config.huawei.HuaweiRadarConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * 获取spring容器中类和参数
 */
@Slf4j
@Component
public class ApplicationContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static Object getObject(String id) {
        Object object = null;
        object = context.getBean(id);
        return object;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (context == null) {
            log.info("applicationContext is null");
        } else {
            return context.getBean(clazz);
        }
        return null;
    }

    public static String getProperty(String name) {
        Environment environment = ApplicationContextUtil.getBean(Environment.class);
        return environment.getProperty(name);
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * 修改yml文件。暂时未启用
     */
    private static void updateYaml(String name, String data) {
        try {
            URL url = HuaweiRadarConfig.class.getClassLoader().getResource("application.yml");
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
            dumperOptions.setPrettyFlow(false);
            Yaml yaml = new Yaml(dumperOptions);
            Map map = yaml.load(new FileInputStream(url.getFile()));
            Object o = map.get(name);
            log.info("=====> 这是修改前：" + o);
            map.put(name, data);
            yaml.dump(map, new OutputStreamWriter(new FileOutputStream(url.getFile())));
            log.info("=====> 这是修改后：" + map.get(name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跟新容器中的bean
     *
     * @param updateBeanName 需要更新的beanName
     * @param obj            bean中的javaBean属性值
     * @param fileName       bean中的其他类型属性名称
     * @param data           bean中的其他类型属性值
     */
    public static boolean updateBean(String updateBeanName, Object obj, String fileName, String data) {
        ApplicationContext applicationContext = ApplicationContextUtil.getApplicationContext();
        String[] beans = applicationContext.getBeanDefinitionNames();
        for (String beanName : beans) {
            // 拿到bean的Class对象
            Class<?> beanType = applicationContext.getType(beanName);
            if (beanType == null) {
                continue;
            }
            // 拿到当前bean类型的所有字段
            Field[] declaredFields = beanType.getDeclaredFields();
            if (!beanName.contains(updateBeanName)) {
                continue;
            }
            for (Field field : declaredFields) {
                // 从spring容器中拿到这个具体的bean对象
                Object bean = applicationContext.getBean(beanName);
                // 当前字段设置新的值
                try {
                    if (obj != null && field.getType().equals(obj.getClass())) {
                        field.setAccessible(true);
                        field.set(bean, obj);
                    }

                    if (field.getName().equals(fileName)) {
                        setFieldData(field, bean, data);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    private static void setFieldData(Field field, Object bean, String data) throws Exception {
        // 注意这里要设置权限为true
        field.setAccessible(true);
        Class<?> type = field.getType();
        if (type.equals(String.class)) {
            field.set(bean, data);
        } else if (type.equals(Integer.class)) {
            field.set(bean, Integer.valueOf(data));
        } else if (type.equals(Long.class)) {
            field.set(bean, Long.valueOf(data));
        } else if (type.equals(Double.class)) {
            field.set(bean, Double.valueOf(data));
        } else if (type.equals(Short.class)) {
            field.set(bean, Short.valueOf(data));
        } else if (type.equals(Byte.class)) {
            field.set(bean, Byte.valueOf(data));
        } else if (type.equals(Boolean.class)) {
            field.set(bean, Boolean.valueOf(data));
        } else if (type.equals(Date.class)) {
            field.set(bean, new Date(Long.parseLong(data)));
        }
    }

}
