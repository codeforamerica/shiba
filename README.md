# SHIBA - Securely Hosted Integrated Benefits Application

## Development setup

Install the following system dependencies:

- Java 14 Development Kit: `brew cask install java`
- Gradle build tool version 6.3: `brew install gradle`

Setup live reload:

- Install the [live reload chrome extension](https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei?hl=en)

Setup IntelliJ for the project:

- Configure an additional dictionary in `preferences > editor > proofreading > spelling` using `.idea/dictionaries/shiba.xml`
- Install the Lombok plugin
- Enable annotation processing
- Set the Project SDK to Java 14 in `File > Project Structure`
- Run the application using ShibaApplication run configuration

Test:

From the project directory invoke
```./gradlew clean test```