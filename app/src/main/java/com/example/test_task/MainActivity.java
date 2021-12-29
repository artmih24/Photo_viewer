package com.example.test_task;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    public Context mainActivity;
    private String content = "";
    private String linkToPictures = "https://dev-tasks.alef.im/task-m-001/list.php";
    private ArrayList<String> urls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        //Скачиваем и парсим данные с сайта
        DownloadTask task = new DownloadTask();
        try {
            content = task.execute(linkToPictures).get();
            getResources(content);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        ShowPicsTable();
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.contact_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // очищаем содержимое таблицы
                TableLayout tableOfPics = (TableLayout) findViewById(R.id.tableOfPics);
                tableOfPics.removeAllViewsInLayout();
                // очищаем наш список ссылок на картинки
                urls = new ArrayList<>();

                //Скачиваем и парсим данные с сайта
                DownloadTask task = new DownloadTask();
                try {
                    content = task.execute(linkToPictures).get();
                    getResources(content);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                ShowPicsTable();

                // прячем индикатор обновления
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.contact_swipe_refresh);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    protected void ShowPicsTable() {
        TableLayout tableOfPics = (TableLayout) findViewById(R.id.tableOfPics);
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        if (!isTablet()) {
            for (int i = 0; i < (Math.ceil(urls.size() / 2.0)); i++) {
                // если это смартфон, то вставляем ряд с двумя ImageView для смартфонов
                TableRow row = (TableRow) layoutInflater.inflate(R.layout.pic_row_phones, null);
                ImageView pic1 = (ImageView) row.findViewById(R.id.pic1),
                        pic2 = (ImageView) row.findViewById(R.id.pic2);
                SetBitmap(urls.get(i * 2), pic1);
                if (i < (Math.floor(urls.size() / 2.0) + 1))
                    // устанавливаем изображение только если существует
                    // (i * 2 + 1)-ый элемент в списке ссылок
                    SetBitmap(urls.get(i * 2 + 1), pic2);
                pic1.setMinimumHeight(pic1.getMeasuredWidth());
                pic2.setMinimumHeight(pic2.getMeasuredWidth());
                tableOfPics.addView(row);
            }
        } else {
            for (int i = 0; i < (Math.ceil(urls.size() / 3.0)); i++) {
                // если это планшет, то вставляем ряд с тремя ImageView для планшетов
                TableRow row = (TableRow) layoutInflater.inflate(R.layout.pic_row_tablets, null);
                ImageView pic1 = (ImageView) row.findViewById(R.id.pic1),
                        pic2 = (ImageView) row.findViewById(R.id.pic2),
                        pic3 = (ImageView) row.findViewById(R.id.pic3);
                SetBitmap(urls.get(i * 3), pic1);
                if (i < (Math.floor(urls.size() / 3.0) + 1))
                    // устанавливаем изображение только если существует
                    // (i * 3 + 1)-ый элемент в списке ссылок
                    SetBitmap(urls.get(i * 3 + 1), pic2);
                if (i < (Math.floor(urls.size() / 3.0) + 2))
                    // устанавливаем изображение только если существует
                    // (i * 3 + 2)-ый элемент в списке ссылок
                    SetBitmap(urls.get(i * 3 + 2), pic3);
                pic1.setMinimumHeight(pic1.getMeasuredWidth());
                pic2.setMinimumHeight(pic2.getMeasuredWidth());
                pic3.setMinimumHeight(pic3.getMeasuredWidth());
                tableOfPics.addView(row);
            }
        }
    }

    // определение типа устройства - планшет или смартфон
    protected boolean isTablet() {
        int width = GetScreenSize()[0],
                height = GetScreenSize()[1];
        // вычисление длины диагонали экрана
        double screenDiagonal = Math.sqrt(width * width + height * height);
        // возвращаем:
        // true - если диагональ экрана 7 дюймов или больше (т.е. это планшет)
        // false - если диагональ экрана меньше 7 дюймов (т.е. это смартфон)
        return (screenDiagonal >= 7.0);
    }

    // получение размеров экрана
    protected int[] GetScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        // вычисление ширины экрана
        int width = displayMetrics.widthPixels / displayMetrics.densityDpi;
        // вычисление длины экрана
        int height = displayMetrics.heightPixels / displayMetrics.densityDpi;
        // возвращаем ширину и длину
        return new int[]{width, height};
    }

    protected void SetBitmap(String url, ImageView imageView) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            ImageRequest imageRequest = new ImageRequest(
                    url, // ссылка на изображение
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            // если все нормально и нет никаких ошибок,
                            // установка изображения
                            imageView.setImageBitmap(response);
                            // запись ссылки на изображение
                            // для передачи в активность просмотра изображения
                            ((SquareImageView) imageView).SetImageURL(url);
                        }
                    },
                    0, // ширина изображения
                    0, // высота изображения
                    ImageView.ScaleType.CENTER_CROP, // режим масштабирования изображения
                    Bitmap.Config.RGB_565, // режим декодирования изображения
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // если есть ошибка
                            // получаем сообщение об ошибке
                            String errorMsg = error.toString();
                            if (!errorMsg.startsWith("com.android.volley.NoConnectionError")) {
                                // получаем код ошибки
                                int statusCode = error.networkResponse.statusCode;
                                if (statusCode == 301) {
                                    // если код ошибки == 301,
                                    // т.е. если ссылка перенаправляющая,
                                    // пробуем аналогичную ссылку, но через HTTPS
                                    String url1 = url.replace("http", "https");
                                    // пробуем заново с новой ссылкой
                                    SetBitmap(url1, imageView);
                                } else
                                    // в случае ошибки ставим картинку с битым изображением
                                    imageView.setImageResource(R.drawable.error_response);
                            } else
                                // в случае ошибки ставим картинку с битым изображением
                                imageView.setImageResource(R.drawable.error_response);
                            // imageView.setImageResource(R.drawable.error_response);
                            // Log.i("JSON error:", error.getMessage());
                            // error.printStackTrace();
                        }
                    }
            );
            // добавляем запрос в очередь запросов
            requestQueue.add(imageRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // парсинг полученных данных
    protected void getResources(String cont) {
        // ищем ссылки на изображения
        // начало и конец - двойные кавычки
        String start = "\"",
                finish = "\"";
        Pattern pattern = Pattern.compile(start + "(.*?)" + finish);
        String splitContent = cont;
        Matcher matcher = pattern.matcher(splitContent);
        while (matcher.find()) {
            // если найдена ссылка
            String[] splitContent2 = matcher.group(1).split(" ");
            String url = splitContent2[0];
            // добавление ссылки в список ссылок
            urls.add(url);
        }
    }

    // обработка нажатия на ImageView
    public void fullScreen(View view) {
        SquareImageView squareImageView = (SquareImageView) view;
        // получение ссылки на изображение
        String imageURL = squareImageView.GetImageURL();
        if (!imageURL.equals("")) {
            // если установлен не плейсхолдер, а изображение,
            // то передаем в активность ссылку на изображение
            Intent intent = new Intent(this.mainActivity, ImageViewer.class);
            intent.putExtra("imageURL", imageURL);
            // открываем активность просмотра изображения
            startActivity(intent);
        } else
            // если установлен плейсхолдер, то говорим, что изображение недоступно,
            // активность не открываем
            Toast.makeText(getApplicationContext(),"Изображение недоступно", Toast.LENGTH_SHORT).show();
    }

    // получение html-кода страницы
    private static class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            URL url = null;
            HttpsURLConnection urlConnection = null;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    result.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return result.toString();
        }
    }
}