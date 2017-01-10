# Dash2: Android Take Home Code Exercise

The starter project provides: 
- Jumblr gradle dependency (Reference: (https://github.com/tumblr/jumblr))
- `DashboardActivity` with `RecyclerView` + empty `DashboardAdapter`
- One sample unit test using `Robolectric` that tests the method `onPagination(List<Post> newPosts)` in the `DashboardActivity`

## The Project:
Your challenge, should you choose to accept it, is to recreate the Tumblr dashboard in Android. We’ve given you a zip with a simple Android project to get you started.
The recommended time to spend on this take home is approximately 4-5 hours (i.e. you shouldn’t be spending more than a day on this!)

## Requirements
The basic requirements for your assignment are as follows:
- It must be an Android Studio project.
- It should display a list of dashboard posts from the Tumblr API. At minimum, the posts should show the captions and images.
- It must render at least these two post types: Text & Photo (including “photosets” of multiple photos)
- Paginate to request more posts when reaching the end of the feed.
- Pull to refresh to get newer posts.

## Extra Credit
You won't have time to do all, but here's some ideas. You can also do extra credit not listed here.
- Play GIFs in photo posts
- Play videos in video posts
- Play audio in audio posts
- Work offline (use the data on disk)
- Add unit tests
- Have cool design
- Have cool animations
- Let me login

## Getting started
There's not much we need to help you with, but for the sake of ease, we've created some authentication mumbo jumbo to get up and running faster.
```
String consumerKey = "Omq1FerYKMWeZnlvrIH9Qy3r6YIbyVDPdkQSfU5obu8eJBnt5n";
String consumerSecret = "GHqE8rxq6r0IXCbBkj9NPR4ed0EIBqb8xP9k6PdulMuwsxJfyo";

String oAuthToken = "5AX3lj6EjPUVTbMOKvuMHBPb8M4NWZN5kerNXo4v7RYmzPKXCC";
String oAuthSecret = "KsvGQwzzuKM1fv7jMNdEhDqH1NwpJL7JB6AUoxxBEfweLKh6np";

// Create a new client
JumblrClient client = new JumblrClient(consumerKey, consumerSecret);
client.setToken(oAuthToken, oAuthSecret);
```

## FAQ
> Can I use third party libraries?

Yes! But please be judicious, we want to see how awesome you are, not how awesome you are at using libraries. For example, if you love front-end development then maybe don't use the support library CardView and make your own, see what I mean?

> Do I have to use Jumblr?

Nope, feel free to hit up the API directly. You can use the same tokens that we’ve provided, or you can get your own.

> Does it have to look exactly like the Tumblr Android app?

No way! If you have an interesting way of displaying the data that still fulfills the requirements, then let your imagination run wild!
