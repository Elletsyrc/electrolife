# electrolife
An android application philippine based calculator app

# What is the app?
The app is called ElectroLife. It is a native Android application built using Java and modern Android architecture principles (MVVM, Room Database, WorkManager).

# What is it for?
ElectroLife acts as a personal pocket energy assistant. It is designed to help users track, manage, and optimize their daily electricity consumption by monitoring the usage of individual home appliances.

# What is the problem solved?
Many households struggle with surprisingly high electricity bills and a lack of visibility into what exactly is driving those costs. People often don't know how much it actually costs to run a specific appliance (like an air conditioner or a space heater) for a certain number of hours.

ElectroLife solves this problem by eliminating the guesswork. It translates raw usage hours into clear estimated costs and energy metrics (kWh). By doing so, it empowers users to:

Identify power-hungry appliances.
Reduce their monthly energy bills.
Lower their overall carbon footprint through smarter energy habits.

# Core Features
*Appliance Energy Calculator*: Users can add their home appliances to a dashboard, input usage hours, and calculate daily/monthly energy consumption (kWh) alongside accurate cost estimates based on tariff rates.
*Built-in Appliance Library*: To make onboarding easy, the app includes a comprehensive library of common household appliances to quickly add to the user's tracking list.
*Historical Tracking*: The app automatically logs energy usage and spending history, allowing users to visualize their habits month-by-month.
*Smart Recommendations & Tips*: Based on the user's tracked appliances, the app provides actionable, personalized advice on how to cut down energy waste.
*Background Monitoring & Alerts*: Utilizing Android's WorkManager, the app monitors usage in the background and sends helpful push notifications (for example, reminding users to turn off high-consumption devices).
*Offline-First Architecture*: All user data, appliance history, and settings are stored securely on the device using a local Room Database, meaning the app functions perfectly without an internet connection.
