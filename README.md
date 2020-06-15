## Back-to-Office-app

Software which can read endpoints to register, enter, get status update and exit employees using a card reader system.

It's a system that can be implemented to aid employee count in an office.

This project can serve as a skeleton code for any project which requires the need to serially communicate with a card reading API, for example public transport or public building head count.

#### What does this package do?

- Passenger count based on entry and exit 
- Validation for entries and exits
- Register a place on the waiting list
- Status update of employees on the waiting list

## API

## Architecture

Java Spring, H2 db

### Folder structure

The project is divided into layers:

- Service layer - service package contains service classes with business logic
- DTO layer - dto package containing dot classes
- Repository - contains repository 
- Entity layers
- Controller - controller class for endpoints 

### Data models

Connection to the database using DTOs 

### Api endpoints

Implemented 4 RESTful endpoints for HTTP requests. They send and recieve messages in JSON format

#### Enter:

- **URL**

  /api/entry/:employeeId

- **Method:**

  `POST`

- **Success Response:**

  - **Code:** 200

    

#### Exit:

- **URL**

  /api/exit/:employeeId

- **Method:**

  `POST`

- **Success Response:**

  - **Code:** 200

    

#### Status:

- **URL**

  /api/status/:id

- **Method:**

  `GET`

- **Success Response:**

  - **Code:** 200

    

#### Register:

- **URL**

  /api/register

- **Method:**

  `POST`

- **Success Response:**

  - **Code:** 200

    OR

  - **Code:** 201

- **Error Response:**

  - **Code:** 403 FORBIDDEN

    

# Methodology

We are a group of 3 developers using **Agile Scrum** methodology. 

To complete the project we had one sprint, and therefore one sprint planning session where we assigned tickets. 

Tickets were managed on our **Miro** board, and we also had pair programming sessions to avoid merge conflicts since features of the system are highly dependent on each other. 

After completion we had a retro session to identify improvements for our next sprint.

We used **BDD** as our software development approach.

# Testing

Based on BDD software development principles we wrote acceptance tests using **Cucumber** testing system.

Tested usage of the four main endpoints.



# Next steps / Roadmap

To be implemented in the future:

1. Implement Docker for scalability 
