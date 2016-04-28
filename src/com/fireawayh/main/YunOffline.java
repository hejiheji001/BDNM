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
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YunOffline implements Runnable {
    private static final String TOKEN_GET_URL = "https://passport.baidu.com/v2/api/?getapi&tpl=mn&apiver=v3&tt=1453851410376&class=login&gid=46392DC-171D-4213-A522-BF70BC214F9C&logintype=dialogLogin&callback=bd__cbs__k3wtgt";
    private static final String LOGIN_POST_URL = "https://passport.baidu.com/v2/api/?login";
    private static final String HEADER_USER_AGENT = "netdisk;4.6.2.0;PC;PC-Windows;10.0.10240;WindowsBaiduYunGuanJia";
    private static ArrayList<String> argsList = new ArrayList<>();
    private static IOUtils iou = new IOUtils();
    private SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(SSLContexts.createDefault(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    private String email;
    private String password;
    private String token;
    private String panToken = "";
    private String source_url;
    private String renameFile = "";
    private List<String[]> source_urls = new ArrayList<>();
    private volatile String newVcode;
    private volatile String newInput;
    private String savepath = "/";
    private boolean loginVcode = true;
    private boolean newThread = false;
    private BasicCookieStore cookieStore = new BasicCookieStore();
    private String[][] err = new String[][]{
            {"-1", "系统错误,请您稍后再试"},
            {"1", "您输入的帐号格式不正确"},
            {"2", "您输入的帐号不存在"},
            {"3", "验证码不存在或已过期,请重新输入"},
            {"4", "您输入的帐号或密码有误"},
            {"5", "请在弹出的窗口操作,或重新登录"},
            {"6", "您输入的验证码有误"},
            {"7", "密码错误"},
            {"16", "您的帐号因安全问题已被限制登录"},
            {"257", "请输入验证码"},
            {"100027", "百度正在进行系统升级，暂时不能提供服务，敬请谅解"},
            {"120016", "未知错误 120016"},
            {"18", "未知错误 18"},
            {"400031", "请在弹出的窗口操作,或重新登录"},
            {"400032", "未知错误 400032"},
            {"400034", "未知错误 400034"},
            {"401007", "您的手机号关联了其他帐号，请选择登录"},
            {"120021", "登录失败,请在弹出的窗口操作,或重新登录"},
            {"500010", "登录过于频繁,请24小时后再试"},
            {"200010", "验证码不存在或已过期"},
            {"100005", "系统错误,请您稍后再试"},
            {"120019", "请在弹出的窗口操作,或重新登录"},
            {"110024", "此帐号暂未激活"},
            {"100023", "开启Cookie之后才能登录"},
            {"17", "您的帐号已锁定,请解锁后登录'http://passport.baidu.com/v2/?ucenterfeedback#login_10"},
            {"400401", "未知错误 400401 可能需要在网页端重新登陆后进行手机验证"},
            {"400037", "未知错误 400037 可能需要在网页端重新登陆后进行手机验证"}
    };

    public YunOffline(String email, String password) {
        this.email = email;
        this.password = password;
    }
//    private String path;
//    private String oldName;
//    private String newName;

    public YunOffline(String email, String password, String[] sourceArr, String path, boolean newThread) {
        this.email = email;
        this.password = password;
        this.source_url = sourceArr[0];
        this.newThread = newThread;
        this.savepath = path;
        if (sourceArr[2] != "") {
            iou.appendStringToFile("Item:path=" + sourceArr[1] + ",oldName=" + sourceArr[2] + ",newName=" + sourceArr[3], "rename.txt");
        }
    }

    public YunOffline(String panToken, String source_url, String newVcode, String newInput) {
        this.panToken = panToken;
        this.source_url = source_url;
        this.newVcode = newVcode;
        this.newInput = newInput;
    }

    public YunOffline(String email, String password, String path, boolean newThread, List<String[]> source_urls) {
        this.email = email;
        this.password = password;
        this.newThread = newThread;
        this.savepath = path;
        this.source_urls = source_urls;
        for (String[] s : source_urls) {
            s[1] = path;
            iou.appendStringToFile("Item:path=" + path + ",oldName=" + s[2] + ",newName=" + s[3], "rename.txt");
        }
    }

    public YunOffline(String email, String password, String renameFile, boolean newThread) {
        this.email = email;
        this.password = password;
        this.newThread = newThread;
        this.renameFile = renameFile;
    }

    public static void main(String[] args) {
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
                    if (BD.initYunPan()) {
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
                            if (BD2.initYunPan()) {
                                BD2.getYunPanToken();
                                BD2.saveToYunPan("", "");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "-r":
                    break;
            }
        }
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(BasicCookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

//    public synchronized void captchaSetter(String v, String c) {
//        newVcode = v;
//        newInput = c;
//    }
//
//    public synchronized String getC() {
//        return newInput;
//    }
//
//    public synchronized String getV() {
//        return newVcode;
//    }

    public String getErrMsg(String errCode){
        String msg = "";
        for (String[] s : err){
            if(s[0].equals(errCode)){
                msg = s[1];
            }
        }
        return msg;
    }

    public String getSavepath() {
        return savepath;
    }

    public void setSavepath(String savepath) {
        this.savepath = savepath;
    }

    public String getPanToken() {
        return this.panToken;
    }

//    public void getCloudInfo() throws Exception {
//        URL u = new URL("https://pcs.baidu.com/rest/2.0/pcs/quota?method=info&access_token=" + token);
//        URLConnection conn = u.openConnection();// 打开网页链接
//        // 获取用户云盘信息
//    }

    public void setPanToken(String panToken) {
        this.panToken = panToken;
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
                Pattern pattern = Pattern.compile("err_no=(.*?)&");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    String errCode = matcher.group(1);
                    if(errCode.equals("0")){
                        result = true;
                        saveCookie();
                        System.err.println("登陆成功!/Login Success!");
                    }else{
                        System.err.println(getErrMsg(errCode));
                        boolean code = false;
                        if (errCode.equals("257")) {
                            code = true;
                            System.err.println("Need Captcha");
                        } else if (errCode.equals("6")) {
                            code = true;
                            System.err.println("Captcha wrong");
                        }
                        if (code) {
                            pattern = Pattern.compile("codeString=(.*?)&");
                            matcher = pattern.matcher(content);
                            if (matcher.find()) {
//                                if(newVcode != null && newInput != null){
//                                    result = initYunPan();
//                                }else{
                                this.newVcode = matcher.group(1);
                                String img = "http://passport.baidu.com/cgi-bin/genimage?" + newVcode;
                                newInput = enterCaptcha(img);
                                System.err.println("使用验证码重新登录.../Login Again With Verify Code...");
                                post.abort();
//                                    if(newInput != null) {
                                result = initYunPan();
//                                    }
//                                while (this.newInput == null){
//                                    System.err.println("Waiting for Captcha" + getC());
//                                    Thread.sleep(3000);
//                                }
//                                result = initYunPan();
//                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    EntityUtils.consume(response != null ? response.getEntity() : null);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
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
//        switch (type) {
//            case "init":
//                list.add(new BasicHeader("Host", HEADER_HOST));
//                list.add(new BasicHeader("Connection", HEADER_CONNECTION));
//                list.add(new BasicHeader("Accept", HEADER_ACCEPT));
//                list.add(new BasicHeader("Origin", HEADER_ORIGIN));
                list.add(new BasicHeader("User-Agent", HEADER_USER_AGENT));
                list.add(new BasicHeader("Referer", "http://pan.baidu.com/disk/home"));
//                list.add(new BasicHeader("Accept-Encoding", HEADER_ACCEPT_ENCODING));
//                list.add(new BasicHeader("Accept-Language", HEADER_ACCEPT_LANGUAGE));
//                list.add(new BasicHeader("Cache-Control", "max-age=0"));
//                list.add(new BasicHeader("Connection", HEADER_CONNECTION));
//                break;
//            case "pan":
//                list.add(new BasicHeader("Host", "pan.baidu.com"));
//                list.add(new BasicHeader("Connection", HEADER_CONNECTION));
//                list.add(new BasicHeader("Accept", HEADER_ACCEPT));
//                list.add(new BasicHeader("Origin", "http://pan.baidu.com"));
//                list.add(new BasicHeader("User-Agent", HEADER_USER_AGENT));
//                list.add(new BasicHeader("Referer", "http://pan.baidu.com/disk/home"));
//                list.add(new BasicHeader("Accept-Encoding", HEADER_ACCEPT_ENCODING));
//                list.add(new BasicHeader("Accept-Language", HEADER_ACCEPT_LANGUAGE));
//                break;
//        }
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
                EntityUtils.consume(response != null ? response.getEntity() : null);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private UrlEncodedFormEntity produceFormEntity() {
        List<NameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("staticpage", "http://www.baidu.com/cache/user/html/v3Jump.html"));
        parameters.add(new BasicNameValuePair("charset", "utf-8"));
        parameters.add(new BasicNameValuePair("token", token));
        parameters.add(new BasicNameValuePair("tpl", "pp"));
        parameters.add(new BasicNameValuePair("subpro", ""));
        parameters.add(new BasicNameValuePair("apiver", "v3"));
        parameters.add(new BasicNameValuePair("tt", "" + System.currentTimeMillis()));
        parameters.add(new BasicNameValuePair("codestring", newVcode));
        parameters.add(new BasicNameValuePair("safeflg", "0"));
        parameters.add(new BasicNameValuePair("u", "https://passport.baidu.com/"));
        parameters.add(new BasicNameValuePair("isPhone", "false"));
        parameters.add(new BasicNameValuePair("quick_user", "0"));
        parameters.add(new BasicNameValuePair("logintype", "basicLogin"));
        parameters.add(new BasicNameValuePair("logLoginType", "pc_loginBasic"));
        parameters.add(new BasicNameValuePair("loginmerge", "true"));
        parameters.add(new BasicNameValuePair("username", email));
        parameters.add(new BasicNameValuePair("password", password));
        parameters.add(new BasicNameValuePair("verifycode", newInput));
        parameters.add(new BasicNameValuePair("men_pass", "on"));
        parameters.add(new BasicNameValuePair("ppui_logintime", "50918"));
        parameters.add(new BasicNameValuePair("countrycode", ""));
        parameters.add(new BasicNameValuePair("callback", "parent.bd__pcbs__oa36qm"));
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
                EntityUtils.consume(response != null ? response.getEntity() : null);
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

    public boolean saveToYunPan(String vcode, String input) {
        boolean flag = true;
        loginVcode = false;
        System.err.println("百度云Token/YunPan Token: " + panToken);
        try {
            String saveUrl = "http://pan.baidu.com/rest/2.0/services/cloud_dl?channel=chunlei&clienttype=0&web=1&app_id=250528&bdstoken=" + panToken;
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
                this.newInput = enterCaptcha(img);
                flag = false;
            } else if (result.contains("task_id")) {
                String taskid = jd.object().get("task_id").toString();
                System.out.println("添加任务成功!/Success! ID:" + taskid);
            } else {
                System.out.println("未知错误/Unknown Error" + result);
                flag = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public void run() {
        if (this.newThread) {
            System.out.println("New Thread");
            if(initYunPan()){
                getYunPanToken();
                boolean flag;

                if (renameFile.isEmpty()) {
                    if (source_urls.size() == 0) {
                        flag = saveToYunPan(newVcode, newInput);
                        while (!flag) {
                            System.out.println("Rest 5 seconds and try again with code " + newInput);
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            flag = saveToYunPan(newVcode, newInput);
                        }
                    } else {
                        for (String[] s : source_urls) {
                            this.source_url = s[0];
                            flag = saveToYunPan(newVcode, newInput);

                            while (!flag) {
                                System.out.println("Rest 5 seconds and try again with code " + newInput);
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                flag = saveToYunPan(newVcode, newInput);
                            }

                            System.out.println("Rest 5 seconds");
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println("转存完成/Finished! You can rename all tasks afterwards");
                    }
                } else {
                    String[] content = iou.getStringFromFile(renameFile).split("Item:");
                    for (String c : content) {
                        if (!c.isEmpty()) {
                            String[] info = c.split(",");
                            String path = info[0].split("=")[1];
                            String oldName = info[1].split("=")[1];
                            String newName = info[2].split("=")[1];
                            renameFile(path, oldName, newName);
                        }
                    }
                    System.out.println("批量改名完成/All Tasks Finished!");
                }
            }
        } else {
            if(loginVcode){
                initYunPan();

//                Thread t = new Thread(new YunOffline);
//                System.out.print(this.newInput);
//                captchaSetter(this.newVcode, this.newInput);
            }else {
                System.out.println("Input is: " + newInput);
                System.out.println("Code is: " + newVcode);
                System.out.println("Source is: " + source_url);
                System.out.println("panToken is: " + panToken);
                saveToYunPan(newVcode, newInput);
            }
        }

    }

    private String enterCaptcha(String imgSrc) {
        System.out.println("Enter Captcha");
        if (argsList.isEmpty()) {
            ShowImg s = new ShowImg(imgSrc, panToken, newVcode, source_url);
            s.showFrame();
            s.loadURLImage(imgSrc);
            while (s.getCode() == null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return s.getCode();
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
        int ptr;
        in = new BufferedInputStream(in);
        StringBuilder buffer = new StringBuilder();
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
            if(result.contains("taskid")){
                System.out.println("改名成功!/Rename Success! From: " + oldName + " TO: " + newName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
