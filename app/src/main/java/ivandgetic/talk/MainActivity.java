package ivandgetic.talk;

import android.app.Activity;
import android.content.ReceiverCallNotAllowedException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private long ExitTime = 0;
    public Thread ConnectThread, ReceiveThread;
    public Socket socket;
    ImageView compose_button_send;
    EditText compose_edit;
    String message, KEY_CONTENT;
    OutputStream outStream = null;
    ListView listView;
    List<String> messageshow = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        compose_button_send = (ImageView) findViewById(R.id.compose_button_send);
        compose_edit = (EditText) findViewById(R.id.compose_edit);
        ConnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("192.168.1.200", 10086);
                    final BufferedReader br = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    ReceiveThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (true) {
                                    final String line = br.readLine();
                                    listView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            messageshow.add(line);// 把接收到的消息添加到List用来显示
                                            listView.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                                                    android.R.layout.simple_list_item_1, messageshow));// 刷新ListView
                                            listView.setSelection(messageshow.size());// 滚动到ListView的最下面
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    ReceiveThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        ConnectThread.start(); // 开启线程
        compose_button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = compose_edit.getText().toString() + "\n";
                byte[] msgBuffer = null;
                try {
                    msgBuffer = message.getBytes("UTF-8");//字符编码转换
                    outStream = socket.getOutputStream();//获得Socket的输出流
                    outStream.write(msgBuffer);//发送数据
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                compose_edit.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_clear) {
            messageshow.removeAll(messageshow);// 删掉List全部的东西
            listView.setAdapter(new ArrayAdapter<String>(MainActivity.this,
                    android.R.layout.simple_list_item_1, messageshow));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - ExitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次后退键退出程序",
                        Toast.LENGTH_SHORT).show();
                ExitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
                android.os.Process.killProcess(android.os.Process.myPid());
                finish();
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        if (ConnectThread != null)
            ConnectThread.interrupt();
        if (ReceiveThread != null)
            ReceiveThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
