package com.myboxteam.scanner.services.httpservice;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import com.myboxteam.scanner.utils.DatabaseUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by jack on 2/25/17.
 */

public class HTTPServer extends NanoHTTPD {
    private String password;
    private boolean isPasswordProtected = false;
    private AngularFileManager angularFileManager;
    private static String TAG = "HTTPServer";
    private JSONArray LIST_ELEMENTS;
    private HTTPServer mCallbacks;
    private String mPath, mInput;
    private boolean mRootMode, isRegexEnabled, isMatchesEnabled;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private StringBuffer mMainHtml, mLoginHtml;

    public static final String KEY_PATH = "path";
    public static final String KEY_INPUT = "input";
    public static final String KEY_OPEN_MODE = "open_mode";
    public static final String KEY_ROOT_MODE = "root_mode";
    public static final String KEY_REGEX = "regex";
    public static final String KEY_REGEX_MATCHES = "matches";

    private Context mContext;


    public HTTPServer(int port, Context context) {
        super(port);
        mContext = context;


        mCallbacks = this;

        angularFileManager = new AngularFileManager(mContext);

        mMainHtml = new StringBuffer("<html lang=\"en\" data-ng-app=\"FileManagerApp\">\n \n<header> \n");
        mMainHtml.append("<meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\"> \n");
        mMainHtml.append("<meta charset=\"utf-8\"> \n");
        mMainHtml.append("<title>Easy File Manager</title> \n");
        mMainHtml.append("<script src=\"/angular_min.js\"></script> \n");
        mMainHtml.append("<script src=\"/angular_translate_min.js\"></script> \n");
        mMainHtml.append("<script src=\"/ng_file_upload_min.js\"></script> \n");
        mMainHtml.append("<script src=\"/jquery_min.js\"></script> \n");
        mMainHtml.append("<script src=\"/bootstrap_min.js\"></script> \n");
        mMainHtml.append("<link rel=\"stylesheet\" href=\"/bootstrap_min.css\" /> \n");

        mMainHtml.append("<link rel=\"stylesheet\" href=\"/angular_filemanager_min.css\"> \n");
        mMainHtml.append("<script src=\"/angular_filemanager_min.js\"></script> \n");
        mMainHtml.append("</header> \n");
        mMainHtml.append("<script type=\"text/javascript\">\n");
        mMainHtml.append("    angular.module('FileManagerApp').config(['fileManagerConfigProvider', function (config) {\n");
        mMainHtml.append("      var defaults = config.$get();\n");
        mMainHtml.append("      config.set({\n");
        mMainHtml.append("        appName: 'angular-filemanager',\n");
        mMainHtml.append("        pickCallback: function(item) {\n");
        mMainHtml.append("          var msg = 'Picked %s \"%s\" for external use'\n");
        mMainHtml.append("            .replace('%s', item.type)\n");
        mMainHtml.append("            .replace('%s', item.fullPath());\n");
        mMainHtml.append("          window.alert(msg);\n");
        mMainHtml.append("        },\n");
        mMainHtml.append("\n");
        mMainHtml.append("        allowedActions: angular.extend(defaults.allowedActions, {\n");
        mMainHtml.append("          upload: false,\n");
        mMainHtml.append("          rename: false,\n");
        mMainHtml.append("          move: false,\n");
        mMainHtml.append("          copy: false,\n");
        mMainHtml.append("          edit: false,\n");
        mMainHtml.append("          changePermissions: false,\n");
        mMainHtml.append("          compress: false,\n");
        mMainHtml.append("          compressChooseName: false,\n");
        mMainHtml.append("          extract: false,\n");
        mMainHtml.append("          downloadMultiple: false,\n");
        mMainHtml.append("          remove: false,\n");
        mMainHtml.append("          createFolder: false,\n");
        mMainHtml.append("          pickFiles: false,\n");
        mMainHtml.append("          pickFolders: false,\n");
        mMainHtml.append("        }),\n");
        mMainHtml.append("      });\n");
        mMainHtml.append("    }]);\n");
        mMainHtml.append("$(document).ready(function(){\n");
        mMainHtml.append("\tsetTimeout(function(){$(\".breadcrumb li\")[0].remove();},100);\n");
        mMainHtml.append("})\n");
        mMainHtml.append("  </script>\n");
        mMainHtml.append("<body class=\"ng-cloak\">\n");
        mMainHtml.append("<div class=\"col-md-12\" >\n");
        mMainHtml.append("<angular-filemanager></angular-filemanager> \n");
        mMainHtml.append("<div class=\"container\"><a href=\"https://play.google.com/store/apps/developer?id=MyboxTeam\">Free Download File Manager on Google Play Store:Click here</a></div> \n");
        mMainHtml.append("</div> \n");
        mMainHtml.append("</body> \n</html>");

        mLoginHtml = new StringBuffer();
        mLoginHtml.append("<!DOCTYPE html>\n");
        mLoginHtml.append("<html lang=\"en\">\n");
        mLoginHtml.append("  <head>\n");
        mLoginHtml.append("    <meta charset=\"utf-8\">\n");
        mLoginHtml.append("    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n");
        mLoginHtml.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        mLoginHtml.append("    <meta name=\"description\" content=\"\">\n");
        mLoginHtml.append("    <meta name=\"author\" content=\"\">\n");
        mLoginHtml.append("    <title>Easy wifi file manager</title>\n");
        mLoginHtml.append("    <link href=\"/bootstrap_min.css\" rel=\"stylesheet\">\n");
//        mLoginHtml.append("    <script src=\"/bootstrap_min.js\"></script>\n");
        mLoginHtml.append("  </head>\n");
        mLoginHtml.append("\n");
        mLoginHtml.append("  <body>\n");
        mLoginHtml.append("    <div class=\"container\">\n");
        mLoginHtml.append("    <div class=\"col-md-4 col-sm-12\">\n");
        mLoginHtml.append("    </div>\n");
        mLoginHtml.append("    <div class=\"col-md-4 col-sm-12\">\n");
        mLoginHtml.append("      <form class=\"form-signin\" action=\"/\" method=\"POST\">\n");
        mLoginHtml.append("        <h2 class=\"form-signin-heading\">Please sign in</h2>\n");
        mLoginHtml.append("        <label for=\"inputPassword\" class=\"sr-only\">Password</label>\n");
        mLoginHtml.append("        <input name=\"password\" type=\"password\" id=\"inputPassword\" class=\"form-control\" placeholder=\"Password\" required>\n");
        mLoginHtml.append("        <button class=\"btn btn-lg btn-primary btn-block\" type=\"submit\">Sign in</button>\n");
        mLoginHtml.append("      </form>\n");
        mLoginHtml.append("    </div>\n");
        mLoginHtml.append("    <div class=\"col-md-4 col-sm-12\">\n");
        mLoginHtml.append("    </div>\n");
        mLoginHtml.append("    </div> \n");
        mLoginHtml.append("  </body>\n");
        mLoginHtml.append("</html>");

    }


    private static final Random RANDOM = new Random();
    private static final int TOKEN_SIZE = 24;
    private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String TOKEN_COOKIE = "__SESSION_ID__";
    private static HashMap<String, String> sessions = new HashMap<>();

    private String genSessionToken() {
        StringBuilder sb = new StringBuilder(TOKEN_SIZE);
        for (int i = 0; i < TOKEN_SIZE; i++) {
            sb.append(HEX[RANDOM.nextInt(HEX.length)]);
        }
        return sb.toString();
    }

    private String newSessionToken() {
        String token;
        do {
            token = genSessionToken();
        } while (sessions.containsKey(token));
        return token;
    }

    public synchronized String findSessionCreate(CookieHandler cookies) {
        String token = cookies.read(TOKEN_COOKIE);
        if (token == null) {
            token = newSessionToken();
            cookies.set(TOKEN_COOKIE, token, 10);
        }
        if (!sessions.containsKey(token)) {
            sessions.put(token, token);
        }
        return sessions.get(token);
    }

    public String findSession(CookieHandler cookies) {
        String token = cookies.read(TOKEN_COOKIE);
        if (token == null) {
            return null;
        } else {
            cookies.delete(TOKEN_COOKIE);
        }

        return sessions.get(token);
    }

    public void destroySession(String token, CookieHandler cookies) {
        sessions.remove(token);
        cookies.delete(TOKEN_COOKIE);
    }


    @Override
    public Response serve(IHTTPSession session) {

        String uri = session.getUri();
        Log.d(TAG, uri);

        if ("/".equals(uri)) {

            if (Method.POST.equals(session.getMethod())) {

                Map<String, String> files = new HashMap<String, String>();
                try {
                    session.parseBody(files);
                } catch (IOException ioe) {
                    Log.e(TAG, ioe.getMessage(), ioe);

                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                } catch (ResponseException re) {
                    Log.e(TAG, re.getMessage(), re);
                    return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
                }

                String mypass = session.getParms().get("password");

                if (password.equals(mypass)) {
                    findSessionCreate(session.getCookies());
                    return newFixedLengthResponse(mMainHtml.toString());
                } else {
                    return newFixedLengthResponse("oop!Wrong password <a href=\"\\\">Back to login page</a>");
                }
            }

            if (isPasswordProtected) {
                if (findSession(session.getCookies()) != null) {
                    return newFixedLengthResponse(mMainHtml.toString());
                } else {
                    return newFixedLengthResponse(mLoginHtml.toString());
                }
            } else {
                return newFixedLengthResponse(mMainHtml.toString());
            }

        } else if (uri.contains("js")) {
            InputStream fis;
            try {
                fis = mContext.getResources().getAssets().open(uri.substring(1, uri.length()));
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", angularFileManager.error().toString());
            }
            return newChunkedResponse(Response.Status.OK, "application/javascript", fis);

        } else if (uri.equals("/favicon.ico")) {
            InputStream fis;
            try {
                fis = mContext.getResources().getAssets().open(uri.substring(1, uri.length()));
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", angularFileManager.error().toString());
            }
            return newChunkedResponse(Response.Status.OK, "image/x-icon", fis);

        } else if (uri.contains("css")) {
            InputStream fis;
            try {
                fis = mContext.getResources().getAssets().open(uri.substring(1, uri.length()));
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", angularFileManager.error().toString());
            }
            return newChunkedResponse(Response.Status.OK, "text/css", fis);
        } else if (uri.contains("woff2")) {
            InputStream fis;
            try {
                fis = mContext.getResources().getAssets().open(uri.substring(1, uri.length()).replace("/", "_"));
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", angularFileManager.error().toString());
            }
            return newChunkedResponse(Response.Status.OK, "font/woff2", fis);
        } else if (uri.contains("woff")) {

            InputStream fis;
            try {
                fis = mContext.getResources().getAssets().open(uri.substring(1, uri.length()).replace("/", "_"));
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.OK, "application/json", angularFileManager.error().toString());
            }
            return newChunkedResponse(Response.Status.OK, "font/woff", fis);

        } else if (uri.equals("/bridges/php/handler.php")) {
            JSONObject para = null;
            String action = null;
            Map<String, String> files = new HashMap<String, String>();
            Method method = session.getMethod();
            String destination = null;
            List<String> filesUpload = new ArrayList<>();
            if (Method.PUT.equals(method) || Method.POST.equals(method)) {
                try {
                    session.parseBody(files);
                } catch (IOException ioe) {
                    Log.e(TAG, ioe.getMessage(), ioe);

                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                } catch (ResponseException re) {
                    Log.e(TAG, re.getMessage(), re);
                    return newFixedLengthResponse(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
                }


                try {
                    para = new JSONObject(files.get("postData"));
                    action = para.getString("action");


                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }

            } else {

                Map<String, String> getPara = session.getParms();

                action = getPara.get("action");
            }

            if ("list".equals(action)) {
                String realPath = null;
                try {
                    realPath = para.getString("path");

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }

                if ("/".equals(realPath)) {
                    List<ParseObject> dirs = getAllDocument();
                    return newFixedLengthResponse(Response.Status.OK, "application/json", listDirToJson(dirs).toString());
                }else {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Error");
                }

            } else if ("download".equals(action)) {

                String realPath = session.getParms().get("path");
                Log.d(TAG, "real path:" + realPath);

                String objectId = realPath.substring(realPath.lastIndexOf("_")+1,realPath.lastIndexOf("."));

                Log.d(TAG,"realPath:"+realPath);
                Log.d(TAG,"object id:"+objectId);

                ParseObject po= DatabaseUtils.getBookById(objectId);

                realPath = po.getString("pdfPath");

                InputStream is = null;
                File f = null;
                try {

                    f = new File(realPath);
                    is = new FileInputStream(f);

                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getMessage(), e);

                    return newFixedLengthResponse(Response.Status.OK, "application/json", angularFileManager.error().toString());
                }
                String extension = MimeTypeMap.getFileExtensionFromUrl(realPath);

                String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                Response res = newChunkedResponse(Response.Status.OK, type, is);
                res.addHeader("Content-Disposition", "attachment; filename=\"" + po.getString("title")+".pdf" + "\"");
                return res;


            } else if ("getContent".equals(action)) {
                String realPath = null;
                try {
                    realPath = para.getString("item");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                Log.d(TAG, "real path:" + realPath);
                InputStream is = null;
                try {

                    File f = new File(realPath);
                    is = new FileInputStream(f);

                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.getMessage(), e);
                }

                String content = convertStreamToString(is);
                return newFixedLengthResponse(Response.Status.OK, "application/json", stringContentToJson(content).toString());

            } else {
                return newFixedLengthResponse(Response.Status.OK, "application/json", listDirToJson(null).toString());
            }

        } else {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Error");
        }

    }


    private List<ParseObject> getAllDocument() {
        List<ParseObject> data = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(DatabaseUtils.BOOK_COLLECTION);
        //query.whereEqualTo(AppConfig.AUDIO_ISDELETE, 0);
        //query.orderByDescending(AppConfig.AUDIO_GRAVITY);
        query.fromLocalDatastore();

        try {
            data = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return data;
    }


    private JSONObject stringContentToJson(String content) {
        JSONObject result = new JSONObject();

        try {

            result.put("result", content);

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return result;
    }

    private JSONObject listDirToJson(List<ParseObject> dirs) {
        JSONObject result = new JSONObject();
        JSONArray data = new JSONArray();
        for (ParseObject parseObject : dirs) {
            try {

                String pdfPath = parseObject.getString("pdfPath");
                if (pdfPath != null) {
                    File f = new File(pdfPath);
                    Date lastModDate = new Date(f.lastModified());
                    String title = parseObject.getString("title");
                    String objectId = parseObject.getObjectId();

                    JSONObject jsonDir = new JSONObject();

                    jsonDir.put("name",title + "_" + objectId +".pdf");
                    jsonDir.put("rights", "drwxr-xr-x");
                    jsonDir.put("size", String.valueOf(f.length()));
                    jsonDir.put("date", sdf.format(lastModDate));
                    jsonDir.put("type", "file");

                    data.put(jsonDir);
                }


            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        try {

            result.put("result", data);

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return result;
    }

    /**
     * method converts bash style regular expression to java. See {@link Pattern}
     *
     * @param originalString
     * @return converted string
     */
    private String bashRegexToJava(String originalString) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < originalString.length(); i++) {
            switch (originalString.charAt(i) + "") {
                case "*":
                    stringBuilder.append("\\w*");
                    break;
                case "?":
                    stringBuilder.append("\\w");
                    break;
                default:
                    stringBuilder.append(originalString.charAt(i));
                    break;
            }
        }

        Log.d(getClass().getSimpleName(), stringBuilder.toString());
        return stringBuilder.toString();
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPasswordProtected() {
        return isPasswordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        isPasswordProtected = passwordProtected;
    }


    @Override
    public void stop() {
        super.stop();
        //reset session
        sessions = new HashMap<>();
    }


}