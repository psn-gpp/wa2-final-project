export interface CarModel {
    id?: number;
    brand: string;
    model: string;
    modelYear: number;
    segment: string;
    doorsNo: number;
    seatingCapacity: number;
    luggageCapacity: number;
    category: string;
    manufacturer: string;
    costPerDay: number;
    motorDisplacement: number;
    airConditioning:boolean;
    engine:string;
    transmission:string;
    drivetrain:string;
    safetyFeatures:string[]
    infotainments:string[]
}

export interface AddCarModelResponse {
    location: string | null;
    data: CarModel | null;
}

export interface AddVehicleResponse {
    location: string | null;
    data: Vehicle | null;
}

export interface AddMaintenanceResponse {
    location: string | null;
    data: Maintenance | null;
}

export interface ModelFilters {
    brand?: string;
    model?: string;
    modelYear?: number;
    segment?: string;
    doorsNo?: number;
    seatingCapacity?: number;
    luggageCapacity?: number;
    category?: string;
    manufacturer?:string;
    costPerDay?: number;
    motorDisplacement?: number;
    airConditioning?:boolean;
    engine?:string;
    transmission?:string;
    drivetrain?:string;
    safetyFeatures?:string[]
    infotainments?:string[]
}

export interface VehicleFilters {
    availability?:string,
    licencePlate?:string,
    vin?: string,
    kilometers?: number,
    pendingCleaning?: boolean,
    pendingMaintenance?: boolean,
}

export interface MaintenanceFilters {
    defect?: string,
    completedMaintenance?: boolean,
    startDate?:string,
    endDate?:string
}

export interface Vehicle {
    id?: number;
    refCarModel:number;
    availability: string;
    licencePlate: string;
    vin: string;
    kilometers: number;
    pendingCleaning: boolean;
    pendingMaintenance: boolean;
}

export interface NoteFilters {
    author: string;
    startDate: string;
    endDate: string;
}

export interface Note {
    id: number,
    vehicleId: number,
    text: string,
    author: string,
    date: string
}

export interface Maintenance{
    id?: number,
    vehicleId: number,
    defect: string,
    completedMaintenance: boolean,
    date: string
    vehicleLicencePlate:string
}

export interface genericUserData{
    id?:number,
    name:string,
    surname:string,
    email:string,
    phone:string,
    address:string,
    city:string,
    keycloakId?:string,
}

export interface Customer{
    id?:number,
    genericUserData:genericUserData,
    dateOfBirth:string,
    reliabilityScores:number,
    drivingLicence:string,
    expirationDate:string,
}

export interface AddCustomerResponse{
    location:string|null
    data: Customer|null
}

export interface Role{
    id?:number,
    nameRole:string
}

export interface Employee{
    id?:number,
    genericUserData:genericUserData,
    role:Role,
    salary:number,
}

export interface AddEmployeeResponse{
    location:string|null
    data: Employee|null
}

export interface Status{
    id?:number,
    status: string,
}

export interface Reservation{
    id?:number,
    customerId:number,
    startDate:string,
    endDate:string,
    reservationDate:string,
    employeeId?:number,
    status:Status,
    vehicleId:number,
    paymentAmount:number,
    version: number,
}

export interface AddReservationResponse{
    location: string | null;
    data: Reservation | null;
}

export interface ReservationFilters{
    id?:number,
    userId?:number,
    startDate?:string,
    endDate?:string,
    employeeId?:number,
    status?:string,
    carModelId?:number,
}

export interface UserOption{
    id:number,
    name:string,
    surname:string,
    role:string,
}

export interface User {
    name?: string;
    userInfo?: {
        claims?: {
            sub?: string;
            driving_licence?: string;
            realm_access?: {
                roles?: string[];
            };
            date_of_birth?: string;
            name?: string;
            phone_number?: string;
            preferred_username?: string;
            expiration_date?: string;
            given_name?: string;
            family_name?: string;
            email?: string;
        };
    };
    csrf?: string;
}
