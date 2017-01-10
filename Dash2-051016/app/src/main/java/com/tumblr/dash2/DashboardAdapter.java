/**
 * The custom adapter for the RecyclerView.
 * Used to populate it with content from the dashboardPosts arrayList from DashboardActivity
 *
 * Uses the inner class ViewHolder to keep track of some of the shared elements between all post types.
 * Then passes that ViewHolder object into onBindViewHolder.
 * Then populate it with the Post content from the specific postion in the dashboardPosts list
 *
 * Also contains several helper functions for content retrieval and output from the Post objects
 */

package com.tumblr.dash2;

import android.content.Context;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.MediaController;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.ChatPost;
import com.tumblr.jumblr.types.Dialogue;
import com.tumblr.jumblr.types.LinkPost;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.QuotePost;
import com.tumblr.jumblr.types.TextPost;
import com.tumblr.jumblr.types.VideoPost;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private List<Post> dashboardPosts; // stores a member reference for the dashboard posts

    private Context mContext; // stores the context for easy access

    //private JumblrClient client; // used for attempting to retrieve the blog avatar

    /**
     * Constructor for the class.
     *
     * @param context
     * @param posts The list of posts that the adapter will keep a reference to
     * @param jClient Unused currently
     */
    public DashboardAdapter(Context context, List<Post> posts, JumblrClient jClient) {
        mContext = context;
        dashboardPosts = posts;
        //client = jClient;
    }

    /**
     * the inner class used as a generic holder for the post object in the recyclerView
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView blogAuthor, notesCount;
        //ImageView avatar;
        LinearLayout content;

        public ViewHolder(View itemView) {
            super(itemView);

            blogAuthor = (TextView) itemView.findViewById(R.id.blog_author); // the blog the post is from
            notesCount = (TextView) itemView.findViewById(R.id.note_count); // number of notes on the post
            //avatar = (ImageView) itemView.findViewById(R.id.blog_avatar);
            content = (LinearLayout) itemView.findViewById(R.id.content_container);
        }
    }

    /**
     * Simple function to create and inflate the ViewHolder object.
     */
    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewHolder postHolder = new ViewHolder(inflater.inflate(R.layout.post_fragment, parent, false));

        return postHolder;
    }

    /**
     * Binds the dashboardPost object at specified position to the passed ViewHolder object
     * Detects which type of post the Post object is and retrieves content accordingly.
     * Then, programmatically adds content to the LinearLayout in the ViewHolder based on the postType
     *
     * @param holder The View to populate content with
     * @param position Position of the post object to use
     */
    @Override
    public void onBindViewHolder(DashboardAdapter.ViewHolder holder, final int position) {
        Post post = dashboardPosts.get(position); // gets the post from the referenced list

        // fills out the common elements in each post fragment.
        TextView noteCount = holder.notesCount;
        noteCount.setText(post.getNoteCount() + " notes");

        TextView blogAuthor = holder.blogAuthor;
        blogAuthor.setText(post.getBlogName());

        LinearLayout content = holder.content;
        content.removeAllViews();

        // unused code to try to retrieve the blogAvatar image
        // not working
        /*final ImageView avatar = holder.avatar;
        avatar.setImageResource(0);

        class BlogAvatarRequest extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {
                return client.blogAvatar(params[0]);
            }

            @Override
            protected void onPostExecute(String avatarurl) {
                Picasso.with(mContext).load(avatarurl).into(avatar);
            }
        }

        BlogAvatarRequest request = new BlogAvatarRequest();
        request.execute(post.getBlogName());*/

        String postType = post.getType(); // gets the post type
        if (postType.compareTo("photo") == 0) { // For a photo post
            PhotoPost photoPost = (PhotoPost) post;

            List<Photo> photos = photoPost.getPhotos(); // gets the photos in the post

            for (Photo photo : photos) {
                String url = photo.getOriginalSize().getUrl();

                if (url.substring(url.length() - 3, url.length()).compareTo("gif") == 0) { // checks if the photo is a gif
                    System.out.println(position + " " + url);
                    //Glide.with(mContext).load(url).asGif().error(R.drawable.placeholder).into(child);

                    WebView child = new WebView(mContext);// if photo is a gif, uses a webview to display it
                    WebSettings webSettings = child.getSettings();
                    webSettings.setJavaScriptEnabled(true);
                    webSettings.setAllowContentAccess(true);
                    webSettings.setAppCacheEnabled(true);

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    int width = displayMetrics.widthPixels - 10;
                    // this allows the program to adjust the width of the webview to fit the screen
                    String data="<html><head><title>Example</title><meta name=\"viewport\"\"content=\"width="+width+", initial-scale=0.65 \" /></head>";
                    data=data+"<body><center><img width=\""+width+"\" src=\""+url+"\" /></center></body></html>";
                    child.loadData(data, "text/html", null);

                    content.addView(child); // adds a WebView
                }
                else { // otherwise loads the photo into an ImageView which it then adds to the parent View
                    ImageView child = new ImageView(mContext);
                    // scales the image to fill the screen
                    child.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    child.setAdjustViewBounds(true);
                    child.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    child.setImageResource(R.drawable.placeholder);

                    Picasso.with(mContext).load(url).into(child);

                    content.addView(child); // adds an ImageView
                }
            }

            TextView caption = new TextView(mContext); // adds the caption for the photo
            caption.setLayoutParams(addMarginParams(0, 40, 0, 0));
            caption.setMovementMethod(LinkMovementMethod.getInstance()); // allows links in the caption to function
            caption.setText(Html.fromHtml(photoPost.getCaption()));
            content.addView(caption); // adds a TextView
        }
        else if (postType.compareTo("video") == 0) { // For a video post
            VideoPost videoPost = (VideoPost) post;

            if (videoPost.getPermalinkUrl() != null) { // checks if the video is from Youtube, Vimeo, or another external site
                // if so, uses a webview to display the video
                WebView child = new WebView(mContext);
                child.setInitialScale(1);
                child.setLayoutParams(new LinearLayout.LayoutParams(2400, LinearLayout.LayoutParams.WRAP_CONTENT));
                child.setWebChromeClient(new WebChromeClient());
                WebSettings webSettings = child.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setAllowContentAccess(true);
                webSettings.setAppCacheEnabled(true);
                // attempts to scale the video to fill the screen, not quite successful
                webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
                webSettings.setUseWideViewPort(true);
                webSettings.setLoadWithOverviewMode(true);
                child.loadData(videoPost.getVideos().get(1).getEmbedCode(), "text/html", "utf-8");
                content.addView(child); // adds a WebView
            }
            else { // otherwise uses a videoView
                //System.out.println(videoPost.getPermalinkUrl() + "\n" + getVideoURL(videoPost.getVideos().get(1).getEmbedCode()));
                String url = getVideoURL(videoPost.getVideos().get(1).getEmbedCode());

                final VideoView child = new VideoView(mContext);
                child.setVideoPath(url);

                child.setLayoutParams(new LinearLayout.LayoutParams(1000, 800));

                // adds a mediacontroller object to the videodisplay
                MediaController mediaController = new MediaController(mContext);
                mediaController.setAnchorView(child);
                child.setMediaController(mediaController);
                child.requestFocus();
                child.setZOrderOnTop(true);

                child.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        child.start();
                        //child.pause();
                    }
                });

                content.addView(child); // adds a VideoView
            }

            TextView caption = new TextView(mContext); // adds the caption for the video
            caption.setLayoutParams(addMarginParams(0, 40, 0, 0));
            caption.setMovementMethod(LinkMovementMethod.getInstance()); // allows links in the caption to function
            caption.setText(Html.fromHtml(videoPost.getCaption()));
            content.addView(caption); // adds a TextView
        }
        else if (postType.compareTo("text") == 0) { // For a text post
            TextPost textPost = (TextPost) post;

            String titleText = textPost.getTitle();
            if (titleText != null && titleText.indexOf("img src=\"", 0) != -1) { // checks if the text has an image inside
                content.addView(getWebViewforText(titleText)); // then used a webview
            }
            else { // otherwise uses a normal textView
                TextView textTitle = new TextView(mContext);
                textTitle.setText(titleText);
                textTitle.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
                textTitle.setLayoutParams(addMarginParams(0, 0, 0, 10));
                content.addView(textTitle); // adds a TextView
            }

            String bodyText = textPost.getBody();
            if (bodyText != null && bodyText.indexOf("img src=\"", 0) != -1) { // checks if the text has an image inside
                content.addView(getWebViewforText(bodyText)); // then uses a webview
            }
            else {
                TextView textBody = new TextView(mContext);
                textBody.setMovementMethod(LinkMovementMethod.getInstance());
                textBody.setText(Html.fromHtml(bodyText)); // allows links in the body to function
                content.addView(textBody); // otherwise adds a TextView
            }
        }
        else if (postType.compareTo("link") == 0) { // For a link post
            LinkPost linkPost = (LinkPost) post;

            TextView link = new TextView(mContext);
            link.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
            link.setClickable(true);
            link.setMovementMethod(LinkMovementMethod.getInstance());
            // creates the link using the url and the title
            String text = "<a href='" + linkPost.getLinkUrl() + "'>" + linkPost.getTitle() + "</a>";
            link.setText(Html.fromHtml(text));
            content.addView(link); // adds a TextView

            TextView description = new TextView(mContext);
            description.setText(Html.fromHtml(linkPost.getDescription()));
            content.addView(description); // adds a TextView
        }
        else if (postType.compareTo("quote") == 0) { // For a quote post
            QuotePost quotePost = (QuotePost) post;

            TextView quote = new TextView(mContext);
            quote.setTextSize(16);
            quote.setTypeface(null, Typeface.ITALIC);
            quote.setText(Html.fromHtml(quotePost.getText()));
            quote.setLayoutParams(addMarginParams(0, 0, 0, 10));
            content.addView(quote); // adds a TextView

            TextView source = new TextView(mContext);
            source.setText(Html.fromHtml(quotePost.getSource()));
            source.setMovementMethod(LinkMovementMethod.getInstance()); // allows links in the text to function
            content.addView(source); // adds a TextView
        }
        else if (postType.compareTo("chat") == 0) { // For a chat post
            ChatPost chatPost = (ChatPost) post;

            String title = chatPost.getTitle();
            if (title != null) {
                TextView textTitle = new TextView(mContext);
                textTitle.setText(chatPost.getTitle());
                textTitle.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
                textTitle.setLayoutParams(addMarginParams(0, 0, 0, 10));
                content.addView(textTitle); // adds a TextView
            }

            List<Dialogue> fullDialogue = chatPost.getDialogue(); // gets the chat dialogue
            for (Dialogue dialogue : fullDialogue) {
                TextView singleDialogue = new TextView(mContext);
                // does some text formatting
                String dialogueName = "<B>" + dialogue.getName() + ":</B>" + "  " + dialogue.getPhrase();
                singleDialogue.setText(Html.fromHtml(dialogueName));
                singleDialogue.setLayoutParams(addMarginParams(0, 0, 0, 30));
                singleDialogue.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
                singleDialogue.setTextSize(12);
                content.addView(singleDialogue); // adds a Textview
            }
        }
        else { // If post is not one of the above types
            TextView apologies = new TextView(mContext);
            apologies.setTextSize(20);
            apologies.setText("Sorry, but this post of type " + postType + " is not supported");
            content.addView(apologies); // adds a TextView
        }
    }

    /**
     * Needed for class to extend RecyclerView.adapter
     */
    @Override
    public int getItemCount() {
        return dashboardPosts.size();
    }

    /**
     * Helper method to add margins to a programmatically created
     */
    private LinearLayout.LayoutParams addMarginParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(left, top, right, bottom);

        return params;
    }

    /**
     * Helper method for parsing through VideoPost content and get the video url
     */
    private String getVideoURL(String stringToSearch) {
        int startLoc = stringToSearch.indexOf("src=\"");
        int endLoc = stringToSearch.indexOf("\"", startLoc+5);
        //System.out.println("called with " + stringToSearch + " " + startLoc);
        return stringToSearch.substring(startLoc+5, endLoc);
    }

    /**
     * Helper function to create a webview for a textbody if that text itself contains images or gifs.
     *
     * Sometimes used by the TextPost
     */
    private View getWebViewforText(String bodyText) {
        System.out.println(bodyText);
        WebView textBody = new WebView(mContext);
        WebSettings webSettings = textBody.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAppCacheEnabled(true);
        // attempts to scale the webview to fit the screen
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        textBody.loadDataWithBaseURL("", bodyText, "text/html", "UTF-8", "");
        return textBody;
    }

    /**
     * Called when there are new posts to be appended to the existing list of posts
     * @param newPosts the list of new posts (example: from paginating and loading more posts)
     */
    public void addPosts(final List<Post> newPosts){
        // TODO implement, or you can use your own method on how new posts are added to the list
    }
}
