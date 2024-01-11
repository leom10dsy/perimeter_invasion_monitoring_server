package com.csrd.pims.tools;

import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson.JSONObject;
import com.csrd.pims.bean.huawei.result.HuaweiIvsAlarmEventData;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * xml和json互转
 */
public class XMLUtil {

    /**
     * @param xml xml格式字符串
     * @return 返回fastJson
     */
    public static JSONObject xml2Json(String xml) {
        JSONObject object = new JSONObject();
        SAXBuilder sb = new SAXBuilder();
        Document doc = null;
        try {
            doc = sb.build(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element root = doc.getRootElement();
            object.put(root.getName(), iterateElement(root));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return object;
    }


    /**
     * @param xml 输入流
     * @return 返回fastJson
     */
    public static JSONObject xml2Json(byte[] xml) {
        JSONObject object = new JSONObject();
        SAXBuilder sb = new SAXBuilder();
        Document doc = null;
        try {
            doc = sb.build(new ByteArrayInputStream(xml));
            Element root = doc.getRootElement();
            object.put(root.getName(), iterateElement(root));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return object;
    }


    /**
     * @param bis 输入流
     * @return 返回fastJson
     */
    public static JSONObject xml2Json(ByteArrayInputStream bis) {
        JSONObject object = new JSONObject();
        SAXBuilder sb = new SAXBuilder();
        Document doc = null;
        try {
            doc = sb.build(bis);
            Element root = doc.getRootElement();
            object.put(root.getName(), iterateElement(root));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

        return object;
    }

    /**
     * java 转换成xml
     *
     * @param obj 对象实例
     * @return String xml字符串
     * @Description: 使用xStream解析javaBean，javaBean应使用XStreamAlias注解注释xml节点名称
     */
    public static String beanToXml(Object obj) {
        XStream xstream = new XStream();
        // XStream xstream=new XStream(new DomDriver()); //直接用jaxp dom来解释
        // XStream xstream=new XStream(new DomDriver("utf-8")); //指定编码解析器,直接用jaxp dom来解释
        ////如果没有这句，xml中的根元素会是<包.类名>；或者说：注解根本就没生效，所以的元素名就是类的属性
        xstream.processAnnotations(obj.getClass()); //通过注解方式的，一定要有这句话
        return xstream.toXML(obj);
    }

    /**
     * 将传入xml文本转换成Java对象
     *
     * @param cls    xml对应的class类
     * @return T   xml对应的class类的实例对象
     */
    public static <T> T xmlToBean(String xmlStr, Class<T> cls) {
        XStream xstream = new XStream(new DomDriver());
        // 使用注解解析
        xstream.setClassLoader(cls.getClassLoader());
        xstream.processAnnotations(cls);
        xstream.allowTypesByRegExp(new String[] { ".*" });
        // 放开权限校验
        xstream.addPermission(AnyTypePermission.ANY);
        Object obj = xstream.fromXML(xmlStr);
        return (T)obj;
    }


    /**
     * 判断xml元素，根据阶层整理格式
     */
    private static JSONObject iterateElement(Element element) {
        List<Element> node = element.getChildren();
        Element et = null;
        JSONObject obj = new JSONObject();
        List list = null;
        for (int i = 0; i < node.size(); i++) {
            list = new LinkedList();
            et = node.get(i);
            if (et.getTextTrim().equals("")) {
                // 当此节点为父节点时，其下没有文本,将其子节点加入list
                if (et.getChildren().size() == 0)
                    continue;
                if (obj.containsKey(et.getName())) {
                    list = (List) obj.get(et.getName());
                }
                list.add(iterateElement(et));
                obj.put(et.getName(), list);
            } else {
                // 当此节点为叶子节点时，将内容写入
                obj.put(et.getName(), et.getTextTrim());
            }
        }
        return obj;
    }

    public static void main(String[] args) {
        String xml ="<content><AlarmEventId>42</AlarmEventId><AlarmInCode>06686525464772310101</AlarmInCode><DevDomainCode>178bd2fa9cea4556b7fb5d8f513854aa</DevDomainCode><AlarmInType>ALARM_INTRUSION</AlarmInType><AlarmInName>雷视球机</AlarmInName><AlarmLevelValue>100</AlarmLevelValue><AlarmLevelName></AlarmLevelName><AlarmLevelColor>#ffff0000</AlarmLevelColor><isUserDefind>0</isUserDefind><AlarmType>ALARM_INTRUSION</AlarmType><AlarmTypeName></AlarmTypeName><AlarmCategory>010201</AlarmCategory><OccurTime>20220707113010</OccurTime><OccurNumber>1</OccurNumber><AlarmStatus>0</AlarmStatus><IsCommission>0</IsCommission><FileId></FileId><FileIdEx></FileIdEx><PreviewUrl></PreviewUrl><ExistsRecord>0</ExistsRecord><NVRCode>178bd2fa9cea4556b7fb5d8f513854aa</NVRCode><AlarmDesc>Intrusion alarm</AlarmDesc><ExtParam></ExtParam></content>";
        JSONObject jsonObject = xml2Json(xml);
        HuaweiIvsAlarmEventData c = new HuaweiIvsAlarmEventData();
        c.setAlarmCategory("daf");
        c.setAlarmDesc("aggf");
        c.setAlarmInName("dgscf");
        c.setAlarmInCode("dgsdcf");
        c.setAlarmInType("dgfasdcf");
        String s = beanToXml(c);
        HuaweiIvsAlarmEventData huaweiIvsAlarmEventData = xmlToBean(xml, HuaweiIvsAlarmEventData.class);

        System.out.println(huaweiIvsAlarmEventData);
        System.out.println(s);
    }

}
