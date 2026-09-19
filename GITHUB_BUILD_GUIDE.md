# Web Browser (Android Native) - GitHub CI/CD & Build Guide

This Android app is built with **Native Kotlin & Jetpack Compose** using **Gradle** (no Flutter required).

## 🚀 Automatic GitHub Build & Release (Already Configured!)

A complete GitHub Actions workflow has been added in [`.github/workflows/build-and-release.yml`](.github/workflows/build-and-release.yml).

### 1. What happens when you push to GitHub:
- **On every push** to `main` or `master`:
  - Automatically installs JDK 21 and Android SDK.
  - Prepares the Gradle environment and builds both `Debug APK` and `Release APK`.
  - Uploads the APKs directly to GitHub Actions **Artifacts** where you can download them immediately.

### 2. How to get a downloadable GitHub Release version:
- **Create a Git Tag** and push it:
  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```
  GitHub Actions will automatically generate a **GitHub Release** with direct download links for:
  - `web-browser-release.apk`
  - `web-browser-debug.apk`

- **Manual Trigger**:
  You can also go to your GitHub repository -> **Actions** tab -> **"Build & Release Android APK"** -> click **"Run workflow"**.

---

## 🔑 Signing Your Release APK (Optional)
By default, the workflow automatically signs release APKs using the provided keystore fallback so builds never fail. If you have your own upload keystore:
1. Encode your keystore: `base64 -w 0 my-upload-key.jks`
2. In your GitHub repository, go to **Settings > Secrets and variables > Actions > New repository secret**.
3. Add:
   - `KEYSTORE_BASE64`: The base64 output of your keystore file.
   - `STORE_PASSWORD`: Your keystore password.
   - `KEY_PASSWORD`: Your private key password.
   - `KEY_ALIAS`: Your key alias (e.g., `upload`).
