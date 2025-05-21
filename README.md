📄 Project Report: Found404

📌 Project Title:
Found404 — Lost and Found Service for Students

📖 Project Description:
Found404 is a mobile Android application designed to help students find lost or found items within the premises of their educational institution. The project acts as a mini classifieds platform where users can post announcements about lost or found belongings, browse existing posts, and contact the finders or owners directly.

✨ Key Features:
* User registration and login system
* Adding posts about lost or found items
* Viewing a list of posts displayed as cards
* Searching posts by category or keywords
* Navigation between screens using fragments
* Smooth transition animations
* Data exchange through REST API using Retrofit
* Data management with Repository and ViewModel
* Modern, user-friendly interface built with Material Design principles

🛠 Technologies Used:
- Java / Kotlin
- Android Studio
- PostgreSQL
- Retrofit & GsonConverterFactory
- RecyclerView
- ViewModel & LiveData
- Navigation Components
- Material Components
- Backend API (local server)

📂 Project Structure:
- Fragments: For main screens — Home, Add Post, User Profile.
- ApiService & RetrofitInstance: For defining and initializing API calls.
- Repository: Acts as an intermediary between ViewModel and ApiService.
- Adapters: Custom RecyclerView adapters for displaying post lists.
- Animations: For smooth screen transitions and fade effects.

📸 Application Screenshots

![image](https://github.com/user-attachments/assets/671ae2fb-7017-4b7c-853b-7fcedae11cc9)  ![image](https://github.com/user-attachments/assets/fc6b4b48-1fe3-4341-a06c-5e7bb7a5caa3)

![image](https://github.com/user-attachments/assets/53dcecaf-d3d0-4e07-80cd-c94d5ab78741)  ![image](https://github.com/user-attachments/assets/d3876717-f5f0-4940-b5a3-63167ffb2e31)

![image](https://github.com/user-attachments/assets/288a8889-2e97-4131-9b2d-749d5588f3fc)  ![image](https://github.com/user-attachments/assets/a72df57f-a899-409b-a655-87a67aaa621e)


📌 Project Launch Instructions:

To successfully run the Found404 application, it is necessary to correctly configure and launch both the backend server and the mobile frontend. Since the application operates locally during development and testing, the correct IP address of the backend server must be specified in the frontend configuration.

📍 Prerequisites:

* Installed Android Studio
*Backend server source code
* Android Emulator or physical Android device connected to the same local network

🔧 Backend Setup:

* Open the backend project in your preferred IDE or terminal.
* Build and run the backend server according to its setup instructions.
* Find your current local IP address (e.g., 192.168.0.103).

Windows: ipconfig

macOS/Linux: ifconfig
* Confirm that the server is running and accessible via: http://<your_local_ip>:<port>


📱 Android (Frontend) Setup:

* Open the Found404 Android project in Android Studio.
* Locate the RetrofitInstance or API configuration file.
* Replace the base URL with your current local IP address and the backend port:

private static final String BASE_URL = "http://192.168.0.103:8080/";

* Build and run the Android application using an emulator or a physical device connected to the same Wi-Fi network.
* The application will successfully connect to the backend, allowing users to create and view posts.

📌 Important Notes:

- The Android device/emulator must be on the same Wi-Fi network as the backend server.
- Ensure that no firewall or antivirus software is blocking local connections on the specified port.
- If using an Android emulator, you can use 10.0.2.2 instead of the local IP address to access the host machine.

📌 Conclusion:

The Found404 project provides a practical and user-friendly solution for managing lost and found items within a student community. It demonstrates the integration of modern Android development practices, a clean application architecture using Repository and ViewModel patterns, and efficient data exchange with a backend server via Retrofit. The system is designed for easy scalability, and the modular structure ensures simple maintenance and future improvements.

