# EzCarRent

## How to run
1. Download [IntelliJ IDEA](https://www.jetbrains.com/idea/download/?section=windows) 
2. Open the project folder in IntelliJ IDEA
3. Wait until all the components have been loaded (Eventually perform Sync Gradle by going to the related build.gradle.kts file in a module and pressing the _sync_ button)
4. Open a terminal (using [Git Bash](https://git-scm.com/downloads)) in the project's main folder
5. Run `chmod +x init-multiple-databases.sh`
6. Run `dos2unix init-multiple-databases.sh`   
7. Go back and run all the services in the [compose.yaml](compose.yaml) file: postgres, kafka, kafka-connect, setup-connector, kafka-ui, keycloak
8. Run all modules through the Run menu of IntelliJ: APIGateway, PaymentService, ReservationService, UserManagementService
9. Go inside the ReservationFrontend folder and open a terminal
10. Run `npm i`
11. Run `npm run build`
12. Go to http://localhost:8084/
13. Enjoy the app :)

## User Credentials

- **Luigi Caliò**
    - Username: `luigi`
    - Password: `luigipw`
    - Role: `Manager`

- **Laura Verdi**
    - Username: `laura`
    - Password: `laurapw`
    - Role: `Customer`

- **Staff Staff**
    - Username: `staff`
    - Password: `staffpw`
    - Role: `Staff`

- **Fleet Manager**
    - Username: `fleet_manager`
    - Password: `fleetpw`
    - Role: `Fleet Manager`

## Keycloak Credentials
- **Username**: `admin`
- **Password**: `admin`

## Notes

#### 1. Currently, we have implemented the registration process only for new customers. As a result, the “Add New Employee” function in the frontend works locally and does not affect Keycloak, so it does not create a real account for the employee.

#### 2. The payment with PayPal has been disabled to avoid exposing the _paypal-client-id_ and _paypal-client-secret_ on GitHub  
