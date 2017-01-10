/**
 * The first launch activity for Tumbleclone.
 *
 * Uses OAuth to allow the user to log into their tumblr account.
 * Then redirects to the main dashboard activity page
 *
 * Much of the OAuth code is based off the following:
 * http://stackoverflow.com/questions/25562436/android-tumblr-login-issue-jumblrexception-not-authorized/25569363
 */

package com.tumblr.dash2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

public class TumbleLoginActivity extends Activity {
    // the consumerkey and consumersecret tokens provided in the email
    private final String consumerKey = "Omq1FerYKMWeZnlvrIH9Qy3r6YIbyVDPdkQSfU5obu8eJBnt5n";
    private final String consumerSecret = "GHqE8rxq6r0IXCbBkj9NPR4ed0EIBqb8xP9k6PdulMuwsxJfyo";

    // used to generate the callback url when requesting OAuth authentication
    public static final String OAUTH_CALLBACK_SCHEME = "oauthflow-tumblr";
    public static final String OAUTH_CALLBACK_HOST = "callback";
    public static final String CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

    // important web strings needed for authorizing through the tumblr api
    private final String request_token_url = "https://www.tumblr.com/oauth/request_token";
    private final String auth_url = "https://www.tumblr.com/oauth/authorize";
    private final String access_token_url = "https://www.tumblr.com/oauth/access_token";


    private CommonsHttpOAuthConsumer consumer;
    private CommonsHttpOAuthProvider provider;
    private SharedPreferences preferences;
    private Uri uri;

    private ProgressDialog progressDialog;

    private Intent webIntent;

    /**
     * The primary function of this activity. Called on creation
     * Creates the layout and adds the button listener.
     * Then checks if the user has been redirected from the callback url
     *
     * Overrides the onCreate function of ACtivity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // populates the activity with this layout

        preferences = getSharedPreferences("tumblr", Context.MODE_PRIVATE);

        TextView loginTumblrBtn = (TextView) findViewById(R.id.login_button); // link to login
        loginTumblrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        provider = new CommonsHttpOAuthProvider(request_token_url, access_token_url, auth_url);

        uri = this.getIntent().getData(); // gets a reference to the previous intent, if any

        if (uri != null && uri.getScheme().equals(OAUTH_CALLBACK_SCHEME)) { // checks if the user has just come from the authentication site
            Thread thread = new Thread(new Runnable() { // creates a thread since networkrequests must be made asynchronously
                @Override
                public void run() {
                    System.out.println("got callback_scheme from uri");
                    try {
                        consumer.setTokenWithSecret(preferences.getString("requestToken", ""), preferences.getString("requestSecret", ""));

                        provider.setOAuth10a(true);
                        provider.retrieveAccessToken(consumer, uri.getQueryParameter(OAuth.OAUTH_VERIFIER));

                        consumer.setTokenWithSecret(consumer.getToken(), consumer.getTokenSecret());

                        // saves the retrieved token and tokenSecret to sharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("token", consumer.getToken());
                        editor.putString("token_secret", consumer.getTokenSecret());
                        editor.commit();

                        // starts the dashboardActivity and passes in the token and tokenSecret
                        Intent intent = new Intent(TumbleLoginActivity.this, DashboardActivity.class);
                        intent.putExtra("token", consumer.getToken());
                        intent.putExtra("secret", consumer.getTokenSecret());

                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the webintent to the login website when the user hits the login link
     */
    private void login() {
        progressDialog = ProgressDialog.show(this, "Loading", "Please Wait..."); // displays progress dialog while webpage is loading

        new Thread(new Runnable() { // creates a thread since networkrequests must be made asynchronously
            @Override
            public void run() {
                try {
                    String authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("requestToken", consumer.getToken());
                    editor.putString("requestSecret", consumer.getTokenSecret());
                    editor.commit();
                    webIntent = new Intent("android.intent.action.VIEW", Uri.parse(authUrl)); // loads the webintent
                    startActivity(webIntent); // transitions user over to the authentication site
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    /**
     * Used to dismiss the progress dialog once user has left the activity
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
