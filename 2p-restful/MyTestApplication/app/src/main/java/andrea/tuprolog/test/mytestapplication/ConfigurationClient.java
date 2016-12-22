package andrea.tuprolog.test.mytestapplication;

import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Andrea on 20/12/2016.
 */

public class ConfigurationClient {
    private String urlRootService = "";
    private String authToken = "";
    private OkHttpClient client = null;

    public ConfigurationClient() {
        client = new OkHttpClient();
    }

    public ConfigurationClient(String rootUrl) {
        urlRootService = rootUrl;
        client = new OkHttpClient();
    }

//    public String getInfo() throws IOException {
//        Request request = new Request.Builder()
//                .url(urlRootService).get().build();
//
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful())
//                    throw new IOException("Unexpected code " + response);
//                avvisi.setText
//            }
//        });
//
//        Response response = client.newCall(request).execute();
//        return response.body().string();
//    }


//    public String getInfo() throws IOException {
//        Request request = new Request.Builder()
//            .url(urlRootService).get().build();
//        Response response = client.newCall(request).execute();
//        return response.body().string();
//    }


    private String readStream(InputStream in) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();
        String line = null;
        while((line = rd.readLine()) != null)
        {
            builder.append(line);
        }
        rd.close();
        return builder.toString();
    }
}
