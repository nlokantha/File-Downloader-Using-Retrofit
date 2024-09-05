package com.c3labs.retroftfiledownloader;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "demo";
    ProgressBar progressBar;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    ArrayList<String> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);

//        itemList.add("Ombuddsman_Sin.jpg");
//        itemList.add("Ombuddsman_Eng.jpg");
//        itemList.add("Ombuddsman_Tam.jpg");
//        itemList.add("DEBIT_CARDS-SIN.mp4");
//        itemList.add("e_PASSBOOK-DEMO.mp4");

        for (int i = 0; i < itemList.size(); i++) {
            downloadFile("Paste your URl Here"+itemList.get(i), itemList.get(i));
        }
    }

    private void downloadFile(String fileUrl,String fileName){
        DownloadService downloadService = RetrofitClient.getRetrofitInstance().create(DownloadService.class);
        Call<ResponseBody> call = downloadService.downloadFile(fileUrl);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    executorService.execute(()->{
                        boolean success = saveFile(fileName,response.body());
                        runOnUiThread(()->{
                            if (success){
                                Toast.makeText(MainActivity.this, "File Downloaded!!!!!", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(MainActivity.this, "Failed to Save File!!!!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                }else {
                    runOnUiThread(()->{
                        Toast.makeText(MainActivity.this, "Failed to Download!!!!!", Toast.LENGTH_SHORT).show();
                    });
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error!!!!!!!!", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private boolean saveFile(String fileName,ResponseBody body){
        try {
            String path = Method.createOrGetDirectory().getAbsolutePath() + File.separator + fileName;

            File file = new File(path);
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true){
                    int read = inputStream.read(fileReader);
                    if (read == -1){
                        break;
                    }
                    outputStream.write(fileReader,0,read);
                    fileSizeDownloaded += read;

                    final int progress = (int) (fileSizeDownloaded * 100 / fileSize);
                    progressBar.setProgress(progress);

                    Log.d(TAG, "File Download: "+fileSizeDownloaded + " of "+fileSize);
                }

                outputStream.flush();
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }finally {
                if (inputStream != null){
                    inputStream.close();
                }
                if (outputStream != null){
                    outputStream.close();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
}