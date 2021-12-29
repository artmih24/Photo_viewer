package com.example.test_task;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

public class ImageViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);
        ImageView imageView = findViewById(R.id.imageView);
        String imageURL = getIntent().getExtras().getString("imageURL");
        SetBitmap(imageURL, imageView);
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
                        }
                    },
                    0, // ширина изображения
                    0, // высота изображения
                    ImageView.ScaleType.CENTER_CROP, // режим масштабирования изображения
                    Bitmap.Config.RGB_565, // режим декодирования изображения
                    new Response.ErrorListener() { // Error listener
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


}