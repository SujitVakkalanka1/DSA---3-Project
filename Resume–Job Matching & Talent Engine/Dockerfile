# Use an official OpenJDK 17 runtime as a parent image
FROM eclipse-temurin:17-jdk-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . /app

# Create bin directory and compile the Java application
RUN mkdir -p bin && javac -encoding UTF-8 -d bin src/model/*.java src/dsa/*.java src/storage/*.java src/server/*.java src/Main.java

# Expose port 8080 (the port your app runs on)
EXPOSE 8080

# Command to run the application
CMD ["java", "-cp", "bin", "Main"]
