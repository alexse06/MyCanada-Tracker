# MyCanada Tracker 🇨🇦

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple) ![Compose](https://img.shields.io/badge/Jetpack%20Compose-Enabled-blue) ![AI](https://img.shields.io/badge/AI-Powered-orange) ![License](https://img.shields.io/badge/License-MIT-green)

A modern, AI-powered Android application to help applicants track their Canadian Immigration status (Express Entry, Citizenship, PR, etc.) with clarity and ease.

<p align="center">
  <img src="https://raw.githubusercontent.com/alexse06/MyCanada-Tracker/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="150" alt="App Icon"/>
</p>

## ⚠️ DISCLAIMER: USE AT YOUR OWN RISK

**THIS APPLICATION IS NOT AFFILIATED, ASSOCIATED, AUTHORIZED, ENDORSED BY, OR IN ANY WAY OFFICIALLY CONNECTED WITH IMMIGRATION, REFUGEES AND CITIZENSHIP CANADA (IRCC), THE GOVERNMENT OF CANADA, OR ANY OF ITS SUBSIDIARIES OR AFFILIATES.**

- This software is an **unofficial** tool built for educational and personal use purposes only.
- It retrieves data that is accessible to you via your own credentials.
- The developers assume **NO RESPONSIBILITY** for the accuracy, reliability, or completeness of the data displayed.
- **USE AT YOUR OWN RISK.** The developers shall not be liable for any damages or issues arising from the use of this software, including but not limited to missed deadlines, misunderstandings of status, or account issues.
- Always verify your official status on the [Official IRCC Website](https://www.canada.ca/en/immigration-refugees-citizenship.html).

---

## ✨ Features

- **📊 Visual Dashboard**: See your application status at a glance with beautiful progress bars and status cards.
- **🤖 AI Insights**: Powered by Google Gemini, get plain-English explanations for complex immigration codes and updates.
- **📱 Home Screen Widget**: Keep track of your status without even opening the app.
- **🔮 "Crystal Ball" Forecast**: Estimates your completion date based on real-time processing data (sourced publicly).
- **📶 Offline Mode**: Access your cached data even when you don't have internet access.
- **🇫🇷 Bilingual**: Full support for English and French.
- **🔔 Smart Notifications**: Get notified only when significant changes occur.

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

## �📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Made with ❤️ for fellow applicants.*
