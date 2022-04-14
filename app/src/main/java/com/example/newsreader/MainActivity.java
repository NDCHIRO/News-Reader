package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    String[] webText;
    ArrayList<String> best6TopicsIds;
    ArrayList<String> best6TopicsTitles;
    //static to call it in another activity
    static ArrayList<String> best6TopicsURLs;
    ArrayAdapter adapter;
    ListView listView;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //shared preferences to save the websites
        sharedPreferences = this.getSharedPreferences("com.example.newsreader",MODE_PRIVATE);
        //definitions
        listView = findViewById(R.id.listView);
        best6TopicsIds = new ArrayList<String>();
        best6TopicsTitles = new ArrayList<String>();
        best6TopicsURLs= new ArrayList<String>();

        //extract the data from sharedprefences if it exits
        try {
            //best6TopicsIds = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("ids",ObjectSerializer.serialize(new ArrayList<String>())));
            best6TopicsTitles = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("titles",ObjectSerializer.serialize(new ArrayList<String>())));
            best6TopicsURLs = (ArrayList<String>)ObjectSerializer.deserialize(sharedPreferences.getString("urls",ObjectSerializer.serialize(new ArrayList<String>())));
            //adapter.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //if the titles array is empty,that means there are no data saved so start execute the given url
        if(best6TopicsTitles.size() < 1) {
            //execute the url to then get and save the first best 6 topics id in best6TopicsIds
            executeURL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty", true);

            for (int i = 0; i < webText.length; i++)
                webText[i] = webText[i].replace(",", "");

            for (int i = 1; i < 7; i++) {
                if (!webText[i].equals("")) {
                    best6TopicsIds.add(webText[i]);
                    //get the title and url of the topic
                    getTitlesAndUrlsFromIds(best6TopicsIds.get(i - 1));
                }
            }
        }

        adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, best6TopicsTitles);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                // send the index of the clicked item
                intent.putExtra("names", i);
                startActivity(intent);
            }
        });
    }

    private void getTitlesAndUrlsFromIds(String id) {
        //get the json data from the url
        String text = executeURL("https://hacker-news.firebaseio.com/v0/item/"+id+".json?print=pretty",false);
        String title="";
        String url="";
        try
        {
            //extract the title and url from the json object
            JSONObject jsonObject = new JSONObject(text);
            title = jsonObject.getString("title");
            if(!title.equals(""))
                best6TopicsTitles.add(title);
            else
                Toast.makeText(getApplicationContext(),"could not find the title :(",Toast.LENGTH_SHORT).show();

            url = jsonObject.getString("url");
            if(!url.equals(""))
                best6TopicsURLs.add(url);
            else
                Toast.makeText(getApplicationContext(),"could not find the url :(",Toast.LENGTH_SHORT).show();
            //save the title and the url of each website
            sharedPreferences.edit().putString("titles",ObjectSerializer.serialize(best6TopicsTitles)).apply();
            sharedPreferences.edit().putString("urls",ObjectSerializer.serialize(best6TopicsURLs)).apply();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    //boolean split is just used to split the ids of the best topics from each other
    public String executeURL(String url,boolean split)
    {
        DownloadTask task = new  DownloadTask();
        String text="";
        try {
            text=task.execute(url).get();
            if (split == true) {
                webText = text.split("\\s+");       //split the text to array of strings contains ids
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return text;
    }

    class DownloadTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls) {
            URL url;
            String result="";
            try
            {
                url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int data = inputStreamReader.read();
                char current;
                while (data != -1)
                {
                    current = (char) data;
                    result += current;
                    data=inputStreamReader.read();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            Log.i("info", result);
            return result;
        }

    }


}

