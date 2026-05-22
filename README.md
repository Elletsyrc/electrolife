# ElectroLife

> Your personal pocket energy assistant.

## What is ElectroLife?
**ElectroLife** is a native Android application built using **Java** and modern Android architecture principles (MVVM, Room Database, WorkManager). It provides a seamless, offline-first experience for tracking home energy usage.

## What is it for?
The app is designed to help users **track, manage, and optimize** their daily electricity consumption. By monitoring the usage of individual home appliances, it translates raw tracking data into actionable insights, helping users understand exactly where their electricity is going.

---

## The Problem & Solution

**The Problem:** Many households struggle with surprisingly high electricity bills and a complete lack of visibility into *what* exactly is driving those costs. People often don't know how much it actually costs to run a specific appliance (like an air conditioner or a space heater) for a specific number of hours. 

**The Solution:** ElectroLife eliminates the guesswork. It translates raw usage hours into clear estimated costs and energy metrics (`kWh`). By doing so, it empowers users to:
* **Identify** power-hungry appliances.
* **Reduce** their monthly energy bills.
* **Lower** their overall carbon footprint through smarter energy habits.

---

## Core Features

* **Appliance Energy Calculator:** Add home appliances to your dashboard, input usage hours, and calculate daily/monthly energy consumption (`kWh`) alongside accurate cost estimates based on tariff rates.
  
* **Built-in Appliance Library:** To make onboarding frictionless, the app includes a comprehensive library of common household appliances to quickly add to your tracking list.
  
* **Historical Tracking:** Automatically log energy usage and spending history, allowing you to visualize habits month-by-month.
  
* **Smart Recommendations & Tips:** Based on your tracked appliances, the app provides actionable, personalized advice on how to cut down energy waste.
  
* **Background Monitoring & Alerts:** Utilizing Android's `WorkManager`, the app monitors usage in the background and sends helpful push notifications (e.g., reminding you to turn off high-consumption devices).
  
* **Offline-First Architecture:** All user data, appliance history, and settings are stored securely on the device using a local `Room` Database. The app functions perfectly without an internet connection.

## Prerequisites
* Android Studio (Jellyfish or newer recommended).
* Android SDK API 34+ installed.
* An Android Emulator or a physical Android device (API 24+).

## Installation & Run
Clone the repository (or extract the source code folder):

Bash
git clone [https://github.com/your-username/ElectroLife.git](https://github.com/your-username/ElectroLife.git)
cd ElectroLife

Open in Android Studio:
* Launch Android Studio.
* Select File > Open and choose the ElectroLife root folder.

Sync Gradle:
* Wait for Android Studio to index the files and download the required Gradle dependencies. If prompted, click Sync Project with Gradle Files.

Build and Run:
* Connect your physical device via USB/Wi-Fi debugging, or launch an AVD (Android Virtual Device).

* Click the green Run (Play) button in the top toolbar or press Shift + F10.

## Configuration & Tweaks
* Tariff Rates: You can adjust the default electricity rate (Price per kWh) inside the Settings tab of the app or modify the default constants in CalculationsUtil.java.

* Notifications: To test background notifications locally, you can use the Android Studio App Inspection tool to manually trigger the EnergyMonitorWorker.

📜 License
This project is developed by Crystelle and is provided for educational and personal tracking purposes.
