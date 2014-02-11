ChanExplorer
============

[chanexplorer.wakarimasen.co](http://chanexplorer.wakarimasen.co/)

ChanExplorer is an 4Chan browser for Android. It is designed to augment 4chan for mobile browsing, it has a similar purpose to 4Chan X. Currently ChanExplorer is no longer available on the Play store.

## Building
ChanExplorer was built with Eclispe and targets Gingerbread (API Level 10).

### Dependencies

+ [Android SDK](http://developer.android.com/sdk/index.html)
+ [Android NDK](http://developer.android.com/tools/sdk/ndk/index.html)
+ [Android v4 Compat](http://developer.android.com/tools/support-library/setup.html)
+ [Android v7 Compat](http://developer.android.com/tools/support-library/setup.html)
+ [ActionBarSherlock (4.1.0)](http://actionbarsherlock.com/)
+ [SlidingMenu (7343c3cdc734f1aaf373c3a0cca8ed45fca21e6a)](http://github.com/jfeinstein10/SlidingMenu)
+ [com.mindprod.Boyer](http://mindprod.com/precis/boyer.txt)
+ [Google Guava](https://code.google.com/p/guava-libraries/)

### Build (Eclispe + ADT)

Assuming you are already set up with Android:

1. Import ChanExplorer into your Workspace
2. Import ActionBarSherlock & SlidingMenu into your workspace
3. Right click the ChanExplorer project and select `Project Properties`
4. Select the `Java Build Path` menu item on the left, then select the `Libraries` tab
5. Click `Add JARs...`
6. Add `boyer.jar`, `android-support-v4.jar` and `guava-13.0.jar`
7. In the `Order and Export` tab, ensure `boyer.jar` and `guava-13.0.jar` are selected.
8. Select the `Android` menu item on the left.
9. Under `Library` press `Add`
10. Add the `ActionBarSherlock` and `SlidingMenu` projects.
11. Use `ndk-build` to build the native libraries. `cd {chanexplorer}/jni && ndk-build` on OSX and Linux.

After the following steps, you can simply export and APK, or run the project on your Android device. Open an issue if there are problems.

## Contributing

Simply submit a pull request to contribute. I would like to keep Gingerbread support as my Android smartphone does not support 4.0.

## License
MIT
