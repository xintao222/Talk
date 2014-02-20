package ivandgetic.talk;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements ActionBar.TabListener {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private long ExitTime = 0;
    public Thread ConnectThread, ReceiveThread;
    public Socket socket;
    ImageView compose_button_send;
    EditText compose_edit;
    String message;
    OutputStream outStream = null;
    ListView listView;
    List<String> messageshow = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }


        ConnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("192.168.137.1", 10086);
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
        /*compose_button_send.setOnClickListener(new View.OnClickListener() {
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
        });*/
    }

    public void send(View view){//发送消息
        message = compose_edit.getText().toString() + "\n";//获取输入框内容
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(PlaceholderFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.message).toUpperCase(l);//返回ViewPage标题
                case 1:
                    return getString(R.string.people).toUpperCase(l);//返回ViewPage标题
            }
            return null;
        }
    }

    public class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                compose_edit = (EditText) rootView.findViewById(R.id.compose_edit);
                listView = (ListView) rootView.findViewById(R.id.listView);
                compose_button_send = (ImageView) rootView.findViewById(R.id.compose_button_send);
            } else
                rootView = inflater.inflate(R.layout.activity_online, container, false);
            return rootView;
        }
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
