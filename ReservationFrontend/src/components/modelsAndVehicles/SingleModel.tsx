import React, {useState, useEffect, Dispatch, SetStateAction, useContext} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {
    Button,
    Container,
    Row,
    Col,
    Card,
    Spinner,
    Table,
    Pagination,
    Form,
    OverlayTrigger,
    Tooltip
} from 'react-bootstrap';
import CarModelAPI from "../../api/carModelAPI.ts";
import {CarModel, Reservation, User, Vehicle, VehicleFilters} from "../../types.ts";
import CarModelModal from "./modals/CarModelModal.tsx";
import "./SingleModel.css"
import VehicleAPI from "../../api/vehicleAPI.ts";
import FormSorting from "./forms/FormSorting.tsx";
import VehicleModal from "./modals/VehicleModal.tsx";
import vehicleAPI from "../../api/vehicleAPI.ts";
import "bootstrap-icons/font/bootstrap-icons.css";
import ReservationModal from "../ReservationModal.tsx";
import ReservationAPI from "../../api/reservationAPI.ts";
import CustomerAPI from "../../api/customerAPI.ts";
import {UserContext} from "../../App.tsx";

interface SingleModelProps {
    showModal: boolean;
    setShowModal: Dispatch<SetStateAction<boolean>>;
    setErrorMessage: Dispatch<SetStateAction<string>>;
    setShowErrorModal: Dispatch<SetStateAction<boolean>>;
}

const SingleModel: React.FC<SingleModelProps> = ({
                                                     showModal,
                                                     setShowModal,
                                                     setErrorMessage,
                                                     setShowErrorModal
                                                 }) => {
    const context = useContext(UserContext) ;

    const user = context.user as User;
    const role = context.role as string[] | null;
    const userId = context.userId as number;

    const {modelId} = useParams();
    const [modelDetails, setModelDetails] = useState<CarModel>();
    const navigate = useNavigate();
    const [vehicles, setVehicles] = useState<Vehicle[]>([]);
    const [loadingVehicles, setLoadingVehicles] = useState<boolean>(true);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [sortOptions, setSortOptions] = useState<string[]>([]);
    const [showVehicleModal, setShowVehicleModal] = useState(false)
    const [vehicleToModify, setVehicleToModify] = useState<Vehicle | undefined>(undefined)
    const [filters, setFilters] = useState<VehicleFilters>({
        availability: undefined,
        licencePlate: undefined,
        vin: undefined,
        kilometers: undefined,
        pendingCleaning: undefined,
        pendingMaintenance: undefined
    });
    const [engines, setEngines] = useState<string[]>([])
    const [transmissions, setTransimissions] = useState<string[]>([])
    const [drivetrains, setDrivetrains] = useState<string[]>([])
    const [safetyFeatures, setSafetyFeatures] = useState<string[]>([])
    const [infotainemts, setInfotainments] = useState<string[]>([])
    const [categories, setCategories] = useState<string[]>([])
    const [availability, setAvailability] = useState<string[]>([])

    const [showReservationModal, setShowReservationModal] = useState(false);

    const pageSize = 10;
    const sortingParams = [
        "kilometers"
    ];


    useEffect(() => {
        const fetchCarDetails = async () => {
            try {
                const response = await CarModelAPI.getCarModel(modelId);
                setModelDetails(response);
            } catch (error) {
                console.error("Error fetching car model:", error);
            }
        };

        const fetchFilters = async () => {
            const response = await CarModelAPI.getCarModelFilters()
            setEngines(response.engines)
            setDrivetrains(response.drivetrains)
            setInfotainments(response.infotainments)
            setSafetyFeatures(response.safetyFeatures)
            setTransimissions(response.transmission)
            setCategories(response.categories)
        }

        const fetchVehicleFilters = async () => {
            const response = await VehicleAPI.getVehicleFilters()
            setAvailability(response.availabilities)
        }

        fetchFilters()
        fetchVehicleFilters()
        fetchCarDetails();
        fetchVehicles();
        fetchFilters()
    }, [modelId]);

    const parsedModelId = modelId ? Number(modelId) : undefined;

    // Verifica che sia un numero valido
    if (parsedModelId === undefined || isNaN(parsedModelId)) {
        return <div>Model ID non valido</div>;
    }

    const fetchVehicles = async () => {
        try {
            const response = await VehicleAPI.getVehicles({refCarModel: modelId}, currentPage, pageSize, sortOptions);
            setVehicles(response.content);
            setTotalPages(response.totalPages);
        } catch (error) {
            console.error('Error fetching vehicles:', error);
        } finally {
            setLoadingVehicles(false);
        }
    };

    const handleDelete = async () => {
        try {
            await CarModelAPI.deleteCarModel(modelId, user.csrf as string)
            navigate(`/ui/`)  //return to list of all car models

        } catch (error) {
            console.error("Error deleting the car model:", error);
        }
    };

    const handleDeleteVehicle = async (vehicleId: number | undefined) => {
        try {
            await vehicleAPI.deleteVehicle(vehicleId, user.csrf as string)
            setVehicles((prev) => prev.filter((v) => v.id !== vehicleId));

        } catch (error) {
            console.error("Error deleting the car model:", error);
        }
    };

    if (!modelDetails) {
        return (
            <Container className="text-center mt-5">
                <Spinner animation="border" variant="primary"/>
                <p>Loading...</p>
            </Container>
        );
    }

    const handleUpdateCar = async (carModel: CarModel) => {
        try {
            await CarModelAPI.modifyCarModel(carModel, user.csrf as string);
            setModelDetails(carModel)
        } catch (error) {
            console.error("Error modifing a car model:", error);
        }
    };


    const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setCurrentPage(0); // Reset della paginazione quando si applicano nuovi filtri
        fetchVehicles();
    };

    const handleVehicleSubmit = async (vehicle: Vehicle) => {
        try {
            if (vehicleToModify) {//edit
                await vehicleAPI.modifyVehicle(vehicle, user.csrf as string)
                //vehicles list update with new info about the edited vehicle
                setVehicles((prevVehicles) =>
                    prevVehicles?.map((v) => v.id === vehicle.id ? vehicle : v)
                );
            } else { //add
                const {location, data} = await vehicleAPI.addVehicle(vehicle, user.csrf as string);

                if (!location) {
                    throw new Error("No header in the response");
                }
                //navigate(`/ui/vehicles/${data?.id}`)
                navigate(`/ui/carModels/${parsedModelId}/vehicles/${data?.id}`)
            }

        } catch (error) {
            console.error("Error adding a vehicle:", error);
        }
    }

    const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, type, value, checked} = e.target;
        setFilters((prev) => ({
            ...prev,
            [name]: type === 'checkbox'
                ? checked
                : type === 'number'
                    ? Number(value)
                    : value
        }));
    };

    const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const {name, value} = e.target;

        setFilters(prev => ({
            ...prev,
            [name]: value || undefined, // così se l'utente seleziona vuoto, torna undefined
        }));
    };


    const handleCreateReservation = async (reservation: Omit<Reservation, 'id' | 'employeeId' | 'status' | 'vehicleId' | 'reservationDate'|'version'>) => {
        try {
            const assignedVehicle = vehicles.find(v => v.availability !== "rented");

            if (!assignedVehicle || assignedVehicle.id === undefined) {
                throw new Error("No available vehicle found");
            }

            assignedVehicle.availability = "rented";

            const completeReservation: Reservation = {
                ...reservation,
                status: {
                    id: 0,
                    status: "PENDING",
                },
                vehicleId: assignedVehicle.id,
                reservationDate: new Date().toISOString().split('T')[0],
                version: 0
            };

            const {location, data} = await ReservationAPI.addReservation(completeReservation, user.csrf as string)

            if (!location) {
                throw new Error("No header in the response");
            }

            if (!data) {
                throw new Error("No data in the response");
            }

            setVehicles((prevVehicles) =>
                prevVehicles?.map((v) => v.id === assignedVehicle.id ? assignedVehicle : v)
            );

            //navigate to new reservation
            //navigate(`/ui/carModels/${user.id}/reservations/${data?.id}`)

            //navigate to user profile where all reservation are shown
            navigate(`/ui/users/${userId}`)

        } catch (error) {
            console.error("Error creating new reservation:", error);
        }
    };

    const checkEligibility = async () => {
        const eligible = await CustomerAPI.getCustomerEligibility(userId)
        console.log(eligible)
        if (eligible) {
            setShowReservationModal(true)
        } else {
            setErrorMessage("The user is not eligible for the rent")
            setShowErrorModal(true)
        }
    }

    return (
        <>
            <Container className="my-5">
                <Row>
                    <Col md={12}>
                        <Card>
                            <Card.Body>
                                <Card.Title>Car model details</Card.Title>
                                <Row>
                                    <Col md={4}>
                                        <p><strong>Model:</strong> {modelDetails.model}</p>
                                        <p><strong>Year:</strong> {modelDetails.modelYear}</p>
                                        <p><strong>Manufacturer:</strong> {modelDetails.manufacturer}</p>
                                        <p><strong>Segment:</strong> {modelDetails.segment}</p>
                                    </Col>
                                    <Col md={4}>
                                        <p><strong>Brand:</strong> {modelDetails.brand}</p>
                                        <p><strong>Doors Number:</strong> {modelDetails.doorsNo}</p>
                                        <p><strong>Seating Capacity:</strong> {modelDetails.seatingCapacity}</p>
                                        <p><strong>Luggage Capacity:</strong> {modelDetails.luggageCapacity}</p>
                                        <p><strong>Category:</strong> {modelDetails.category}</p>
                                    </Col>
                                    <Col md={4}>
                                        <p><strong>Brand:</strong> {modelDetails.drivetrain}</p>
                                        <p><strong>Doors Number:</strong> {modelDetails.engine}</p>
                                        <p><strong>Seating Capacity:</strong> {modelDetails.motorDisplacement}</p>
                                        <p><strong>Luggage Capacity:</strong> {modelDetails.segment}</p>
                                        <p><strong>Category:</strong> {modelDetails.transmission}</p>
                                    </Col>
                                </Row>
                                <Row className="mb-2">
                                    <Col xs={3} className="fw-bold">Infotainments:</Col>
                                    <Col xs={9}>
                                        <Row>
                                            {modelDetails.infotainments.map(i => (
                                                <Col xs="auto" key={i} className="me-3">• {i}</Col>
                                            ))}
                                        </Row>
                                    </Col>
                                </Row>
                                <Row className="mb-2">
                                    <Col xs={3} className="fw-bold">Safety features:</Col>
                                    <Col xs={9}>
                                        <Row>
                                            {modelDetails.safetyFeatures.map(i => (
                                                <Col xs="auto" key={i} className="me-3">• {i}</Col>
                                            ))}
                                        </Row>
                                    </Col>
                                </Row>
                                { role?.includes("Fleet_Manager") && user.name ?
                                    <Row className="mt-3">
                                        <Col md={6}>
                                            <Button onClick={() => setShowModal(true)}>Edit car model</Button>

                                        </Col>
                                        <Col md={6}>
                                            <Button
                                                variant="danger"
                                                onClick={handleDelete}
                                            >
                                                Delete car model
                                            </Button>
                                        </Col>
                                    </Row>
                                : null }
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>
                { role?.includes("Fleet_Manager") && user.name ?
                    (<>
                        <h3 className="mt-4">Vehicles</h3>
                        {loadingVehicles ? (
                            <Spinner animation="border" variant="primary"/>
                        ) : (

                            <div className="table-responsive" style={{maxHeight: '400px', overflowY: 'auto'}}>
                                <Form onSubmit={handleSearch} className="bg-light p-4 rounded shadow-sm mb-4">
                                    <Row className="g-3">
                                        <Col key={"filterAvailability"} md={3}>
                                            <Form.Group controlId="filterAvailability" className="mb-2">
                                                <Form.Label>Availability</Form.Label>
                                                <Form.Select
                                                    name="availability"
                                                    value={filters.availability ?? ''}
                                                    onChange={handleSelectChange}
                                                >
                                                    <option value="">Select availability</option>
                                                    {availability?.map(option => (
                                                        <option key={option} value={option}>
                                                            {option}
                                                        </option>
                                                    ))}
                                                </Form.Select>
                                            </Form.Group>
                                        </Col>
                                        <Col key={"filterLicencePlate"} md={3}>
                                            <Form.Group controlId="filterLicencePlate" className="mb-2">
                                                <Form.Label>Licence plate</Form.Label>
                                                <Form.Control
                                                    type="text"
                                                    name="licencePlate"
                                                    value={filters.licencePlate}
                                                    onChange={handleFilterChange}
                                                />
                                            </Form.Group>
                                        </Col>
                                        <Col key={"filterVin"} md={3}>
                                            <Form.Group controlId="filterVin" className="mb-2">
                                                <Form.Label>VIN</Form.Label>
                                                <Form.Control
                                                    type="text"
                                                    name="vin"
                                                    value={filters.vin}
                                                    onChange={handleFilterChange}
                                                />
                                            </Form.Group>
                                        </Col>
                                        <Col key={"filterKilometers"} md={3}>
                                            <Form.Group controlId="filterKilometers" className="mb-2">
                                                <Form.Label>Kilometers</Form.Label>
                                                <Form.Control
                                                    type="number"
                                                    name="kilometers"
                                                    value={filters.kilometers}
                                                    onChange={handleFilterChange}
                                                />
                                            </Form.Group>
                                        </Col>
                                        <Col key={"filterPendingCleaning"} md={3}>
                                            <Form.Check
                                                type="checkbox"
                                                id="filterPendingCleaning"
                                                name="pendingCleaning"
                                                label="Pending cleaning"
                                                checked={filters.pendingCleaning}
                                                onChange={handleFilterChange}
                                                className="mb-2"
                                            />
                                        </Col>
                                        <Col key={"filterPendingMaintenance"} md={3}>
                                            <Form.Check
                                                type="checkbox"
                                                id="filterPendingMaintenance"
                                                name="pendingMaintenance"
                                                label="Pending maintenance"
                                                checked={filters.pendingMaintenance}
                                                onChange={handleFilterChange}
                                                className="mb-2"
                                            />
                                        </Col>
                                    </Row>
                                    <FormSorting
                                        sortOptions={sortOptions}
                                        setSortOptions={setSortOptions}
                                        sortingParams={sortingParams}
                                    />

                                    <div className="mt-3 text-center">
                                        <Button type="submit" variant="primary">
                                            Search
                                        </Button>
                                    </div>
                                </Form>
                                <Button onClick={() => {
                                    setVehicleToModify(undefined);
                                    setShowVehicleModal(true)
                                }}>Add new vehicle</Button>
                                <Table striped bordered hover responsive>
                                    <thead>
                                    <tr>
                                        <th>Availability</th>
                                        <th>Licence Plate</th>
                                        <th>VIN</th>
                                        <th>Kilometers</th>
                                        <th>Pending Cleaning</th>
                                        <th>Pending Maintenance</th>
                                        <th>Actions</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {vehicles.length > 0 ? (
                                        vehicles?.map((vehicle) => (
                                            <tr key={vehicle.id}>
                                                <td>{vehicle.availability ? 'Yes' : 'No'}</td>
                                                <td>{vehicle.licencePlate}</td>
                                                <td>{vehicle.vin}</td>
                                                <td>{vehicle.kilometers}</td>
                                                <td>{vehicle.pendingCleaning ? 'Yes' : 'No'}</td>
                                                <td>{vehicle.pendingMaintenance ? 'Yes' : 'No'}</td>
                                                <td>
                                                    <Button size="sm"
                                                            onClick={() => /*navigate(`/ui/vehicles/${vehicle.id}`*/navigate(`/ui/carModels/${parsedModelId}/vehicles/${vehicle.id}`)}>
                                                        <i className="bi bi-info-circle-fill"></i>
                                                    </Button>
                                                    <Button size="sm" onClick={() => {
                                                        setVehicleToModify(vehicle);
                                                        setShowVehicleModal(true)
                                                    }}>
                                                        <i className="bi bi-pencil-square"></i>
                                                    </Button>
                                                    <Button size="sm" variant="danger" onClick={() => {
                                                        handleDeleteVehicle(vehicle.id)
                                                    }}>
                                                        <i className="bi bi-trash3-fill"></i>
                                                    </Button>
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan={7} className="text-center">No vehicles found</td>
                                        </tr>
                                    )}
                                    </tbody>
                                </Table>

                                {/* Pagination controls */}
                                {totalPages > 1 && (
                                    <Pagination className="justify-content-center">
                                        {[...Array(totalPages)]?.map((_, idx) => (
                                            <Pagination.Item
                                                key={idx}
                                                active={idx === currentPage}
                                                onClick={() => setCurrentPage(idx)}
                                            >
                                                {idx + 1}
                                            </Pagination.Item>
                                        ))}
                                    </Pagination>
                                )}
                            </div>
                        )}
                    </>) : null}
                    { role?.includes("Customer") || !user.name ?
                        <div className="text-center mt-4">
                            <OverlayTrigger
                                placement="top"
                                delay={{show: 250, hide: 400}}
                                overlay={<Tooltip id="button-tooltip">
                                    {vehicles.filter(v => v.availability !== "rented").length === 0 ? "No cars available for booking" : ""}
                                </Tooltip>}
                                show={vehicles.filter(v => v.availability !== "rented").length === 0 ? undefined : false}
                            >
                              <span
                                  className={vehicles.filter(v => v.availability !== "rented").length === 0 ? "d-inline-block" : ""}>
                                <Button
                                    variant="success"
                                    onClick={() => {
                                        if (user.name) {
                                            checkEligibility()
                                        } else {
                                            navigate('/ui/login')
                                        }
                                    }}
                                    disabled={vehicles.filter(v => v.availability !== "rented").length === 0}
                                    style={vehicles.filter(v => v.availability !== "rented").length === 0 ? {pointerEvents: 'none'} : {}}
                                >
                                  Book Car
                                </Button>
                              </span>
                            </OverlayTrigger>
                        </div>
                    : null}
            </Container>

            <CarModelModal
                show={showModal}
                handleClose={() => setShowModal(false)}
                onSubmitCarModel={handleUpdateCar}
                initialData={modelDetails}
                engines={engines}
                transmissions={transmissions}
                drivetrains={drivetrains}
                safetyOptions={safetyFeatures}
                infotainmentOptions={infotainemts}
                categories={categories}
            />

            <VehicleModal
                show={showVehicleModal}
                handleClose={() => setShowVehicleModal(false)}
                onSubmit={handleVehicleSubmit}
                carModelId={parsedModelId}
                vehicle={vehicleToModify}   //is undefined when called by add, is the vehicle to edit when called by edit
            />

            <ReservationModal
                show={showReservationModal}
                onHide={() => setShowReservationModal(false)}
                onCreateReservation={handleCreateReservation}
                userId={userId}
                isEditMode={false}
                pricePerDay={modelDetails.costPerDay}
            />
        </>
    );
};

export default SingleModel;