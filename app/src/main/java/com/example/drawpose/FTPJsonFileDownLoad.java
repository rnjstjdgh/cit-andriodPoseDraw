package com.example.drawpose;

import android.os.AsyncTask;
import java.lang.String;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

public class FTPJsonFileDownLoad extends AsyncTask<Void,Void,String> {


    @Override
    protected String doInBackground(Void... strings) {
        try{
            URL yahoo = new URL("http://106.10.45.102/SendDataResult.json");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            yahoo.openStream()));

            String inputLine;
            String out = (String) "";
            while ((inputLine = (String) in.readLine()) != null)
                out = out + inputLine;

            in.close();
            return out;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(String result) {
        // Call activity method with results
        MainActivity.jsonStr = result;
    }

}
