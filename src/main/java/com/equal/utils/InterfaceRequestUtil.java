package com.equal.utils;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


@Slf4j
public class InterfaceRequestUtil {


    /**
     * 以get方式调用第三方接口
     * @param url
     * @return
     */
    public static String doGet(String url, String token){

        log.info("doGet请求被调用，接口地址：{}",url);
        String res = "";
        CloseableHttpClient httpClient = null;
        try {
            //get请求，不需要加绕过ssl的代码
            //httpClient = createHttpsClient();
            httpClient  = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(url);
            if (token == null || token.equals("")){
                token = getToken();
            }
            //自定义header头，用于token验证使用
            get.addHeader("token", token);
            get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
            HttpResponse response = httpClient.execute(get);
            log.info("doGet接口返回状态码：{}",response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                //返回json格式
                res = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null){
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return res;
    }

    /**
     * 以post方式调用第三方接口
     * @param url
     * @return
     */
    public static String doPost(String url, String jsonBody, String token){

        String res = "";
        CloseableHttpClient httpClient = null;
        try {
            if (httpClient == null){
                //post请求需要绕过ssl认证
                //httpClient = HttpClientBuilder.create().build();
                httpClient = createHttpsClient();
            }

            HttpPost post = new HttpPost(url);

            if (token == null || token.equals("")){
                token = getToken();
//                token = "XXXX";
            }

            //自定义请求头
            post.addHeader("csrf-token", token);
            post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
            post.addHeader("Content-Type","application/json");
            post.addHeader("groupCode","LCZYEG");
            post.addHeader("projectId","Pj2025122415");

            StringEntity s = new StringEntity(jsonBody);
            s.setContentEncoding("UTF-8");
            //设置请求参数
            post.setEntity(s);
            HttpResponse response = httpClient.execute(post);
            log.info("doPost接口返回状态码：{}",response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                //返回json格式
                res = EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (httpClient != null){
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return res;
    }


    public static CloseableHttpClient createHttpsClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        //4.3版本之前用这个
        //SSLConnectionSocketFactory sslcsf = new SSLConnectionSocketFactory(
        //builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        //4.3版本之后用这个
        SSLConnectionSocketFactory sslcsf = new SSLConnectionSocketFactory(builder.build(),
                new String[] {"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null,new NoopHostnameVerifier());


        Registry<ConnectionSocketFactory> registry = RegistryBuilder.
                <ConnectionSocketFactory> create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslcsf)
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(2000);
        // 整个连接池最大连接数
        // 每路由最大连接数，默认值是2
        cm.setDefaultMaxPerRoute(5);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslcsf)
                .setConnectionManager(cm)
                .build();
        return httpClient;

    };




    /**
     * 获取登陆接口的token
     */
    public static String getToken(){

        log.info("getToken请求被调用");
        String token = "";
        String url = "https://qa.ysbdtp.com/adm-server/auth/account/login?projectId=&groupCode=&userId=";

        JSONObject object = new JSONObject();
        object.put("phone", "15011518953");
        object.put("passWord", "XXXX");
        object.put("loginDeviceType","1");
        object.put("loginType","2");

        CloseableHttpClient httpClient = null;
        try {
            if (httpClient == null){
                httpClient =  createHttpsClient();
                //httpClient = createHttpsClient();
            }
            HttpPost post = new HttpPost(url);

            post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
            post.addHeader("Content-Type","application/json");

            StringEntity s = new StringEntity(object.toString());
            s.setContentEncoding("UTF-8");
            //设置请求参数
            post.setEntity(s);
            HttpResponse response = httpClient.execute(post);
            log.info("getToken接口返回状态码：{}",response.getStatusLine().getStatusCode());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //把HttpResponse转为jsonObject
                JSONObject result = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                if (result.containsKey("data")) {
                    token = result.getString("data");
                    log.info("返回token值：{}",token);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (httpClient != null){
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return token;
    }





}
