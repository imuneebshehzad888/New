# Aimen Birthday Surprise (Android, Java + XML)

A native Android birthday surprise app. Package: `com.muneeb.aimenbirthday`. Works fully offline, no permissions, no audio.

## 1. Upload to GitHub and build the APK

1. **Extract the ZIP.** You get a folder called `AimenBirthdaySurprise`. The `.github` folder is hidden on some systems. Turn on "Show hidden files" so you can see it.
2. **Create a repository:** on github.com tap **+ > New repository**, name it (for example `aimen-birthday`), choose **Private**, and create it. Do NOT add a README or .gitignore there.
3. **Upload the files:** on the new repo page click **uploading an existing file**. Open the extracted `AimenBirthdaySurprise` folder and drag everything *inside* it (including the `.github` folder, `app`, `gradle`, `build.gradle`, `settings.gradle`, `gradle.properties`, `README.md`, `.gitignore`) into the browser. If your browser will not drag hidden folders, create the file manually: **Add file > Create new file**, type `.github/workflows/android.yml` as the name and paste the contents of that file.
4. **Commit to the main branch:** scroll down, choose **Commit directly to the main branch**, press **Commit changes**.
5. **Open Actions:** click the **Actions** tab. A run called **Build Android Debug APK** starts automatically. If it does not, open the workflow in the left list and press **Run workflow**.
6. **Read the result:** a yellow circle means running. A **green check** means success. A **red cross** means failure: open the run, open the failed step and read the red text (see Troubleshooting).
7. **Download:** open the green run, scroll to **Artifacts**, click **Aimen-Birthday-Debug-APK**. A ZIP downloads. Extract it to get `app-debug.apk`.
8. **Install:** copy the APK to the Android phone and tap it. If Android asks, allow **Install unknown apps** for the app you opened it from (Files, Chrome, etc.), then continue.

The APK is a downloadable *build artifact* (a debug build for testing and sharing). It is not a public Play Store release. Artifacts are kept by GitHub for a limited time (90 days by default), so download and keep a copy.

Workflow file: `.github/workflows/android.yml`. APK path: `app/build/outputs/apk/debug/app-debug.apk`.

### About the Gradle wrapper
The genuine Gradle Wrapper JAR could not be obtained in the environment that made this project (no internet), and it was not faked. Instead the workflow installs the pinned Gradle 8.7 itself (`gradle/actions/setup-gradle` with `gradle-version: '8.7'`) and builds with `gradle --no-daemon assembleDebug`. No `gradlew` is needed. Optional: if you later want a wrapper, open the project in Android Studio (it offers to create one) and commit the new files.

## 2. Things you can change

| What | Where |
|---|---|
| Password | `app/src/main/java/com/muneeb/aimenbirthday/MainActivity.java`, the constant `SURPRISE_PASSWORD` (case-sensitive, spaces at the ends are ignored) |
| Recipient name, birthday message, button texts | `app/src/main/res/values/strings.xml` |
| Colors | `app/src/main/res/values/colors.xml` and the shapes in `res/drawable/` |
| Floating symbols | the `SYMBOLS` list in `FloatingDecorView.java` |

**Security note:** a password inside an APK is a "surprise lock", not real security. Anyone who unpacks the APK can potentially find it. Do not reuse a real password.

## 3. Troubleshooting

- **"gradlew: command not found":** the workflow does not use gradlew. Make sure `.github/workflows/android.yml` is the one from this project.
- **Workflow does not appear in Actions:** the file must be exactly `.github/workflows/android.yml` on the `main` branch. Check the folder names for typos.
- **YAML error ("Invalid workflow file"):** indentation must use spaces, never tabs. Re-copy the original file.
- **JDK or Gradle errors ("Unsupported class file", "requires Java 17"):** this project needs JDK 17, Gradle 8.7, Android Gradle Plugin 8.5.2. Do not change one without the others.
- **SDK or license errors:** re-run the job; the workflow installs the Android SDK and accepts licenses.
- **Branch is `master`, not `main`:** rename the branch to `main` or run the workflow manually with **Run workflow**.
- **Phone says "App not installed":** uninstall any older copy with the same package name, then install again.
