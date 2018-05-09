package com.ironbear775.musichunter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends BaseActivity {
    final static String TAG = "MusicHunter-Main";
    final static String LOCAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    final static String FILE_DIR = "/MusicHunter";
    final static String MY_ALIPAY_QRCODE = "https://qr.alipay.com/tsx06081gjjlyskuhkvxhef";
    final static String REQUEST_URL_1 = "http://test.tmaize.net:8080/api/proxy" +
            "?url=luaapp.cn/music.search.json" + "&urlParm=key:";
    final static String ALBUMARTURL = "https://y.gtimg.cn/music/photo_new/T002R300x300M000%1$s.jpg";
    /*"https://www.tikitiki.cn"*/
    final static String REQUEST_URL_2 = ";page:";
    /*"/searchjson.do?keyword=%1$s&page=%2$s&type=%3$s"*/
    public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    private ArrayList<Music> musicArrayList = new ArrayList<>();
    private ProgressDialog dialog;
    private JumpReceiver receiver;
    private ArrayList<byte[]> musicBytesList = new ArrayList<>();
    private ArrayList<String> songNameList = new ArrayList<>();
    private ArrayList<Long> idList = new ArrayList<>();
    private int count = 0;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        requestPermission();

        receiver = new JumpReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Jump Activity");
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction("Download Music");
        filter.addAction("Search Failed");

        registerReceiver(receiver, filter);

        input = findViewById(R.id.input);
        final Button searchButton = findViewById(R.id.search_button);

        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    search();
                    return true;
                }
                return false;
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
    }

    private void search() {
        String search = input.getText().toString();
        if ("".equals(search)) {
            Toast.makeText(MainActivity.this, R.string.input_null
                    , Toast.LENGTH_SHORT).show();
        } else {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle(R.string.searching);
            dialog.show();
            searchMusic(getApplicationContext(), search, "1",
                    true, musicArrayList);
        }
    }

    /**
     * 申请读取本地存储权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int check = ActivityCompat.checkSelfPermission(
                    MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (check != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1101) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                requestPermission();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.about)
                        .setMessage(R.string.about_app)
                        .show();
                break;
            case R.id.donate:
                try {
                    String qrcode = URLEncoder.encode(MY_ALIPAY_QRCODE, "utf-8");
                    final String alipayqr =
                            "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode="
                                    + qrcode;
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(alipayqr + "%3F_s%3Dweb-other&_t="
                                    + System.currentTimeMillis()));
                    startActivity(intent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, R.string.check_alipay_installed,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.contact_me:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D"
                                    + "2bqJtkjtHw4QfEvPJQpFnHFT_vkUpEN4")));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, R.string.check_qq_installed,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rate:
                try {
                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent intent = new Intent("android.intent.action.VIEW", uri);
                    intent.setPackage("com.coolapk.market");//指定应用市场
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, R.string.check_qq_installed,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList("songNameList", songNameList);
        outState.putSerializable("idList", idList);
        outState.putSerializable("musicBytesList", musicBytesList);
        outState.putInt("count", count);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        songNameList = savedInstanceState.getStringArrayList("songNameList");
        idList = (ArrayList<Long>) savedInstanceState.getSerializable("idList");
        musicBytesList = (ArrayList<byte[]>) savedInstanceState
                .getSerializable("musicBytesList");
        count = savedInstanceState.getInt("count");
    }

    /**
     * 根据歌曲名和艺术家搜索歌曲
     *
     * @param search        要搜索的关键字
     * @param page          搜索的页码
     * @param startActivity 是否跳转activity
     * @param arrayList     将结果储存在arrayList中
     */
    public static void searchMusic(final Context context, final String search, String page,
                                   final boolean startActivity, final ArrayList<Music> arrayList) {
        try {
            OkHttpClient client = new OkHttpClient();

            URL u = new URL("http://c.y.qq.com/soso/fcgi-bin/search_for_qq_cp"
                    + "?format=json" + "&p=" + page + "&n=20" + "&w=" + search);

            client.newBuilder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build();

            FormBody body1 = new FormBody.Builder()
                    .add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .add("Referer", "http://m.y.qq.com")
                    .add("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1")
                    .add("Connection", "keep-alive")
                    .build();


            final Request request = new Request.Builder()
                    .post(body1)
                    .url(u)
                    .get()
                    .build();

            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d(TAG, "Failed");
                    context.sendBroadcast(new Intent("Search Failed"));

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response)
                        throws IOException {
                    if (response.isSuccessful()) {
                        final String result = Objects.requireNonNull(response.body()).string();
                        Log.d(TAG, "onResponse: "+result);
                        getMusicInfo(context, search, result, startActivity, arrayList);
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 获取歌曲ID和MID等信息并存储到ArrayList中
     *
     * @param context      context
     * @param search       要搜索的关键字
     * @param result       搜索返回的结果
     * @param jumpActivity 是否转跳Activity
     * @param arrayList    将结果储存在arrayList中
     */
    private static void getMusicInfo(Context context, String search, String result,
                                     boolean jumpActivity, ArrayList<Music> arrayList) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject song = data.getJSONObject("song");
            JSONArray list = song.getJSONArray("list");

            for (int i = 0; i < list.length(); i++) {
                JSONObject object = list.getJSONObject(i);
                JSONArray singerList = object.getJSONArray("singer");

                Music music = new Music();
                music.setAlbum(object.getString("albumname") != null ?
                        String.valueOf(Html.fromHtml(object.getString("albumname"))) : "");

                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < singerList.length(); j++) {
                    JSONObject singerObject = singerList.getJSONObject(j);
                    sb.append(singerObject.getString("name")).append("/");
                }
                String singer = sb.toString();
                if (singer.endsWith("/")) {
                    singer = singer.substring(0, singer.length() - 1);
                }

                String albumArtUrl = String.format(ALBUMARTURL,
                        URLEncoder.encode(object.getString("albummid"), "UTF-8"));

                music.setAlbumArtUrl(albumArtUrl);

                music.setArtist(String.valueOf(Html.fromHtml(singer)));

                music.setTitle(object.getString("songname") != null ?
                        String.valueOf(Html.fromHtml(object.getString("songname"))) : "");

                music.setSongID(object.getInt("songid"));

                music.setUrl(object.getString("songmid"));

                long ogg = object.getLong("sizeogg");
                if (ogg != 0)
                    music.setOggSize(
                            String.valueOf(ogg / 1024.0 / 1024).substring(0, 4));
                else
                    music.setOggSize("0");
                long mp3Low = object.getLong("size128");
                if (mp3Low != 0)
                    music.setMp3LowSize(
                            String.valueOf(object.getLong("size128") / 1024.0 / 1024).substring(0, 4));
                else
                    music.setMp3LowSize("0");
                long mp3 = object.getLong("size320");
                if (mp3 != 0)
                    music.setMp3Size(
                            String.valueOf(object.getLong("size320") / 1024.0 / 1024).substring(0, 4));
                else
                    music.setMp3Size("0");

                arrayList.add(music);
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            context.sendBroadcast(new Intent("Search Failed"));
        }

        if (jumpActivity) {
            Intent intent = new Intent("Jump Activity");
            intent.putExtra("search", search);
            context.sendBroadcast(intent);
        } else {
            context.sendBroadcast(new Intent("ArrayList Is Ready"));
        }
    }


    /**
     * 下载歌曲文件
     */
    private void downloadMusic(String songName, String url, byte[] albumArtBytes) {

        DownloadManager.Request request = null;
        if (songName.contains(".ogg")) {
            request = new DownloadManager.Request(
                    Uri.parse(url));
        } else if (songName.contains(".mp3")) {
            request = new DownloadManager.Request(
                    Uri.parse(url));
        }

        if (request != null) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setTitle(songName);
            request.setDestinationInExternalPublicDir(
                    MainActivity.FILE_DIR,
                    songName);

            DownloadManager downloadManager =
                    (DownloadManager) getApplicationContext()
                            .getSystemService(Context.DOWNLOAD_SERVICE);

            if (downloadManager != null) {
                long id = downloadManager.enqueue(request);

                count++;
                songNameList.add(songName);
                musicBytesList.add(albumArtBytes);
                idList.add(id);

                Toast.makeText(this, R.string.add_to_download_enqueue, Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }

    private class JumpReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case "Jump Activity":
                        Intent jumpIntent = new Intent(MainActivity.this,
                                MusicListActivity.class);
                        jumpIntent.putExtra("List", musicArrayList);
                        jumpIntent.putExtra("search", intent.getStringExtra("search"));
                        dialog.dismiss();
                        startActivity(jumpIntent);
                        musicArrayList.clear();
                        break;
                    case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,
                                -1L);
                        int num = idList.indexOf(id);
                        if (num >= 0) {
                            if (songNameList.get(num).contains(".ogg")) {
                                Toast.makeText(getApplicationContext()
                                        , R.string.download_complete, Toast.LENGTH_SHORT)
                                        .show();
                            } else if (songNameList.get(num).contains(".mp3")) {
                                embedAlbumArt(num);
                            }
                            count--;
                            Vibrator vibrator = (Vibrator) getApplicationContext()
                                    .getSystemService(Context.VIBRATOR_SERVICE);

                            if (vibrator != null)
                                vibrator.vibrate(200);

                        }
                        break;
                    case "Download Music":
                        downloadMusic(intent.getStringExtra("Song Name"),
                                intent.getStringExtra("Url"),
                                intent.getByteArrayExtra("Image Data"));
                        break;
                    case "Search Failed":
                        Toast.makeText(MainActivity.this,R.string.error, Toast.LENGTH_SHORT).show();
                        if (dialog.isShowing())
                            dialog.dismiss();
                        break;
                }
            }
        }
    }


    /**
     * MP3嵌入专辑封面
     *
     * @param num 通过songNameList和musicBytesList得到相应的文件名和封面图片数据
     */
    private void embedAlbumArt(int num) {
        File dir = new File(LOCAL_PATH, FILE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, songNameList.get(num));
        try {
            Mp3File mp3File = new Mp3File(file);
            ID3v2 id3v2Tag;
            if (mp3File.hasId3v2Tag()) {
                id3v2Tag = mp3File.getId3v2Tag();
            } else {
                id3v2Tag = new ID3v24Tag();
            }

            id3v2Tag.clearAlbumImage();
            id3v2Tag.setAlbumImage(musicBytesList.get(num), "image/jpg");

            mp3File.setId3v2Tag(id3v2Tag);

            mp3File.save(mp3File.getFilename() + "_1");
            new File(mp3File.getFilename()).delete();
            new File(mp3File.getFilename() + "_1")
                    .renameTo(new File(mp3File.getFilename()));

            Toast.makeText(getApplicationContext()
                    , R.string.download_complete, Toast.LENGTH_SHORT)
                    .show();

        } catch (IOException | UnsupportedTagException |
                InvalidDataException | NotSupportedException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && count > 0) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keyCode, event);
    }
}