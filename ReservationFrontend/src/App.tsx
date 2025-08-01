//import {useState} from 'react'
import './App.css'
import {Routes, Route, Outlet, Navigate, useParams} from 'react-router-dom';
import CarModels from "./components/modelsAndVehicles/CarModels.tsx";
import SingleModel from "./components/modelsAndVehicles/SingleModel.tsx";
import {createContext, JSX, useContext, useEffect, useState} from "react";
import SingleVehicle from "./components/modelsAndVehicles/SingleVehicle.tsx";
import {User, /*Customer, genericUserData, Employee, Role*/} from "./types";
import Navigation from "./components/Navigation.tsx";
import UserProfile from "./components/users/UserProfile.tsx";
import ErrorModal from "./components/ErrorModal.tsx";
import ReservationTable from "./components/ReservationTable.tsx";
import CustomerManagement from "./components/users/CustomersManagment.tsx";
import EmployeeManagement from "./components/users/EmployeeManagment.tsx";
import LoadingPage from "./components/LoadingPage.tsx";
import CustomerAPI from "./api/customerAPI.ts";
// import EmployeeAPI from "./api/employeeAPI.ts";

export const UserContext = createContext({user: {}, userId:0, role: [] as string[], paidReservationId:1})

/*export function AnonymousContent({children}: {children: ReactNode}) {
    const user = useContext(UserContext) as User;
    if (user?.name) {
        return null
    } else return children
}

export function UserContent({children}: {children: ReactNode}) {
    const user = useContext(UserContext) as User;
    if (user?.name) {
        return children
    } else return null
}*/

function App() {
    const [showCarModelModal, setShowCarModelModal] = useState(false);
    const [showError, setShowError] = useState(false);
    const [errorMessage, setErrorMessage] = useState("")
    const [paidReservationId, setPaidReservationId] = useState<number | null>(1);
    const [me, setMe] = useState<User>({})
    const [role, setRole] = useState<string[] | null>([]);
    const [userId, setUserId] = useState<number>(0);

    useEffect(() => {
        fetch('/me')
            .then(res => res.json())
            .then(setMe)
            .catch(()=>setMe({}))
    }, [setMe]);

    useEffect(() => {

        const fetchCustomerByKeycloakId = async () =>
        {
            try {
                const keycloakId = me?.userInfo?.claims?.sub as string;
                const res = await CustomerAPI.getCustomerByKeycloakId(keycloakId)

                console.log(res)

                if (res.id) {
                    console.log(res.id)
                    setUserId(res.id);
                } else {
                    console.error(`No id found for Keycloak ID: ${me?.userInfo?.claims?.sub}`);
                }

            } catch (e) {
                /*const user = me?.userInfo?.claims;
                console.log(user)
                const genericUserData : genericUserData = {
                    keycloakId: user?.sub as string,
                    name: user?.given_name as string,
                    email: user?.email as string,
                    surname: user?.family_name as string,
                    phone: user?.phone_number as string || '',
                    address: '',
                    city: ''
                }
                console.log(genericUserData)

                if(user?.realm_access?.roles?.includes("Customer")) {
                    const customer : Customer = {
                        genericUserData: genericUserData,
                        dateOfBirth: user?.date_of_birth as string,
                        reliabilityScores: 5,
                        drivingLicence: user?.driving_licence as string,
                        expirationDate: user?.expiration_date as string,
                    }
                    console.log(customer)
                    const id = await CustomerAPI.addCustomer(customer, me?.csrf as string)
                    console.log(id.data)
                    setUserId(id.data?.id || 0);
                    console.log(userId)
                } else {
                    let role : Role = {id: 0, nameRole: "NULL"};
                    if(user?.realm_access?.roles?.includes("Staff")) {
                        role = {id:1,nameRole: "Staff"};
                    } else/!* if(user?.realm_access?.roles?.includes("Manager"))*!/ {
                        role = {id:2,nameRole: "Manager"};
                    }/!* else if(user?.realm_access?.roles?.includes("Fleet_Manager")) {
                        role = {id:3,nameRole: "Fleet Manager"};
                    }*!/

                    const employee : Employee = {
                        genericUserData: genericUserData,
                        role: role,
                        salary: 1000
                    }

                    const response = await EmployeeAPI.addEmployee(employee, me?.csrf as string)
                    console.log(response)
                    setUserId(response.data?.id || 0)
                }*/

            }

        }

        if (me?.name) {
            fetchCustomerByKeycloakId()
            setRole(me?.userInfo?.claims?.realm_access?.roles || []);
        }
    }, [me])

    return (
        <UserContext value = {{user: me, userId: userId, role: role as string[], paidReservationId: paidReservationId as number}}>
            <Routes>
                <Route
                    element={
                        <>
                            <Navigation
                            />
                            <Outlet/>
                        </>
                    }
                >
                    <Route path="ui">
                        <Route index element={<Navigate to="/ui/carModels" replace/>}/>

                        <Route path="login" element={
                            <h1>Hello, {me?.name?.toString()}!</h1>
                        }/>

                        <Route path="?logout" element={<Navigate to="/carModels" replace/>}/>

                        <Route path="users/:userId" element={
                            <ProtectedRoute matchUserId>
                                <UserProfile
                                    setPaidReservationId={setPaidReservationId}
                                />
                            </ProtectedRoute>
                        }/>

                        <Route path="carModels" element={
                            <CarModels
                                showModal={showCarModelModal}
                                setShowModal={setShowCarModelModal}
                            />
                        }/>

                        <Route path="carModels/:modelId" element={
                            <SingleModel
                                showModal={showCarModelModal}
                                setShowModal={setShowCarModelModal}
                                setErrorMessage={setErrorMessage}
                                setShowErrorModal={setShowError}
                            />
                        }/>

                        <Route path="carModels/:modelId/vehicles/:vehicleId" element={
                            <ProtectedRoute requiredRoles={["Fleet_Manager", "Staff"]}>
                                <SingleVehicle/>
                            </ProtectedRoute>
                        }/>

                        <Route path="reservations" element={
                            <ProtectedRoute requiredRoles={["Customer", "Staff"]}>
                                <ReservationTable
                                    isCustomerView={false}
                                    setPaidReservationId={setPaidReservationId}
                                />
                            </ProtectedRoute>
                        }/>
                        <Route path="customers" element={
                            <ProtectedRoute requiredRoles={["Manager"]}>
                                <CustomerManagement/>
                            </ProtectedRoute>
                        }/>
                        <Route path="employees" element={
                            <ProtectedRoute requiredRoles={["Manager"]}>
                                <EmployeeManagement/>
                            </ProtectedRoute>
                        }/>

                        <Route path="loading" element={<LoadingPage/>} />

                    </Route>

                </Route>
            </Routes>

            <ErrorModal
                show={showError}
                errorMessage={errorMessage}
                onClose={() => setShowError(false)}
            />
        </UserContext>

    );
}

export default App


interface ProtectedRouteProps {
    children: JSX.Element;
    requiredRoles?: string[];
    matchUserId?: boolean;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({children, requiredRoles, matchUserId}) => {
    const params = useParams();
    const context = useContext(UserContext) ;

    const user = context.user as User;
    const role = context.role as string[] | null;
    const userId = context.userId as number;


    useEffect(() => {
        if (!user.name) {
            window.location.href= "/serverLogin"
        }

        // Role not authorized
        if (requiredRoles && !role?.some(it => requiredRoles.includes(it))) {
            window.location.href= "/serverLogin"
        }

        if (matchUserId && params.userId && parseInt(params.userId) !== userId) { // Chiedere spiegazioni a Chri
            window.location.href= "/serverLogin"
        }
    }, []);

    return children;
};