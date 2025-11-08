# Secure Remote File-System NAS Homeserver Backend
A secure Spring Boot application meant to mimic cloud storage services such as *Google Drive*, *icloud* and others  for file management on a Network Attached Storage. Remote accessibility of one's files is leveraged from the frontend hosted online and the backend at the comfort of one's home. Extreme privacy and security, no big coporation holding your data.  A homelab/server is recommended to proceed however testing can be done on any device with Windows🪟 or Linux🐧. I personally hosted the app on a Raspberry Pi 5 with 4 Harddrives of about 1TB each. Files are not restricted to specific file types eg *.pdf*, *.jpeg*, *. mp3*, *.txt*, *.mkv* etc. Files are automatically encrypted upon upload and decrpyted upon download for improved security and privacy.

## Requirements
- A Java Runtime
- MySQL Database
- 4GB RAM or more

## Guide
1. Clone the repository onto your local PC

``` 
git clone https://github.com/anzieri/piCloud.git
```

2. Assign appropriate values to the variables within .env file whilst following instructions within the comments.

3. Run the main method within the **PiCloudApplication.class** file. Kindly **follow instructions** within the comments.

## Making an Executable
After ensuring that the variables are set and the SpringApplication has run successfully, prepare for production environment by running maven package. The compilation will take a while so be patient.
```bash
mvn package
```
Upon successful packaging, a jar file will be available in your */target* project directory and will look something like **piCloud-0.0.1-SNAPSHOT.jar**. Copy and jar file as well as your .env file and place them wherever you wish to run your app. Remember the *directoryPath* variable in your env file must match the actual path you wish to store your files and directories in.
### Installation
1. For Linux

```bash
nohup java -jar piCloud-0.0.1-SNAPSHOT.jar > output.txt 2>&1 & 
```
Executes program in background and output logs and errors into *output.txt*.

2. For Windows

```bash
java -jar piCloud-0.0.1-SNAPSHOT.jar > output.txt
```
To verify whether its working, open the *output.txt* file. If you see something like *"Started PiCloudApplication in xx seconds"*, then the application is running successfully. You can now access the backend via *http://localhost:8080/* or *http://your-server-ip-address:8080/* if accessing remotely. To test the endpoints, you can use *Postman* with the following link to the collection: https://team-acethis.postman.co/workspace/My-Workspace~f558a4cc-b6de-46e0-88d9-e0d98898cb43/collection/36686099-9fa776b6-527f-4981-bc60-9589feccc945?action=share&creator=36686099&active-environment=36686099-1937cf22-271c-4efd-ba37-6192d17eca42
