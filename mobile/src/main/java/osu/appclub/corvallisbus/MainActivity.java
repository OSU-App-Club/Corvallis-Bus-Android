package osu.appclub.corvallisbus;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setup initial UI and Toolbar setup
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get our Fragment Manager and begin fragment transaction
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //Load the Main Fragment
        MainFragment mainFragment = new MainFragment();
        ft.replace(R.id.content_frame, mainFragment);
        ft.commit();


        //Current FAB action -- This may be used when adding favorites?
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "FAB Action is Fab!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        //Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        //Handle the navigation drawer when user presses the back button
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the options menu along the action bar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar item clicks
        /*Remember: Specify a parent activity in the manifest so Android
        *           handles Home/Up button clicks automagically*/
        int id = item.getItemId();

        //Setting action
        if (id == R.id.action_settings) {
            //We would launch the SettingActivity here, or something...
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Switch the current view based on the selected item
        displayView(item.getItemId());

        //Close drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void displayView(int id) {
        Fragment fragment = null;

        //Main Nav Item
        if (id == R.id.nav_main) {
            //Create new fragment with args
            fragment = new MainFragment();
            Bundle args = new Bundle();
            //args.put...
            fragment.setArguments(args);

            //Get fragment manager and start transaction
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            //Replace current fragment
            ft.replace(R.id.content_frame, fragment);
            ft.commit();

        }

        else if (id == R.id.nav_item1) {
            //Handle action here...
        }

        else if (id == R.id.nav_item2) {
            //Handle action here...
        }

        else if (id == R.id.nav_settings) {
            //Handle action here...
        }
    }
}
