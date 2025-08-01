import React, {Dispatch, SetStateAction, useContext, useEffect, useState} from 'react';
import {Table, Spinner, Pagination, Form, Button, Row} from 'react-bootstrap';
import {CarModel, Customer, Employee, Reservation, User, Vehicle} from "../types.ts";
import CustomerAPI from "../api/customerAPI.ts";
import EmployeeAPI from "../api/employeeAPI.ts";
import ReservationAPI from "../api/reservationAPI.ts";
import ReservationModal from "./ReservationModal.tsx";
import VehicleAPI from "../api/vehicleAPI.ts";
import CarModelAPI from "../api/carModelAPI.ts";
import SortIcon from "./SortIcon.tsx";
import { UserContext } from '../App.tsx';

interface ReservationTableProps {
    isCustomerView: boolean;
    setPaidReservationId: Dispatch<SetStateAction<number | null>>
}

const PAGE_SIZE = 10;

const ReservationTable: React.FC<ReservationTableProps> = ({isCustomerView, setPaidReservationId}) => {
    const context = useContext(UserContext)

    const user = context.user as User;
    const role = context.role as string[] | null;
    const userId = context.userId as number;

    const [reservations, setReservations] = useState<Reservation[]>([]);
    const [filteredReservations, setFilteredReservations] = useState<Reservation[]>([]);
    const [users, setUsers] = useState<Record<number, Customer>>({});
    const [employees, setEmployees] = useState<Record<number, Employee>>({});
    const [models, setModels] = useState<Record<number, CarModel>>({});
    const [vehicles, setVehicles] = useState<Record<number, Vehicle>>({})
    const [page, setPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [filters, setFilters] = useState<{ [key: string]: any }>({});
    const [sort, setSort] = useState<string[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [customerNameFilter, setCustomerNameFilter] = useState<string>("");
    const [employeeNameFilter, setEmployeeNameFilter] = useState<string>("");
    const [showReservationModal, setShowReservationModal] = useState(false)
    const [reservationToModify, setReservationToModify] = useState<Reservation | undefined>(undefined)

    const fetchData = async () => {
        setLoading(true);
        try {
            const appliedFilters = {...filters};

            if (isCustomerView && user.name) {
                appliedFilters.customerId = userId;
            }


            if (appliedFilters.customerName) delete appliedFilters.customerName;
            if (appliedFilters.employeeName) delete appliedFilters.employeeName;
            console.log(appliedFilters)
            const result = await ReservationAPI.getReservations(appliedFilters, page, PAGE_SIZE, sort);

            setReservations(result.content || []);
            setTotalPages(result.totalPages || 0);

            const userIds: number[] = Array.from(new Set(result.content.map((r: Reservation) => r.customerId)));
            const employeeIds = Array.from(new Set(result.content.map((r: Reservation) => r.employeeId).filter(Boolean))) as number[];
            const vehicleIds: number[] = Array.from(new Set(result.content.map((r: Reservation) => r.vehicleId)));

            const promises: Promise<any>[] = [];

            if (!isCustomerView) {
                userIds.forEach(id => promises.push(CustomerAPI.getCustomer(id)));
                employeeIds.forEach(id => promises.push(EmployeeAPI.getEmployee(id)));
            }
            vehicleIds.forEach(id => promises.push(VehicleAPI.getVehicle(id.toString())));

            const responses = await Promise.all(promises);

            //console.log("responses",responses)

            const newUsers: Record<number, Customer> = {};
            const newEmployees: Record<number, Employee> = {};
            const newVehicles: Record<number, Vehicle> = {}; // salviamo anche i veicoli per estrarre modelId
            const modelIds = new Set<number>();

            let responseIndex = 0;

            if (!isCustomerView) {
                //console.log("customer view")
                userIds.forEach(id => {
                    newUsers[id] = responses[responseIndex]//.data;
                    responseIndex++;
                });

                employeeIds.forEach(id => {
                    newEmployees[id] = responses[responseIndex]//.data;
                    responseIndex++;
                });
            }

            vehicleIds.forEach(id => {
                /*console.log("id veicolo",id)
                console.log("responseIndex", responseIndex)
                console.log("response", responses)
                console.log("response[responseIndex]", responses[responseIndex])
                console.log("response[responseIndex].data",responses[responseIndex].data)*/
                const vehicle: Vehicle = responses[responseIndex]//.data;
                //console.log("vehicle",vehicle)
                newVehicles[id] = vehicle;
                if (vehicle.refCarModel) {
                    modelIds.add(vehicle.refCarModel);
                }
                responseIndex++;
            });

            const modelPromises = Array.from(modelIds).map(id => CarModelAPI.getCarModel(id.toString()));
            const modelResponses = await Promise.all(modelPromises);

            const newModels: Record<number, CarModel> = {};
            Array.from(modelIds).forEach((id, index) => {
                newModels[id] = modelResponses[index]//.data;
            });

            setUsers(newUsers);
            setEmployees(newEmployees);
            setVehicles(newVehicles)
            setModels(newModels);
            /*console.log("newVehicles",newVehicles)
            console.log("newModels",newModels)
            console.log("newUsers",newUsers)
            console.log("newEmployees", newEmployees)*/
        } catch (error) {
            console.error('Error fetching reservations:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [page, filters, sort, isCustomerView]);

    // Filtra le prenotazioni in base ai filtri frontend
    useEffect(() => {
        if (!isCustomerView) {
            const filtered = reservations.filter(reservation => {
                // Filtra per nome cliente se il filtro è impostato
                const customerMatch = !customerNameFilter ||
                    (users[reservation.customerId] &&
                        `${users[reservation.customerId].genericUserData.name} ${users[reservation.customerId].genericUserData.surname}`
                            .toLowerCase()
                            .includes(customerNameFilter.toLowerCase()));

                // Filtra per nome dipendente se il filtro è impostato
                const employeeMatch = !employeeNameFilter ||
                    (reservation.employeeId &&
                        employees[reservation.employeeId] &&
                        `${employees[reservation.employeeId].genericUserData.name} ${employees[reservation.employeeId].genericUserData.surname}`
                            .toLowerCase()
                            .includes(employeeNameFilter.toLowerCase()));

                return customerMatch && employeeMatch;
            });
            setFilteredReservations(filtered);
        } else {
            setFilteredReservations(reservations);
        }
    }, [reservations, users, employees, customerNameFilter, employeeNameFilter, isCustomerView]);

    const handleSort = (field: string) => {
        const currentSort = sort.find(s => s.startsWith(field));
        if (currentSort) {
            if (currentSort.endsWith('asc')) {
                setSort([`${field},desc`]);
            } else {
                setSort([]);
            }
        } else {
            setSort([`${field},asc`]);
        }
    };

    const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
        const {name, value} = e.target;

        // Gestisci separatamente i filtri frontend
        if (name === "customerName") {
            setCustomerNameFilter(value);
            return;
        }

        if (name === "employeeName") {
            setEmployeeNameFilter(value);
            return;
        }

        if(name==="carModelId"){
            setFilters(prev => ({
                ...prev,
                [name]: Number(value)
            }));
        }else{
            // Gestisci gli altri filtri normalmente
            setFilters(prev => ({
                ...prev,
                [name]: value
            }));
        }
        setPage(0);
    };

    const handleEditReservation = async (reservation: Reservation) => {
        try {
            await ReservationAPI.modifyReservation(reservation, user?.csrf as string);

            setReservations((prevReservation) =>
                prevReservation?.map((r) => r.id === reservation.id ? reservation : r)
            );
        } catch (error) {
            console.error("Error updating the reservation:", error);
        }

    }

    const handleDeleteReservation = async (reservationId: number | undefined) => {
        try {
            await ReservationAPI.deleteReservation(reservationId, user?.csrf as string)
            setReservations((prev) => prev.filter((r) => r.id !== reservationId));

        } catch (error) {
            console.error("Error deleting the reservation:", error);
        }
    }

    //TODO aggiungere stato payed
    const handlePayReservation = async (reservationId: number | null) => {
        try {
            setPaidReservationId(reservationId);
            if (!reservationId) {
                console.error('Reservation ID is null in handle pay reservation');
                return;
            }
            const paymentUrl = await ReservationAPI.payReservation(reservationId, user?.csrf as string);

            if (paymentUrl) {
                window.open(paymentUrl, '_blank');
            } else {
                console.error('No payment URL returned');
            }
        } catch (error) {
            console.error("Error processing payment:", error);
        }
    };



    if (loading) {
        return (
            <div className="text-center my-4">
                <Spinner animation="border"/>
            </div>
        );
    }

    const fakeData= "2025-05-16"

    return (
        <div className="ms-3 me-3 mt-3">
            {/* Filtri */}
            <Form className="mb-3 col-12 d-flex justify-content-between align-items-center flex-wrap gap-3">
                <Form.Group controlId="statusFilter">
                    <Form.Label>Status</Form.Label>
                    <Form.Control as="select" name="status" value={filters.status} onChange={handleFilterChange}>
                        <option value="">All</option>
                        <option value="PENDING">Pending</option>
                        <option value="APPROVED">Approved</option>
                        <option value="REJECTED">Rejected</option>
                        <option value="ON_COURSE">On course</option>
                        <option value="TERMINATED">Terminated</option>
                    </Form.Control>
                </Form.Group>

                <Form.Group controlId="modelFilter">
                    <Form.Label>Car Model</Form.Label>
                    <Form.Control as="select" name="carModelId" value={Object.entries(models).find(([_, model]) => model.id == filters.carModelId)?.[1].id} onChange={handleFilterChange}>
                        <option value="">All</option>
                        {Object.entries(models).map(([key, model]) => (
                            <option key={key} value={model.id}>
                                {model.model}
                            </option>
                        ))}
                    </Form.Control>
                </Form.Group>

                <Form.Group controlId="startDateFilter">
                    <Form.Label>Start Date</Form.Label>
                    <Form.Control type="date" value={filters.startDate} name="startDate" onChange={handleFilterChange}/>
                </Form.Group>

                <Form.Group controlId="endDateFilter">
                    <Form.Label>End Date</Form.Label>
                    <Form.Control type="date" name="endDate" value={filters.endDate} onChange={handleFilterChange}/>
                </Form.Group>

                {!isCustomerView && (
                    <>
                        <Form.Group controlId="customerFilter">
                            <Form.Label>Customer Name</Form.Label>
                            <Form.Control
                                type="text"
                                name="customerName"
                                placeholder="Customer name"
                                value={customerNameFilter}
                                onChange={handleFilterChange}
                            />
                        </Form.Group>

                        <Form.Group controlId="employeeFilter">
                            <Form.Label>Employee Name</Form.Label>
                            <Form.Control
                                type="text"
                                name="employeeName"
                                placeholder="Employee name"
                                value={employeeNameFilter}
                                onChange={handleFilterChange}
                            />
                        </Form.Group>
                    </>
                )}
                <div className="me-3">
                    <Button onClick={()=>{
                        setSort([])
                        setFilters({"customerId":userId})
                    }} variant="outline-danger">
                        Clear
                    </Button>
                </div>
            </Form>

            {/* Tabella */}
            <Table striped bordered hover responsive>
                <thead>
                <tr>
                    <th onClick={() => handleSort('startDate')}>
                        Start date <SortIcon field="startDate" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('endDate')}>
                        End date <SortIcon field="endDate" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('status.status')}>
                        Status <SortIcon field="status.status" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('reservationDate')}>
                        Reservation date <SortIcon field="reservationDate" sort={sort}/>
                    </th>
                    <th>
                        Car model
                    </th>
                    {!isCustomerView && (
                        <>
                            <th>Customer</th>
                            <th>Employee</th>
                        </>
                    )}
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {filteredReservations.map((reservation) => (
                    <tr key={reservation.id ?? `${reservation.customerId}-${reservation.startDate}`}>
                        <td>{reservation.startDate.split('T')[0]}</td>
                        <td>{reservation.endDate.split('T')[0]}</td>
                        <td>{reservation.status.status}</td>
                        <td>{reservation.reservationDate.split('T')[0]}</td>
                        <td>
                            {vehicles[reservation.vehicleId]
                                ? models[vehicles[reservation.vehicleId].refCarModel]?.model || 'Load...'
                                : 'Loading...'}
                        </td>
                        {!isCustomerView && (
                            <>
                                <td>{users[reservation.customerId] ? `${users[reservation.customerId].genericUserData.name} ${users[reservation.customerId].genericUserData.surname}` : 'Loading...'}</td>
                                <td>{reservation.employeeId && employees[reservation.employeeId] ? `${employees[reservation.employeeId].genericUserData.name} ${employees[reservation.employeeId].genericUserData.surname}` : 'No one in charge at the moment'}</td>
                            </>
                        )}

                        <td>
                            {role?.includes("Customer") ?
                                (<>
                                    {reservation.status.status == "PENDING" &&
                                        <Button size="sm" onClick={() => {
                                            setShowReservationModal(true)
                                            setReservationToModify(reservation)
                                        }}>
                                            <i className="bi bi-pencil-square"></i>
                                        </Button>
                                    }
                                    {((reservation.status.status == "PENDING" || reservation.status.status == "APPROVED") &&
                                        <Row className="d-flex">
                                            <div>
                                                <Button size="sm" variant="danger" onClick={() => {
                                                    handleDeleteReservation(reservation.id)
                                                }}>
                                                    <i className="bi bi-trash3-fill"></i>
                                                </Button>
                                            </div>
                                            {(reservation.status.status == "APPROVED" &&
                                                <Button size="sm" variant="success" onClick={() => {
                                                    if (reservation.id === undefined) {
                                                        console.log("Reservation ID is undefined al click su pay");
                                                        return
                                                    }
                                                    handlePayReservation(reservation.id)
                                                }}>
                                                    <i className="bi bi-credit-card-fill"></i> Pay
                                                </Button>
                                            )}
                                        </Row>
                                    )}
                                </>)
                                : (
                                    <>
                                        {new Date(reservation.startDate) > new Date(fakeData) &&
                                            reservation.status.status === "PENDING" &&
                                            <>
                                                <Button size="sm" variant="success" onClick={() => {
                                                    const updatedReservation = {
                                                        ...reservation,
                                                        employeeId:userId,
                                                        status: {
                                                            ...reservation.status,
                                                            status: "APPROVED"
                                                        }
                                                    };
                                                    console.log("approve reservation", updatedReservation)
                                                    handleEditReservation(updatedReservation);
                                                }}>
                                                    Approve
                                                </Button>
                                                <Button size="sm" variant="danger" onClick={() => {
                                                    const updatedReservation = {
                                                        ...reservation,
                                                        employeeId:userId,
                                                        status: {
                                                            ...reservation.status,
                                                            status: "REJECTED"
                                                        }
                                                    };
                                                    handleEditReservation(updatedReservation);
                                                }}>
                                                    Reject
                                                </Button>
                                            </>
                                        }
                                        {new Date(reservation.endDate) < new Date(fakeData) &&
                                            reservation.status.status === "ON_COURSE" &&
                                            <>
                                                <Button size="sm" onClick={() => {
                                                    const updatedReservation = {
                                                        ...reservation,
                                                        status: {
                                                            ...reservation.status,
                                                            status: "TERMINATED"
                                                        }
                                                    };
                                                    handleEditReservation(updatedReservation);
                                                }}>
                                                    Terminate rent
                                                </Button>
                                            </>
                                        }

                                        {new Date(reservation.startDate) <= new Date(fakeData) &&
                                            new Date(reservation.endDate) > new Date(fakeData) &&
                                            reservation.status.status === "APPROVED" &&
                                            <>
                                                <Button size="sm" onClick={() => {
                                                    const updatedReservation = {
                                                        ...reservation,
                                                        status: {
                                                            ...reservation.status,
                                                            status: "ON_COURSE"
                                                        }
                                                    };
                                                    console.log("start reservation", updatedReservation)
                                                    handleEditReservation(updatedReservation);
                                                }}>
                                                    Start rent
                                                </Button>
                                            </>
                                        }

                                    </>
                                )}

                        </td>
                    </tr>
                ))}
                </tbody>
            </Table>

            {/* Paginazione */}
            <Pagination className="justify-content-center">
                <Pagination.First onClick={() => setPage(0)} disabled={page === 0}/>
                <Pagination.Prev onClick={() => setPage(p => Math.max(p - 1, 0))} disabled={page === 0}/>
                {[...Array(totalPages)].map((_, idx) => (
                    <Pagination.Item key={idx} active={idx === page} onClick={() => setPage(idx)}>
                        {idx + 1}
                    </Pagination.Item>
                ))}
                <Pagination.Next onClick={() => setPage(p => Math.min(p + 1, totalPages - 1))}
                                 disabled={page === totalPages - 1}/>
                <Pagination.Last onClick={() => setPage(totalPages - 1)} disabled={page === totalPages - 1}/>
            </Pagination>


            <ReservationModal
                show={showReservationModal}
                onHide={() => setShowReservationModal(false)}
                onEditReservation={handleEditReservation}
                isEditMode={true}
                existingReservation={reservationToModify}
                pricePerDay={
                    reservationToModify?.vehicleId !== undefined
                        ? models[vehicles[reservationToModify.vehicleId]?.refCarModel]?.costPerDay
                        : 0
                }
            />
        </div>
    );
};

export default ReservationTable;