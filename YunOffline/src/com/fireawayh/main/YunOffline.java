package com.fireawayh.main;

import com.fireawayh.util.IOUtils;
import com.fireawayh.util.ShowGUI;
import com.fireawayh.util.ShowImg;
import com.oracle.javafx.jmx.json.JSONDocument;
import com.oracle.javafx.jmx.json.JSONFactory;
import com.oracle.javafx.jmx.json.JSONReader;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Fei: 07471264096

/**
 */

//https://passport.baidu.com/v2/api/?getapi&tpl=mn&apiver=v3&tt=1453851410376&class=login&gid=46392DC-171D-4213-A522-BF70BC214F9C&logintype=dialogLogin&callback=bd__cbs__k3wtgt

public class YunOffline implements Runnable {
    private static final String TOKEN_GET_URL = "https://passport.baidu.com/v2/api/?getapi&tpl=mn&apiver=v3&tt=1453851410376&class=login&gid=46392DC-171D-4213-A522-BF70BC214F9C&logintype=dialogLogin&callback=bd__cbs__k3wtgt";
    private static final String LOGIN_POST_URL = "https://passport.baidu.com/v2/api/?login";
    private static final String HEADER_HOST = "passport.baidu.com";
    private static final String HEADER_ORIGIN = "https://www.baidu.com";
    private static final String HEADER_CONNECTION = "keep-alive";
    private static final String HEADER_ACCEPT = "*/*";
    private static final String HEADER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
    private static final String HEADER_REFERER = "https://www.baidu.com/";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static ArrayList<String> argsList = new ArrayList<>();
    private static IOUtils iou = new IOUtils();
    private SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(SSLContexts.createDefault(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    private String email;
    private String password;
    private String token;
    private String panToken = "";
    private String source_url;
    private String newVcode;
    private String newInput;
    private String savepath;
    private boolean loginVcode = false;
    private boolean newThread = false;
    private BasicCookieStore cookieStore = new BasicCookieStore();

    public String getSavepath() {
        return savepath;
    }

    public void setSavepath(String savepath) {
        this.savepath = savepath;
    }

    public String getPanToken() {
        return this.panToken;
    }

    public void setPanToken(String panToken) {
        this.panToken = panToken;
    }

    public YunOffline(String email, String password) {
        this.email = email;
        this.password = password;
    }


    public YunOffline(String email, String password, String source_url, boolean newThread) {
        this.email = email;
        this.password = password;
        this.source_url = source_url;
        this.newThread = newThread;
    }

    public YunOffline(String panToken, String source_url, String newVcode, String newInput) {
        this.panToken = panToken;
        this.source_url = source_url;
        this.newVcode = newVcode;
        this.newInput = newInput;
    }

    public void getCloudInfo() throws Exception {
        URL u = new URL("https://pcs.baidu.com/rest/2.0/pcs/quota?method=info&access_token=" + token);
        URLConnection conn = u.openConnection();// 打开网页链接
        // 获取用户云盘信息
    }

    public static void main(String[] args) {
//		set your email and password
//		YunOffline B = new YunOffline("hejiheji001@163.com", "MyLifeForFire0");
//		B.initYunPan();
        argsList = new ArrayList<>(Arrays.asList(args));
        if (argsList.isEmpty()) {
            new ShowGUI().showGui();
        } else {
            switch (args[0]) {
                case "-cli":
                    String userName = args[1];
                    String password = args[2];
                    YunOffline BD = new YunOffline(userName, password);
                    BD.setSource_url(args[3]);
                    if(BD.initYunPan()){
                        BD.getYunPanToken();
                        BD.saveToYunPan("", "");
                    }
                    break;
                case "-conf":
                    try {
                        Properties prop = new Properties();
                        File file = new File(args[1]);
                        if (file.exists()) {
                            prop.load(new FileInputStream(file));
                            YunOffline BD2 = new YunOffline(prop.getProperty("Username"), prop.getProperty("Password"));
                            BD2.setSource_url(args[2]);
                            BD2.setSavepath(prop.getProperty("Savepath"));
                            if(BD2.initYunPan()){
                                BD2.getYunPanToken();
                                BD2.saveToYunPan("", "");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "-h":
                    break;
            }
        }
    }

    public void setSource_url(String source_url) {
        this.source_url = source_url;
    }

    public boolean initYunPan(){
        boolean result = false;
        System.err.println("正在验证登录状态.../Checking Login Status...");
        CloseableHttpClient loginClient = getClient("init");
        if (checkLogin(loginClient)) {
            System.err.println("无需登录!/Already Logged In!");
            result = true;
        } else {
            System.err.println("正在登录百度.../Login Now...");
            preLogin(loginClient);
            initToken(loginClient);

            HttpPost post = new HttpPost(LOGIN_POST_URL);
            post.setEntity(produceFormEntity());
            HttpResponse response = null;
            try {
                response = loginClient.execute(post);
                String content = EntityUtils.toString(response.getEntity());
                System.out.print(content);
                if (content.contains("err_no=257") || content.contains("err_no=6")) {
                    Pattern pattern = Pattern.compile("codeString=(.*?)&");
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        this.newVcode = matcher.group(1);
                        String img = "http://passport.baidu.com/cgi-bin/genimage?" + newVcode;
                        newInput = getInput(img);
                        loginVcode = true;
                        System.err.println("使用验证码重新登录.../Login Again With Verify Code...");
                        initYunPan();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if (checkLogin(loginClient)) {
                saveCookie();
                System.err.println("登陆成功!/Login Success!");
                result = true;
            } else {
                System.err.println("登陆百度失败!/Login Failed!");
            }
        }

        return result;
    }

    public void saveCookie() {
        if (!cookieStore.getCookies().isEmpty()) {
            if(iou.saveCookie(cookieStore.getCookies())) {
                System.out.println("Save Cookie To File");
            }else{
                System.err.println("Save Cookie Failed");
            }
        } else {
            System.err.println("No Cookies");
        }
    }

    private BasicCookieStore getCookie() {
        if (cookieStore.getCookies().isEmpty()) {
            Cookie[] cookies = iou.getCookie("cookie.file");
            if (cookies != null) {
                cookieStore.addCookies(cookies);
                System.out.println("Get Cookie From File");
            }else{
                System.err.println("Cookie File Is Empty");
            }
        }
        return cookieStore;
    }

    public CloseableHttpClient getClient(String type) {
        List<Header> list = new LinkedList<>();
        switch (type) {
            case "init":
                list.add(new BasicHeader("Host", HEADER_HOST));
                list.add(new BasicHeader("Connection", HEADER_CONNECTION));
                list.add(new BasicHeader("Accept", HEADER_ACCEPT));
                list.add(new BasicHeader("Origin", HEADER_ORIGIN));
                list.add(new BasicHeader("User-Agent", HEADER_USER_AGENT));
                list.add(new BasicHeader("Referer", HEADER_REFERER));
                list.add(new BasicHeader("Accept-Encoding", HEADER_ACCEPT_ENCODING));
                list.add(new BasicHeader("Accept-Language", HEADER_ACCEPT_LANGUAGE));
                break;
            case "pan":
                list.add(new BasicHeader("Host", "pan.baidu.com"));
                list.add(new BasicHeader("Connection", HEADER_CONNECTION));
                list.add(new BasicHeader("Accept", HEADER_ACCEPT));
                list.add(new BasicHeader("Origin", "http://pan.baidu.com"));
                list.add(new BasicHeader("User-Agent", HEADER_USER_AGENT));
                list.add(new BasicHeader("Referer", "http://pan.baidu.com/disk/home"));
                list.add(new BasicHeader("Accept-Encoding", HEADER_ACCEPT_ENCODING));
                list.add(new BasicHeader("Accept-Language", HEADER_ACCEPT_LANGUAGE));
                break;
        }
        return HttpClientBuilder.create().setDefaultCookieStore(getCookie()).setDefaultHeaders(list).setSSLSocketFactory(sslFactory).build();
    }

    private void initToken(CloseableHttpClient loginClient) {
        System.out.println("正在设置token参数.../Getting Login Token...");
        HttpGet get = new HttpGet(TOKEN_GET_URL);
        HttpContext context1 = new BasicHttpContext();
        HttpResponse response = null;
        try {
            response = loginClient.execute(get, context1);
            String str = EntityUtils.toString(response.getEntity());
            Pattern pattern = Pattern.compile("token\" : \"(.*?)\"");
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                token = matcher.group(1);
            }
            iou.saveStringToFile("logintoken=" + token, "logintoken.properties");
            System.out.println("token ： " + token);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                EntityUtils.consume(response.getEntity());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private UrlEncodedFormEntity produceFormEntity() {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("tt", "" + System.currentTimeMillis()));
        parameters.add(new BasicNameValuePair("staticpage", "http://www.baidu.com/cache/user/html/v3Jump.html"));
        parameters.add(new BasicNameValuePair("charset", "utf-8"));
        parameters.add(new BasicNameValuePair("token", token));
        parameters.add(new BasicNameValuePair("tpl", "mn"));
        parameters.add(new BasicNameValuePair("subpro", ""));
        parameters.add(new BasicNameValuePair("apiver", "v3"));
        parameters.add(new BasicNameValuePair("codestring", newVcode));
        parameters.add(new BasicNameValuePair("safeflg", "0"));
        parameters.add(new BasicNameValuePair("u", "https://www.baidu.com/"));
        parameters.add(new BasicNameValuePair("isPhone", "false"));
        parameters.add(new BasicNameValuePair("detect", "1"));
        parameters.add(new BasicNameValuePair("quick_user", "0"));
        parameters.add(new BasicNameValuePair("gid", "46392DC-171D-4213-A522-BF70BC214F9C"));
        parameters.add(new BasicNameValuePair("loginmerge", "true"));
        parameters.add(new BasicNameValuePair("logintype", "dailoglogin"));
        parameters.add(new BasicNameValuePair("logLoginType", "pc_loginDialog"));
        parameters.add(new BasicNameValuePair("idc", ""));
        parameters.add(new BasicNameValuePair("splogin", "rate"));
        parameters.add(new BasicNameValuePair("username", email));


//        try {
//            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//            kpg.initialize(2048);
//            KeyPair kp = kpg.genKeyPair();
//            Key publicKey = kp.getPublic();
//            Key privateKey = kp.getPrivate();
//
//            KeyFactory fact = KeyFactory.getInstance("RSA");
//            RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
//            RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        pubkey, rsakey = self._get_publickey()
//        key = rsa.PublicKey.load_pkcs1_openssl_pem(pubkey)
//        password_rsaed = base64.b64encode(rsa.encrypt(self.password, key))

        parameters.add(new BasicNameValuePair("password", password));
        parameters.add(new BasicNameValuePair("men_pass", "on"));
        parameters.add(new BasicNameValuePair("callback", "parent.bd__pcbs__dhfbkl"));
        parameters.add(new BasicNameValuePair("verifycode", newInput));
        parameters.add(new BasicNameValuePair("crypttype", "12"));
//        parameters.add(new BasicNameValuePair("rsakey", "VRcgNIEQBhibpVfatBvwRcd6tyTuT1CV"));
        UrlEncodedFormEntity encodedFormEntity;
        try {
            encodedFormEntity = new UrlEncodedFormEntity(parameters, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return encodedFormEntity;
    }

    public boolean checkLogin(CloseableHttpClient loginClient) {
        HttpGet get = new HttpGet("http://www.baidu.com");
        HttpResponse response = null;
        boolean res = false;
        try {
            response = loginClient.execute(get);
            String content = EntityUtils.toString(response.getEntity());
            if (content.contains("bds.comm.user=")) {
                Pattern pattern = Pattern.compile("bds.comm.user=\"(.*?)\"");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    System.out.println("登录名:/Login as:" + matcher.group(1));
                    res = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                EntityUtils.consume(response.getEntity());
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return res;
    }

    private void preLogin(CloseableHttpClient loginClient) {
        HttpGet get = new HttpGet("http://www.baidu.com/");
        try {
            loginClient.execute(get);
            get.abort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getYunPanToken() {
        try {
            System.err.println("添加离线任务中!/Adding Offline Task!");
            if(getPanToken().isEmpty()){
                String pt = iou.getStringFromFile("pantoken.txt");
                if (pt.isEmpty()) {
                    HttpClient panClient = getClient("pan");
                    HttpGet get = new HttpGet("http://pan.baidu.com");
                    HttpContext panContext = new BasicHttpContext();
                    HttpResponse response = panClient.execute(get, panContext);
                    String content = EntityUtils.toString(response.getEntity());
                    Pattern pattern = Pattern.compile("\"bdstoken\":\"(.*?)\"");
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        setPanToken(matcher.group(1));
                        iou.saveStringToFile(panToken, "pantoken.txt");
                    }
                } else {
                    setPanToken(pt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveToYunPan(String vcode, String input) {
        String taskid  = "";
        System.err.println("百度云Token/YunPan Token: " + panToken);
        try {
            String saveUrl = "http://pan.baidu.com/rest/2.0/services/cloud_dl?channel=chunlei&clienttype=0&web=1&bdstoken=" + panToken;
            String method = "add_task";
            String app_id = "250528";
            String save_path = getSavepath();

            ArrayList<NameValuePair> para = new ArrayList<>();
            para.add(new BasicNameValuePair("method", method));
            para.add(new BasicNameValuePair("app_id", app_id));
            para.add(new BasicNameValuePair("save_path", save_path));
            para.add(new BasicNameValuePair("source_url", source_url));
            para.add(new BasicNameValuePair("vcode", vcode));
            para.add(new BasicNameValuePair("input", input));

            System.out.println("Real Input is: " + input);
            System.out.println("Real Code is: " + vcode);
            System.out.println("Real Source is: " + source_url);
            System.out.println("Real panToken is: " + panToken);
            System.out.println("Real Path is: " + save_path);
            iou.saveUserInfo(email, password, save_path);

            HttpPost post = new HttpPost(saveUrl);
            HttpClient panClient = getClient("pan");
            HttpEntity postBodyEnt = new UrlEncodedFormEntity(para, "utf-8");
            post.setEntity(postBodyEnt);

            HttpContext context1 = new BasicHttpContext();
            HttpResponse response = panClient.execute(post, context1);

            String result = EntityUtils.toString(response.getEntity());

            JSONReader jsonReader = JSONFactory.instance().makeReader(new StringReader(result));
            JSONDocument jd = jsonReader.build();

            if (result.contains("-19")) {
                String img = jd.object().get("img").toString();
                this.newVcode = jd.object().get("vcode").toString();
                getInput(img);
            } else if (result.contains("task_id")) {
                taskid = jd.object().get("task_id").toString();
                System.out.println("添加任务成功!/Success! ID:" + taskid);
            } else {
                System.out.println("未知错误/Unknown Error" + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskid;
    }

    @Override
    public void run() {
        if (this.newThread) {
            System.out.println("New Thread");
            if(initYunPan()){
                getYunPanToken();
                saveToYunPan(newVcode, newInput);
            }
        } else {
            if(loginVcode){
//                loginCode = newVcode;
//                loginInput = newInput;
            }else {
                System.out.println("Input is: " + newInput);
                System.out.println("Code is: " + newVcode);
                System.out.println("Source is: " + source_url);
                System.out.println("panToken is: " + panToken);
                saveToYunPan(newVcode, newInput);
            }
        }

    }

    private String getInput(String imgSrc) {
        System.out.println("Enter Code");
        if (argsList.isEmpty()) {
            Thread t = new Thread(new ShowImg(imgSrc, panToken, newVcode, source_url));
            t.setName("New Thread Get Code");
            t.start();
            return null;
        } else {
            System.out.println("Please Instal jp2a, apt-get install jp2a, if you are using Linux");
            System.out.println("Or open this image in browser and input code: " + imgSrc);
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(newVcode.getBytes());
                String path = new BASE64Encoder().encode(md5.digest()).replace('/', '_');
                if(iou.downloadNetFile(imgSrc, path + ".jpeg")) {
                    System.out.println("Download Code to ./savedcodes/" + path + ".jpeg");
                    imgSrc = "./savedcodes/" + path + ".jpeg";
                    String newImgSrc = "./savedcodes/" + path + ".jpg";
                    Runtime.getRuntime().exec("convert " + imgSrc + " " + newImgSrc);
                    Thread.sleep(1000);
                    Process ps = Runtime.getRuntime().exec("jp2a --width=150 " + newImgSrc);
                    System.out.print(loadStream(ps.getInputStream()));
                    System.err.print(loadStream(ps.getErrorStream()));
                    Thread.sleep(1000);
                    Runtime.getRuntime().exec("rm " + imgSrc);
                    Runtime.getRuntime().exec("rm " + newImgSrc);
                    System.out.println("Please Enter Verify Code:");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Scanner sc = new Scanner(System.in);
            newInput = sc.nextLine();
            return newInput;
        }
    }

    public String loadStream(InputStream in) throws IOException {
        int ptr = 0;
        in = new BufferedInputStream(in);
        StringBuffer buffer = new StringBuffer();
        while ((ptr = in.read()) != -1) {
            buffer.append((char) ptr);
        }
        return buffer.toString();
    }

    public void renameFile(String path, String oldName, String newName){
        String renameUrl = "http://pan.baidu.com/api/filemanager?opera=rename&async=2&channel=chunlei&clienttype=0&web=1&app_id=250528&bdstoken=" + panToken;
        if(!path.endsWith("/")){
            path += "/";
        }

        ArrayList<NameValuePair> para = new ArrayList<>();
        para.add(new BasicNameValuePair("filelist", "[{\"path\":\"" + path.toUpperCase() + oldName + "\",\"newname\":\"" + newName + "\"}]"));
        try {
            HttpPost post = new HttpPost(renameUrl);
            HttpClient panClient = getClient("pan");
            HttpEntity postBodyEnt = new UrlEncodedFormEntity(para, "utf-8");
            post.setEntity(postBodyEnt);

            HttpContext context1 = new BasicHttpContext();
            HttpResponse response = panClient.execute(post, context1);

            String result = EntityUtils.toString(response.getEntity());
//            System.out.print(result);
            if(result.contains("taskid")){
                System.out.println("改名成功!/Rename Success! From: " + oldName + " TO: " + newName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
