package com.git.blog.utils;

import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

@Component
public class IPUtils {

    private final RestTemplate restTemplate;

    public IPUtils(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getIpFromRequest(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ip = inet.getHostAddress();
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        //"***.***.***.***".length() = 15
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

    public String  ip2Address(String ip) {
        String url = "https://gz.ipapi.vip/api.php?ip=" + ip;
        URI uri = URI.create(url);
        String commentAddress = restTemplate.getForObject(uri, String.class);
        //从查询返回结果获取地址信息，地址信息由国省市区组成
        JSONObject jsonObject = new JSONObject(commentAddress);
        JSONObject result = jsonObject.getJSONObject("result");
        //不明ip,抛异常
        if (result == null) {
//            throw new RuntimeException("查询不到ip对应地址");
            return "未知地址";
        }
        JSONObject adInfo = result.getJSONObject("ad_info");
        String nation = adInfo.getStr("nation");
        String province = adInfo.getStr("province");
        String city = adInfo.getStr("city");
        String district = adInfo.getStr("district");
        return nation + province + city + district;
    }
}
