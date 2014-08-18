ChanExplorer
============

[chanexplorer.wakarimasen.co](http://chanexplorer.wakarimasen.co/)

ChanExplorer is an 4Chan browser for Android. It is designed to augment 4chan for mobile browsing, it has a similar purpose to 4Chan X. Currently ChanExplorer is no longer available on the Play store.

## Development
ChanExplorer development has been revived by [Proplex](https://github.com/Proplex). Follow the new repo and get the updated app @ [https://github.com/Proplex/Leaf](https://github.com/Proplex/Leaf)

## Installing
ChanExplorer is available on [F-Droid](https://f-droid.org/repository/browse/?fdid=co.wakarimasen.chanexplorer). Go to [F-Droid](https://f-droid.org/repository/browse/?fdid=co.wakarimasen.chanexplorer) ([https://f-droid.org/repository/browse/?fdid=co.wakarimasen.chanexplorer](https://f-droid.org/repository/browse/?fdid=co.wakarimasen.chanexplorer)) to download the latest APK. Alternatively, you can download the [F-Droid App](https://f-droid.org/), and install it from there which will also provide you with automated updates.

## Building
ChanExplorer was built with Eclispe and targets Gingerbread (API Level 10).

### Dependencies

+ [Android SDK](http://developer.android.com/sdk/index.html)
+ [Android NDK](http://developer.android.com/tools/sdk/ndk/index.html)
+ [Android v4 Compat](http://developer.android.com/tools/support-library/setup.html)
+ [Android v7 Compat](http://developer.android.com/tools/support-library/setup.html)
+ [ActionBarSherlock (4.1.0)](http://actionbarsherlock.com/)
+ [SlidingMenu (7343c3cdc734f1aaf373c3a0cca8ed45fca21e6a)](http://github.com/jfeinstein10/SlidingMenu)
+ [Google Guava](https://code.google.com/p/guava-libraries/)

### Build (Eclispe + ADT)

Assuming you are already set up with Android:

Eclipse Build Instructions

1. Clone ChanExplorer `git clone git@github.com:wakarimasenco/ChanExplorer.git`
1. Import ChanExplorer into your Workspace as “Existing Android Code into Workspace” (File > Import > Android > Existing Android Code into Workspace)
1. Git Clone `ActionBarSherlock@4.1.0` and `SlidingMenu@7343c3cdc73`. (ABS 4.1.0 can be downloaded here - [http://actionbarsherlock.com/download](http://actionbarsherlock.com/download), and for SlidingMenu you can run `git clone https://github.com/jfeinstein10/SlidingMenu && git checkout 7343c3cdc73`
1. In SlidingMenu, delete the file `/src/com/slidingmenu/lib/app/SlidingMapActivity.java`
1. In SlidingMenu, in the file `/src/com/slidingmenu/lib/app/SlidingFragmentActivity.java` replace `extends FragmentActivity implements SlidingActivityBase` with `extends com.actionbarsherlock.app.SherlockFragmentActivity implements SlidingActivityBase`
1. Import the two projects into Eclipse using the same method as 2.
1. Right Click the SlidingMenu project > Properties > Android > Library > Add
1. Add the ActionBarSherlock project
1. Right Click the ChanExplorer project > Properties > Android > Library > Add
1. Add both the ActionBarSherlock and ChanExplorer projects.
1. Use `ndk-build` to build the native libraries. `cd {chanexplorer}/jni && ndk-build` on OSX and Linux.

You can then plug in your Android device and build a debug apk.

After the following steps, you can simply export and APK, or run the project on your Android device. Open an issue if there are problems.

## Contributing

Simply submit a pull request to contribute. I would like to keep Gingerbread support as my Android smartphone does not support 4.0.

## License
MIT
