package com.csrd.pims.service;

/**
 * 华为震动光纤服务
 */
public interface HuaweiNceService {

    /**
     * nce登录
     *
     * @param ip       nceIp
     * @param port     ncePort
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    boolean login(String ip, String port, String username, String password);


    /**
     * nce 登出
     *
     * @param ip      nceIp
     * @param port    ncePort
     * @param cookie  cookie
     * @param roaRand 非get请求使用
     * @return nce登出结果
     */
    boolean logout(String ip, String port, String cookie, String roaRand);



    /**
     * 新增nce订阅
     *
     * @param ip      nceIp
     * @param port    ncePort
     * @param cookie  cookie
     * @param roaRand 非get请求使用
     * @return
     */
    boolean addSubscription(String ip, String port, String cookie, String roaRand);

    /**
     * 删除订阅
     *
     * @param ip      nceIp
     * @param port    ncePort
     * @param cookie  cookie
     * @param roaRand 非get请求使用
     * @return 删除订阅结果
     */
    boolean deleteSubscription(String ip, String port, String cookie, String roaRand);


    /**
     * 建立长连接
     */
    void createSseConnection();

}
