# MyCanada Tracker 🇨🇦

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple) ![Compose](https://img.shields.io/badge/Jetpack%20Compose-Enabled-blue) ![AI](https://img.shields.io/badge/AI-Powered-orange) ![License](https://img.shields.io/badge/License-MIT-green)

A free, privacy-focused Android app to track your Canadian Immigration status. Built by a geek who was tired of refreshing the official website. 🤓🇨🇦

<p align="center">
  <img src="https://raw.githubusercontent.com/alexse06/MyCanada-Tracker/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="150" alt="App Icon"/>
</p>

## ⚠️ IMPORTANT DISCLAIMER

**This is a personal hobby project.**

*   **Unofficial**: This app is **NOT** affiliated with IRCC. It's just a tool I built for myself using AI.
*   **Use at your own risk**: It works great for me, but since I'm just figuring this out as I go, I can't guarantee it will work forever.
*   **Privacy**: The app runs locally on your phone. I don't see or store any of your data.
*   **Check Official Sources**: Always verify your status on the [Official IRCC Website](https://www.canada.ca/en/immigration-refugees-citizenship.html).

> **Note**: I specifically tested this for my **Permanent Residence (PR)** application. It should work for others, but your mileage may vary.

---

## ✨ Features

- **📊 Visual Dashboard**: See your application status at a glance with beautiful progress bars and status cards.
- **🤖 AI Insights**: Powered by Google Gemini, get plain-English explanations for complex immigration codes and updates.
- **📱 Home Screen Widget**: Keep track of your status without even opening the app.
- **🔮 "Crystal Ball" Forecast**: Estimates your completion date based on real-time processing data (sourced publicly).
- **📶 Offline Mode**: Access your cached data even when you don't have internet access.
- **🇫🇷 Bilingual**: Full support for English and French.
- **🔔 Smart Notifications**: The app monitors your status for you. Stop refreshing the website 100 times a day—we'll ping you if something changes.
- **⏱️ Auto-Check**: The app automatically checks for updates in the background (configurable: every 4, 12, or 24 hours).

## 📸 Screenshots

<p align="center">
  <img src="docs/images/screenshot_dashboard.png" width="250" alt="Dashboard Status"/>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="docs/images/screenshot_timeline.png" width="250" alt="Application Timeline"/>
</p>

## 🔒 Security

- **Your Data Stays With You**: This app runs locally on your device. It communicates directly with IRCC servers using your credentials.
- **Official Source**: Data is fetched directly from the official tracking portal: `https://ircc-tracker-suivi.apps.cic.gc.ca/`.
- **No Middleman Server**: We do not store, harvest, or transmit your personal data to any third-party analytics servers.
- **Open Source**: The code is open for review to ensure transparency.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Clean Architecture
- **Network**: Retrofit + OkHttp
- **AI**: Google Gemini Pro (Generative AI)
- **Local Storage**: SharedPreferences (Encrypted)
- **Background Work**: WorkManager

## 🚀 Getting Started

1.  Clone the repository.
2.  Open in Android Studio.
3.  Get a [Google Gemini API Key](https://aistudio.google.com/) (Required for AI features).
4.  Build and Run on your device.
5.  Enter your credentials and API Key in the settings.

## 📲 Installation

1.  **Download**: [Click here to download MyCanadaTracker-v1.0-debug.apk](release/MyCanadaTracker-v1.0-debug.apk?raw=true)
2.  **Transfer**: If you downloaded this on your computer, send it to your phone.
3.  **Install**:
    - Open the file on your phone.
    - If prompted, allow **"Install from Unknown Sources"**.
    - Click **Install**.
4.  **Enjoy**: Open "MyCanada Tracker" from your app drawer.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Made with ❤️ for fellow applicants.*
