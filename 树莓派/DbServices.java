package com.yucheng.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wjs.bean.UserInfo;
import com.wjs.db.UserInfoDB;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wjs on 2017/9/11.
 */

public class DbServices extends Service
{
    ServerSocket socket;
    boolean isrun=true;
    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new SockerService()).start();
    }
    class SockerService implements Runnable
    {
        @Override
        public void run() {
            try {
                socket=new ServerSocket(8080);
                if(isrun)
                {
                    Socket sock = socket.accept();
                    InputStream inputStream=sock.getInputStream();
                    OutputStream outputStream=sock.getOutputStream();
                    new mInputStream(inputStream,outputStream).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class mInputStream extends Thread
    {
        InputStream inputStream;
        OutputStream outputStream;
        public mInputStream(InputStream inputStream,OutputStream outputStream)
        {
            this.inputStream=inputStream;
            this.outputStream=outputStream;
        }

        @Override
        public void run() {
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(new DataInputStream(inputStream)));
            BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(new DataOutputStream(outputStream)));
            String readString=null;
            try {
                while((readString=bufferedReader.readLine())!=null&&isrun)
                {
                    if(readString.equals("@userinfo@"))
                    {
                        UserInfoDB userInfoDB=new UserInfoDB(DbServices.this);
                        UserInfo userInfo=userInfoDB.selectUser();
                        if(userInfo!=null)
                        {
                            bufferedWriter.write(userInfo.toJSON());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isrun=false;
    }
}
