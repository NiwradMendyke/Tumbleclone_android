/**
 * Central activity of Tumbleclone.
 * Creates the client using the token and the tokensecret from the LoginActivity.
 * Then creates an async activity where the user and the user's dashboard posts are retrieved.
 * Displays them with a Recycleview and an Adapter object.
 *
 * Also sets up a tab bar for post type filtering.
 * And allows for actions such as pull to refresh and pagination of more posts.
 */

package com.tumblr.dash2;

import android.os.AsyncTask;
import android.support.annotation.VisibleForTesting;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private JumblrClient client; // the main client used for the Jumblr library
    private User user; // the main user retrieved from the JumblrClient
    private List<Post> dashboardPosts; // keeps track of the current list of dashboard posts shown on screen
    private boolean isLoading; // boolean flag to keep track of whether the app is currently loading more posts or not

    private LinearLayoutManager linearLayoutManager;
    private DashboardAdapter dashboardAdapter;
    private SwipeRefreshLayout refresher;

    private ArrayList<Button> tabBar; // keeps track of the individual tabs in the tab bar
    private int currentPostType; // keeps track of which tab bar is currently selected
    private String searchValues[] = {"", "photo", "video", "text", "link", "quote", "chat"}; // used for filtering post type

    @VisibleForTesting
    /*package*/ RecyclerView mRecyclerView;

    /**
     * First function called when this activity loads.
     * Creates a Jumblrclient. And loads posts into the dashboardPosts arraylist.
     * Finally sets up the recyclerview, the tabbar, and the pull-to-refresh action
     *
     * Overrides the onCreate method from the Activity class
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a new client
        String consumerKey = "Omq1FerYKMWeZnlvrIH9Qy3r6YIbyVDPdkQSfU5obu8eJBnt5n";
        String consumerSecret = "GHqE8rxq6r0IXCbBkj9NPR4ed0EIBqb8xP9k6PdulMuwsxJfyo";

        client = new JumblrClient(consumerKey, consumerSecret); // creates a Jumblrclient
        client.setToken(getIntent().getStringExtra("token"), getIntent().getStringExtra("secret"));
        dashboardPosts = new ArrayList<Post>();
        // creates the adapter for the recyclerview
        dashboardAdapter = new DashboardAdapter(this, dashboardPosts, client); // passes in the dashboardPosts list
        isLoading = false;
        currentPostType = 0; // sets initial post type to all

        loadPosts(10, 0); // loads the 10 most recent posts from dashboard

        setContentView(R.layout.activity_dashboard); // populates the activity with this layout
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0); // removes shadow tint from actionbar
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        setUpRecyclerView();
        setUpTabBar();

        refresher = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() { // tells layout to listen for swipe-to-refresh action
            @Override
            public void onRefresh() {
                onPullToRefresh();
            }
        });
    }

    /**
     * Sets the layoutmanager for the recyclerview.
     * Modifies some cache settings.
     * Allows for pagination of more posts, and hides the action bar when the user scrolls down
     */
    private void setUpRecyclerView() {
        mRecyclerView.setLayoutManager(linearLayoutManager); // sets layoutmanager
        //mRecyclerView.setItemViewCacheSize(10);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.setAdapter(dashboardAdapter); // sets the adapter

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() { // allows view to detect scrolling
            int mLastFirstVisibleItem = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final int currentFirstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

                // if user reaches bottom of view, adds 10 more posts to the list
                if (linearLayoutManager.findLastVisibleItemPosition() >= (dashboardPosts.size() - 1)) {
                    if (!isLoading) {
                        isLoading = true;
                        loadPosts(10, dashboardPosts.size());
                    }
                }

                // if user is scrolling down, hides the actionbar
                if (currentFirstVisibleItem > this.mLastFirstVisibleItem) {
                    DashboardActivity.this.getSupportActionBar().hide();
                } // or shows the actionbar if the user is scrolling up
                else if (currentFirstVisibleItem < this.mLastFirstVisibleItem) {
                    DashboardActivity.this.getSupportActionBar().show();
                }

                this.mLastFirstVisibleItem = currentFirstVisibleItem;
            }
        });
    }

    /**
     * Sets up the tabbar at the top of the activity.
     * Initializes all the buttons and adds listeners to each.
     *
     * Also contains the inner class for the custom ClickListener
     */
    private void setUpTabBar() {
        tabBar = new ArrayList<>();
        // adds each button to the arraylist
        tabBar.add((Button) findViewById(R.id.all_posts));
        tabBar.add((Button) findViewById(R.id.photo_posts));
        tabBar.add((Button) findViewById(R.id.video_posts));
        tabBar.add((Button) findViewById(R.id.text_posts));
        tabBar.add((Button) findViewById(R.id.link_posts));
        tabBar.add((Button) findViewById(R.id.quote_posts));
        tabBar.add((Button) findViewById(R.id.chat_posts));

        class typeChoiceListener implements View.OnClickListener {
            int postTypePicked; // which is the new filtertype picked

            typeChoiceListener(int picked) {
                postTypePicked = picked;
            }

            @Override
            public void onClick(View v) {
                changeButtonColor(currentPostType, postTypePicked); // changes color of the new tab and the old tab
                //System.out.println("oldButton is " + currentPostType + " newButton is " + postTypePicked);

                currentPostType = postTypePicked; // sets the new filtertype
                loadPosts(10, 0); // reloads the list
            }
        }

        for (int i = 0; i < tabBar.size(); i++) { // adds the custom listener to each tab item
            tabBar.get(i).setOnClickListener(new typeChoiceListener(i));
        }
    }

    /**
     * Simple function to change the colors of the old tabitem and the newly selected tabitem
     *
     * @param oldType the index of the previous tabitem
     * @param newType the index of the new tabitem selected
     */
    private void changeButtonColor(int oldType, int newType) {
        View oldButton = tabBar.get(oldType);
        View newButton = tabBar.get(newType);

        oldButton.setBackgroundColor(getResources().getColor(R.color.tab_unselected));
        newButton.setBackgroundColor(getResources().getColor(R.color.tab_selected));
    }

    /**
     * Simple function to create the custom asynctask and make a post request
     *
     * @param numberOfPosts the number of the posts to retrieve
     * @param position where in dashboardPosts arraylist to add the posts
     */
    private void loadPosts(int numberOfPosts, int position) {
        System.out.println("loading more posts");
        final MakeNetworkRequests request = new MakeNetworkRequests(); // makes the asynctask object
        request.execute(numberOfPosts, position);
    }

    /**
     * Custom asynctask to make the network request for the dashboard posts.
     *
     * Uses the parameters from the loadPosts object to determine how many posts to load.
     * Uses the current post type to determine which type of posts to call for.
     * Then calls onPagination when the task executes and passes the new posts.
     */
    private class MakeNetworkRequests extends AsyncTask<Integer, Void, List<Post>> {
        @Override
        protected List<Post> doInBackground(Integer... params) {
            //System.out.println(clientTemp.user().getName());

            user = client.user();
            int limit = params[0];
            int offset = params[1];

            System.out.println(user.getName());

            Map<String, String> options = new HashMap<>(); // map defining the parameters for the jumblr request
            options.put("limit", "" + limit);
            options.put("offset", "" + offset);
            if (currentPostType != 0) { // if postType isn't "allposts" adds a type filter the options
                options.put("type", searchValues[currentPostType]);
            }
            if (offset == 0 && !dashboardPosts.isEmpty()) { // clears the dashboardPosts list if refreshing
                dashboardPosts.clear();
            }

            return client.userDashboard(options);
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            onPagination(posts);
        }
    }

    @VisibleForTesting
    /* package */void onPullToRefresh() {
        System.out.println("pulling to refresh");
        loadPosts(dashboardPosts.size(), 0); // gets the most recent 10 dashboard posts to replace the existing list
        refresher.setRefreshing(false);
    }

    @VisibleForTesting
    /* package */ void onPagination(final List<Post> newPosts) {
        System.out.println("paginating more posts");
        dashboardPosts.addAll(newPosts); // adds the new posts to the existing list
        dashboardAdapter.notifyDataSetChanged(); // alerts the adapter that the list has changed and needs to be reloaded
        isLoading = false;
    }
}

