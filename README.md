## Back-to-Office-app

Software which can read endpoints to register, enter, get status update and exit employees using a card reader system. It also provides visual representation of employee reservations.

It's a system that can be implemented to aid employee count in an office.

This project can serve as a skeleton code for any project which requires the need to serially communicate with a card reading API, for example public transport or public building head count.

#### What does this package do?

- Passenger count based on entry and exit 

- Validation for entries and exits

- Register a place on the waiting list

- Status update of employees on the waiting list

- Delete reservation for employee

- Get image that represents daily office layout

  

## Docker Container  ##

The `docker` directory contains the `Dockerfile`  that we used to build and run the app. The `docker-compose` which starts Kafka, Zookeper and the Spring app is in the `root` directory.

For image manipulation we use OpenCV.  ` jdk11-opencv.Dockerfile ` file uses a base Ubuntu image on which we install the JDK and required libraries, then compile the native C++ OpenCV libraries with Java bindings from source. The OpenCV image takes a long time to compile, so it's uploaded to Docker Hub. 

https://hub.docker.com/repository/docker/tlaura/reboarding

The Kafka and Zookeeper images are premade images from Docker Hub.

 The `docker-compose` provides a simple way to run these containers at the same time and provides networking between them. 

### How to run ###

Build the image :

``` docker-compose up --build -d ```

To see the logs for Spring or Kafka: 

``` docker logs -f [spring|kafka] ```

To stop the container:

``` docker-compose stop ```



# API

## Architecture

Java Spring, H2 db

### Folder structure

The project is divided into layers:

- Service layer - service package contains service classes with business logic
- Config - contains Kafka configurations
- DTO layer - dto package containing dto classes
- Repository - contains repository classes
- Entity layers
- Controller - controller class for endpoints 
- Util - contains utility classes, like DataInitializer that imports sample data for testing purposes to the database and helper classes to create the layout image

### Data models

Connection to the database using DTOs 

### Api endpoints

Implemented 8 RESTful endpoints for HTTP requests. They send and recieve messages in JSON format.

#### Reservation Layout for Employee:

- **URL**

  /api/employees/layout/:layoutPath

- **Method:**

  `GET`

- **URL Params**

  **Required:**

  `layoutPath=[string]`

- **Success Response:**

  - **Code:** 200 OK

- **Error Response:**

  - **Code:** 401 UNAUTHORIZED



#### Reservation Layout Path for Employee by Date:

- **URL**

  /api/employees/:employeeId

- **Method:**

  `GET`

- **URL Params**

  **Required:**

  `employeeId=[integer]`

- **Success Response:**

  - **Code:** 200 OK

- **Error Response:**

  - **Code:** 400 BAD_REQUEST

    

#### Get Reservation Layout:

- **URL**

  /api/layout

- **Method:**

  `GET`

- **Success Response:**

  - **Code:** 200 OK

    


#### Status:

- **URL**

  /api/status/:employeeId

- **Method:**

  `GET`

  **URL Params**

  **Required:**

  `employeeId=[integer]`

- **Success Response:**

  - **Code:** 200 OK

- **Error Response:**

  - **Code:** 404 NOT_FOUND

    

#### Enter:

- **URL**

  /api/entry/:employeeId

- **Method:**

  `POST`

  **URL Params**

  **Required:**

  `employeeId=[integer]`

- **Success Response:**

  - **Code:** 202 ACCEPTED

- **Error Response:**

  - **Code:** 401 UNAUTHORIZED

    

#### Exit:

- **URL**

  /api/exit/:employeeId

- **Method:**

  `POST`

- **URL Params**

  **Required:**

  `employeeId=[integer]`

- **Success Response:**

  - **Code:** 202 ACCEPTED

- **Error Response:**

  - **Code:** 401 UNAUTHORIZED

    


#### Register:

- **URL**

  /api/register

- **Method:**

  `POST`

- **Success Response:**

  - **Code:** 200 OK

    OR

  - **Code:** 201 CREATED

- **Error Response:**

  - **Code:** 403 FORBIDDEN

    


#### Delete:

- **URL**

  /api/delete

- **Method:**

  `DELETE`

- **Success Response:**

  - **Code:** 200 OK

- **Error Response:**

  - **Code:** 404 NOT_FOUND

  


# Methodology

We are a group of 3 developers using **Agile Scrum** methodology. 

To complete the project we had one sprint, and therefore one sprint planning session where we assigned tickets. 

Tickets were managed on our **Miro** board, and we also had pair programming sessions to avoid merge conflicts since features of the system are highly dependent on each other. 

After completion we had a retro session to identify improvements for our next sprint.

We used **BDD** as our software development approach.

# Testing

Based on BDD software development principles we wrote acceptance tests using **Cucumber** testing system.

Tested usage of the main endpoints.

We also included **Postman scripts** in the `test/resources` directory, the scripts will send postman requests to each endpoint in the application.

# Next steps / Roadmap

To be implemented in the future:

1.  Implement Frontend
