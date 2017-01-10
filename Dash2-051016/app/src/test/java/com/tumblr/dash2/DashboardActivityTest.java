package com.tumblr.dash2;

import android.os.Build;
import android.support.v7.widget.RecyclerView;

import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link DashboardActivity} class
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class DashboardActivityTest {

    @Test
    public void test_paginationOccurred_addedDataSuccessfully() throws Exception {
        DashboardActivity dashboardActivity = new DashboardActivity();

        dashboardActivity.mRecyclerView = new RecyclerView(RuntimeEnvironment.application);
        dashboardActivity.mRecyclerView.setAdapter(new DashboardAdapter());

        final TextPost post = new TextPost();
        post.setTitle("test1");
        post.setBody("test1 body");

        final TextPost post2 = new TextPost();
        post.setTitle("test2");
        post.setBody("test2 body");

        ArrayList<Post> postsList = new ArrayList<>();
        postsList.add(post);
        postsList.add(post2);
        dashboardActivity.onPagination(postsList);

        assertTrue(dashboardActivity.mRecyclerView.getAdapter().getItemCount() == postsList.size());
    }
}