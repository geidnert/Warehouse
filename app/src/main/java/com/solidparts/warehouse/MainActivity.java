package com.solidparts.warehouse;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.solidparts.warehouse.service.ItemService;


public class MainActivity extends ActionBarActivity {
    private ItemService itemService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        itemService = new ItemService(this);

        //Sync data from local database to online database
        String[] args = new String[]{"syncToOnlineDb"};
        ItemSyncTask itemSyncTask = new ItemSyncTask();
        itemSyncTask.execute(args);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            //Sync data from database
            String[] args = new String[]{"fromOnlineDb"};
            ItemSyncTask itemSyncTask = new ItemSyncTask();
            itemSyncTask.execute(args);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddItem(View view) {
        //setContentView(R.layout.activity_add);
        startActivity(new Intent(MainActivity.this, AddItemActivity.class));
    }

    public void onSearch(View view) {
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
    }

    // --------------------------------------------------------------------

    class ItemSyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... searchTerms) {
            try {
                if(searchTerms[0].equals("fromOnlineDb")){
                    itemService.syncFromOnlineDB();
                } else {
                    itemService.syncToOnlineDB();
                }

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        /**
         * This method will be called when doInBackground completes.
         * The paramter result is populated from the return values of doInBackground.
         * This method runs on the UI thread, and therefore can update UI components.
         */

        @Override
        protected void onPostExecute(Boolean bool) {
        }

        @Override
        protected void onPreExecute() {
        }
    }
}
