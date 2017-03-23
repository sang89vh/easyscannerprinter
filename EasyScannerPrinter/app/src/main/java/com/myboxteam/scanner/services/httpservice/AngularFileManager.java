package com.myboxteam.scanner.services.httpservice;

import android.content.Context;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * https://github.com/joni2back/angular-filemanager/blob/master/API.md
 * @author Admin
 *
 */
public class AngularFileManager{
	private String TAG="AngularFileManager";
	private Context mContext;
	private JSONObject errorMessage;
	public AngularFileManager(Context context){
		this.mContext=context;
		 errorMessage = new JSONObject();
		try {

			String error = "Access denied to remove file";
			JSONObject data = new JSONObject();
			data.put("success", false);
			data.put("error", error);

			errorMessage.put("result", data);

		}catch (JSONException e){
			Log.e(TAG,e.getMessage(),e);
		}
	}
/*
	Listing (URL: fileManagerConfig.listUrl, Method: POST)

	JSON Request content

	{
	    "action": "list",
	    "path": "/public_html"
	}
	JSON Response

	{ "result": [ 
	    {
	        "name": "magento",
	        "rights": "drwxr-xr-x",
	        "size": "4096",
	        "date": "2016-03-03 15:31:40",
	        "type": "dir"
	    }, {
	        "name": "index.php",
	        "rights": "-rw-r--r--",
	        "size": "549923",
	        "date": "2016-03-03 15:31:40",
	        "type": "file"
	    }
	]}
	
	*/
	protected JSONObject listing(JSONObject para) throws JSONException {
		String path = para.getString("path");


		JSONObject result = new JSONObject();
		JSONArray data = new JSONArray();
		
		result.put("result", data);
		
		return result;
	}	


	/*
	Edit file (URL: fileManagerConfig.editUrl, Method: POST)

	JSON Request content

	{
	    "action": "edit",
	    "item": "/public_html/index.php",
	    "content": "<?php echo random(); ?>"
	}
	JSON Response

	{ "result": { "success": true, "error": null } }
	
	
	*/
	protected JSONObject edit(JSONObject para) throws JSONException {
		String item = para.getString("item");
		String content = para.getString("content");
		
		String error=null;
		Boolean success = true;
		try  {
			BufferedWriter bw = new BufferedWriter(new FileWriter(item));

			bw.write(content);

			// no need to close it.
		    bw.close();

		} catch (IOException e) {

			error = e.getMessage();

		}

		
		JSONObject result = new JSONObject();
		
		JSONObject data = new JSONObject();
		data.put("success", success);
		data.put("error", error);
		
		result.put("result", data);
		
		return result;
	}
	/*
	Get content of a file (URL: fileManagerConfig.getContentUrl, Method: POST)

	JSON Request content

	{
	    "action": "getContent",
	    "item": "/public_html/index.php"
	}
	JSON Response

	{ "result": "<?php echo random(); ?>" }
	
	*/
	protected JSONObject getContent(JSONObject para) throws JSONException {
		String item = para.getString("item");
		
		JSONObject result = new JSONObject();
		
		JSONObject data = new JSONObject();
		
		result.put("result", data);
		
		return result;
	}

	/*
	Set permissions (URL: fileManagerConfig.permissionsUrl, Method: POST)

	JSON Request content

	{
	    "action": "changePermissions",
	    "items": ["/public_html/root", "/public_html/index.php"],
	    "permsCode": "653",
	    "perms": "rw-r-x-wx",
	    "recursive": true
	}
	JSON Response

	{ "result": { "success": true, "error": null } }
	
	*/
	protected JSONObject changePermissions(JSONObject para) throws JSONException {
		JSONArray items = para.getJSONArray("items");
		String perms = para.getString("perms");
		String permsCode = para.getString("permsCode");
		Boolean recursive = para.getBoolean("recursive");


		Boolean success = true;
		String error=null;
		for (int i = 0; i < items.length(); i++) {
			
			String object = (String) items.getString(i);
			error = changePermission(permsCode,object);
			if(error != null){
				success = false;
			}
		}
		JSONObject result = new JSONObject();

		JSONObject data = new JSONObject();
		data.put("success", success);
		data.put("error", error);
		
		result.put("result", data);
		
		return result;
	}




	/*
	Download multiples files in ZIP/TAR (URL: fileManagerConfig.downloadFileUrl, Method: GET)

	JSON Request content

	{
	    "action": "downloadMultiple",
	    "items": ["/public_html/image1.jpg", "/public_html/image2.jpg"],
	    "toFilename": "multiple-items.zip"
	}}
	Response

	-File content
	Errors / Exceptions

	Any backend error should be with an error 500 HTTP code.

	Btw, you can also report errors with a 200 response both using this json structure

	{ "result": {
	    "success": false,
	    "error": "Access denied to remove file"
	}}
	
	*/
	protected JSONObject downloadMultiple(JSONObject para) throws JSONException {
		JSONArray items = para.getJSONArray("items");
		String toFilename = para.getString("toFilename");
		
		JSONObject result = new JSONObject();
		
		String error="Access denied to remove file";
		JSONObject data = new JSONObject();
		data.put("success", false);
		data.put("error", error);
		
		result.put("result", data);
		
		return result;
	}

	protected JSONObject error(){

		return errorMessage;
	}


	public  String changePermission(String pers,String path){
		Process process = null;
		DataOutputStream dataOutputStream = null;

		try {
			process = Runtime.getRuntime().exec("su");
			dataOutputStream = new DataOutputStream(process.getOutputStream());
			dataOutputStream.writeBytes("chmod "+pers+" "+path+"\n");
			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush();
			process.waitFor();
		} catch (Exception e) {

			return e.getMessage();
		} finally {
			try {
				if (dataOutputStream != null) {
					dataOutputStream.close();
				}
				process.destroy();
			} catch (Exception e) {
				return e.getMessage();
			}
		}
		return null;
	}





}
