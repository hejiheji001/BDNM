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

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class YunOffline implements Runnable {
    private static final String TOKEN_GET_URL = "https://passport.baidu.com/v2/api/?getapi&tpl=mn&apiver=v3&class=login&logintype=dialogLogin";
    private static final String LOGIN_POST_URL = "https://passport.baidu.com/v2/api/?login";
    private static final String HEADER_HOST = "passport.baidu.com";
    private static final String HEADER_ORIGIN = "http://www.baidu.com";
    private static final String HEADER_CONNECTION = "keep-alive";
    private static final String HEADER_ACCEPT = "*/*";
    private static final String HEADER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36";
    private static final String HEADER_REFERER = "http://www.baidu.com/";
    private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    private static ArrayList<String> argsList;
    private static IOUtils iou = new IOUtils();
    private SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(SSLContexts.createDefault(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    private String email;
    private String password;
    private String token;
    private String panToken;
    private String source_url;
    private String newVcode;
    private String newInput;
    private boolean newThread = false;
    private BasicCookieStore cookieStore = new BasicCookieStore();

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

    public static void main(String[] args) {
//		set your email and password
//		YunOffline BD = new YunOffline("hejiheji001@163.com", "MyLifeForFire0");
//		BD.login();

        argsList = new ArrayList<>(Arrays.asList(args));
        if (argsList.isEmpty()) {
            new ShowGUI().showGui();
        } else {
            switch (args[0]) {
                case "-cli":
                    String userName = args[1];
                    String password = args[2];
                    YunOffline BD = new YunOffline(userName, password);
                    iou.saveUserInfo(userName, password);
                    BD.setSource_url(args[3]);
                    BD.login();

                    break;
                case "-cli_conf":
                    try {
                        Properties prop = new Properties();
                        File file = new File(args[1]);
                        if (file.exists()) {
                            prop.load(new FileInputStream(file));
                            YunOffline BD2 = new YunOffline(prop.getProperty("Username"), prop.getProperty("Password"));
                            BD2.setSource_url(args[2]);
                            BD2.login();
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

    public void login() {
        System.err.println("正在验证登录状态.../Checking Login Status...");
        CloseableHttpClient loginClient = getClient("init");

        if (checkLogin(loginClient)) {
            System.err.println("无需登录!/Already Logged In!");
            getYunPanToken();
        } else {
            System.err.println("正在登录百度.../Login Now...");
            preLogin(loginClient);
            initToken(loginClient);

            HttpPost post = new HttpPost(LOGIN_POST_URL);
            post.setEntity(produceFormEntity());
            HttpResponse response = null;
            try {
                response = loginClient.execute(post);
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
                getYunPanToken();
            } else {
                System.err.println("登陆百度失败!/Login Failed!");
            }
        }
    }

    public void saveCookie() {
        if (!cookieStore.getCookies().isEmpty()) {
            try {
                File file = new File("cookie.file");
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
                out.writeObject(cookieStore.getCookies());
                out.close();
                System.out.println("Save Cookie To File");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("No Cookies");
        }
    }

    private BasicCookieStore getCookie() {
        if (cookieStore.getCookies().isEmpty()) {
            try {
                File file = new File("cookie.file");
                if (file.exists()) {
                    ObjectInputStream oi = new ObjectInputStream(new FileInputStream(file));
                    Object x = oi.readObject();
                    Object[] xx = ((ArrayList<?>) x).toArray();
                    Cookie[] xxx = new Cookie[xx.length];
                    for (int i = 0; i < xx.length; i++) {
                        xxx[i] = (Cookie) xx[i];
                    }
                    cookieStore.addCookies(xxx);
                    System.out.println("Get Cookie From File");
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        parameters.add(new BasicNameValuePair("apiver", "v3"));
        parameters.add(new BasicNameValuePair("safeflg", "0"));
        parameters.add(new BasicNameValuePair("u", "http://www.baidu.com/"));
        parameters.add(new BasicNameValuePair("isPhone", "false"));
        parameters.add(new BasicNameValuePair("quick_user", "0"));
        parameters.add(new BasicNameValuePair("loginmerge", "true"));
        parameters.add(new BasicNameValuePair("logintype", "dailoglogin"));
        parameters.add(new BasicNameValuePair("splogin", "rate"));
        parameters.add(new BasicNameValuePair("username", email));
        parameters.add(new BasicNameValuePair("password", password));
        parameters.add(new BasicNameValuePair("men_pass", "on"));
        parameters.add(new BasicNameValuePair("callback", "parent.bd__pcbs__5i3pfd"));
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
            if (!getTokenFromFile()) {
                HttpClient panClient = getClient("pan");
                HttpGet get = new HttpGet("http://pan.baidu.com");
                HttpContext panContext = new BasicHttpContext();

                HttpResponse response = panClient.execute(get, panContext);
                String content = EntityUtils.toString(response.getEntity());
                Pattern pattern = Pattern.compile("\"bdstoken\":\"(.*?)\"");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    panToken = matcher.group(1);
                    saveTokenToFile(panToken);
                }
            }
//			this.source_url = "https://pbs.twimg.com/profile_images/682486169876054021/41DV0hpR_normal.png";
            saveToYunPan("", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean getTokenFromFile() {
        boolean result = false;
        File file = new File("token.txt");
        if (file.exists()) {
            try {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file)); // 建立一个输入流对象reader
                BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
                panToken = br.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!panToken.isEmpty()) {
                result = true;
            }
        }
        return result;
    }

    private void saveTokenToFile(String token) {
        try {
            File file = new File("token.txt");
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(token); // \r\n即为换行
            out.flush();
            out.close();
            System.out.println("Save Pantoken To File");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToYunPan(String vcode, String input) {
        System.err.println("百度云Token/YunPan Token: " + panToken);
        try {
            String saveUrl = "http://pan.baidu.com/rest/2.0/services/cloud_dl?channel=chunlei&clienttype=0&web=1&bdstoken=" + panToken;
            String method = "add_task";
            String app_id = "250528";
            String save_path = "/";

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

            HttpPost post = new HttpPost(saveUrl);
            HttpClient panClient = getClient("pan");
            HttpEntity postBodyEnt = new UrlEncodedFormEntity(para, "utf-8");
            post.setEntity(postBodyEnt);

            HttpContext context1 = new BasicHttpContext();
            HttpResponse response = panClient.execute(post, context1);

            String result = EntityUtils.toString(response.getEntity());

            if (result.contains("-19")) {
                JSONReader jsonReader = JSONFactory.instance().makeReader(new StringReader(result));
                JSONDocument jd = jsonReader.build();
                String img = jd.object().get("img").toString();
                this.newVcode = jd.object().get("vcode").toString();
                getInput(img);
            } else if (result.contains("task_id")) {
                System.out.println("添加任务成功!/Success!");

            } else {
                System.out.println("未知错误/Unknown Error" + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (this.newThread) {
            System.out.println("Newthread");
            login();
        } else {
            System.out.println("Input is: " + newInput);
            System.out.println("Code is: " + newVcode);
            System.out.println("Source is: " + source_url);
            System.out.println("panToken is: " + panToken);
            saveToYunPan(newVcode, newInput);
        }
    }

    private void getInput(String imgSrc) {
        System.out.println("Enter Code");
        if (argsList.isEmpty()) {
            Thread t = new Thread(new ShowImg(imgSrc, panToken, newVcode, source_url));
            t.setName("New Thread Get Code");
            t.start();
        } else {
            System.out.println("Please open this image in browser and input code: " + imgSrc);
            Scanner sc = new Scanner(System.in);
            newInput = sc.nextLine();
            saveToYunPan(newVcode, newInput);
        }
    }
}
