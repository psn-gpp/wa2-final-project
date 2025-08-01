import React, {useContext, useEffect, useState} from "react";
import {Form, Button, Container, Row, Col, Pagination, Spinner, Card, Table} from "react-bootstrap";
import {useNavigate, useParams} from "react-router-dom";
import {Maintenance, MaintenanceFilters, User, Vehicle} from "../../types.ts";
import VehicleAPI from "../../api/vehicleAPI.ts";
import FormSorting from "./forms/FormSorting.tsx";
import MaintenanceAPI from "../../api/maintenanceAPI.ts";
import VehicleModal from "./modals/VehicleModal.tsx";
import NotesModal from "./modals/NotesModal.tsx";
import MaintenanceModal from "./modals/MaintenanceModal.tsx";
import {UserContext} from "../../App.tsx";

/*interface VehiclesProps {
    showModal: boolean;
    setShowModal: Dispatch<SetStateAction<boolean>>;
}*/


const SingleVehicle/*: React.FC<VehiclesProps>*/ = (/*{ showModal, setShowModal}*/) => {
    const context = useContext(UserContext) ;

    const user = context.user as User;
    const role = context.role as string[] | null;

    const {vehicleId} = useParams();
    const [vehicleDetails, setVehicleDetails] = useState<Vehicle>();
    const navigate = useNavigate();
    const [maintenances, setMaintenances] = useState<Maintenance[]>([]);
    const [loadingMaintenances, setLoadingMaintenances] = useState<boolean>(true);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [sortOptions, setSortOptions] = useState<string[]>([]);
    const [showModal, setShowModal] = useState(false)
    const [showNotes, setShowNotes] = useState(false)
    const [filters, setFilters] = useState<MaintenanceFilters>({
        defect: undefined,
        completedMaintenance: undefined,
        startDate: undefined,
        endDate: undefined
    });
    const [selectedMaintenance, setSelectedMaintenance] = useState<Maintenance>()
    const [showMaintenanceModal, setShowMaintenanceModal] = useState(false)

    const pageSize = 10;

    const sortingParams = [
        "date", "defect"
    ];

    useEffect(() => {
        const fetchVehicleDetails = async () => {
            try {
                const response = await VehicleAPI.getVehicle(vehicleId);
                setVehicleDetails(response);
            } catch (error) {
                console.error("Error fetching vehicle:", error);
            }
        };


        fetchVehicleDetails();
        fetchMaintenances();
    }, [vehicleId]);

    const fetchMaintenances = async () => {
        try {
            const response = await MaintenanceAPI.getMaintenances(filters, currentPage, pageSize, sortOptions, vehicleId);
            setMaintenances(response.content);
            setTotalPages(response.totalPages);
        } catch (error) {
            console.error('Error fetching vehicles:', error);
        } finally {
            setLoadingMaintenances(false);
        }
    };

    const parsedVehicleId = vehicleId ? Number(vehicleId) : undefined;

    // Verifica che sia un numero valido
    if (parsedVehicleId === undefined || isNaN(parsedVehicleId)) {
        return <div>Vehicle ID not valid</div>;
    }

    if (!vehicleDetails) {
        return (
            <Container className="text-center mt-5">
                <Spinner animation="border" variant="primary"/>
                <p>Loading...</p>
            </Container>
        );
    }

    const handleDeleteVehicle = async () => {
        try {
            await VehicleAPI.deleteVehicle(vehicleDetails.id, user.csrf as string)
            navigate(`/ui/carModels/${vehicleDetails.refCarModel}`)

        } catch (error) {
            console.error("Error deleting the vehicle:", error);
        }
    };

    const handleUpdateVehicle = async (vehicle: Vehicle) => {
        try {
            await VehicleAPI.modifyVehicle(vehicle, user.csrf as string);
            setVehicleDetails(vehicle)
        } catch (error) {
            console.error("Error modifing vehicle:", error);
        }
    };

    const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setCurrentPage(0); // Reset della paginazione quando si applicano nuovi filtri
        fetchMaintenances();
    };

    const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, type, value, checked} = e.target;

        setFilters((prev) => ({
            ...prev,
            [name]:
                type === 'checkbox'
                    ? checked
                    : type === 'date'
                        ? value
                        : value
        }));
    };


    const handleMaintenanceSubmit = async (formData: {
        defect: string;
        date: string;
        completedMaintenance: boolean;
    }) => {
        try {
            if (selectedMaintenance) {
                await MaintenanceAPI.modifyMaintenance(vehicleId, {
                    ...formData,
                    vehicleId: parsedVehicleId,
                    id: selectedMaintenance.id,
                    vehicleLicencePlate:vehicleDetails.licencePlate
                }, user.csrf as string);

                // Sostituisci l'elemento nella lista
                setMaintenances((prevMaintenances) =>
                    prevMaintenances.map((m) => m.id === selectedMaintenance.id ? {...formData, vehicleId: parsedVehicleId, id: selectedMaintenance.id,vehicleLicencePlate:vehicleDetails.licencePlate} : m)
                );
            } else {
                const {location, data} = await MaintenanceAPI.addMaintenance(vehicleId, {
                    ...formData,
                    vehicleId: parsedVehicleId,
                    vehicleLicencePlate:vehicleDetails.licencePlate
                }, user.csrf as string);
                if (!location || !data) {
                    throw new Error("Error in the response");
                }

                // Aggiungi la nuova manutenzione alla lista
                setMaintenances((prev) => [...prev, data]);
            }
        } catch (error) {
            console.error('Error adding maintenance:', error);
        }
    };

    return (
        <>
            <Container className="mt-5">
                <Row>
                    <Col md={12}>
                        <Card>
                            <Card.Body>
                                <Card.Title>Vehicle details</Card.Title>
                                <Row>
                                    <Col md={6}>
                                        <p><strong>Licence Plate:</strong> {vehicleDetails.licencePlate}</p>
                                        <p><strong>VIN:</strong> {vehicleDetails.vin}</p>
                                        <p><strong>Kilometers:</strong> {vehicleDetails.kilometers}</p>
                                    </Col>
                                    <Col md={6}>
                                        <p><strong>Availability:</strong> {vehicleDetails.availability}</p>
                                        <p><strong>Pending
                                            cleaning:</strong> {vehicleDetails.pendingCleaning ? "Yes" : "No"}</p>
                                        <p><strong>Pending
                                            maintenance:</strong> {vehicleDetails.pendingMaintenance ? "Yes" : "No"}</p>
                                    </Col>
                                </Row>
                                <Row className="mt-3">
                                    <Col md={4}>
                                        <Button onClick={() => setShowModal(true)}>Edit vehicle</Button>
                                    </Col>
                                    <Col md={4}>
                                        <Button variant="warning" onClick={() => setShowNotes(true)}>Open vehicle
                                            notes</Button>
                                    </Col>
                                    <Col md={4}>
                                        <Button variant="danger" onClick={handleDeleteVehicle}> Delete vehicle </Button>
                                    </Col>
                                </Row>
                            </Card.Body>
                        </Card>
                    </Col>
                </Row>

                <h3 className="mt-4">Vehicles</h3>
                {loadingMaintenances ? (
                    <Spinner animation="border" variant="primary"/>
                ) : (
                    <div className="table-responsive" style={{maxHeight: '400px', overflowY: 'auto'}}>
                        <Form onSubmit={handleSearch} className="bg-light p-4 rounded shadow-sm mb-4">
                            <Row className="g-3">
                                <Col key={"defect"} md={3}>
                                    <Form.Group controlId="filterDefect" className="mb-2">
                                        <Form.Label>Defect</Form.Label>
                                        <Form.Control
                                            type="string"
                                            name="defect"
                                            value={filters.defect}
                                            onChange={handleFilterChange}
                                        />
                                    </Form.Group>
                                </Col>
                                <Col key={"startDate"} md={3}>
                                    <Form.Group controlId="filterStartDate" className="mb-2">
                                        <Form.Label>Start date</Form.Label>
                                        <Form.Control
                                            type="date"
                                            name="startDate"
                                            value={filters.startDate}
                                            onChange={handleFilterChange}
                                        />
                                    </Form.Group>
                                </Col>
                                <Col key={"endDate"} md={3}>
                                    <Form.Group controlId="filterEndDate" className="mb-2">
                                        <Form.Label>End date</Form.Label>
                                        <Form.Control
                                            type="date"
                                            name="endDate"
                                            value={filters.endDate}
                                            onChange={handleFilterChange}
                                        />
                                    </Form.Group>
                                </Col>
                                <Col key={"filterCompletedMaintenance"} md={3}>
                                    <Form.Check
                                        type="checkbox"
                                        id="filterCompletedMaintenance"
                                        name="completedMaintenance"
                                        label="Completed maintenance"
                                        checked={filters.completedMaintenance}
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
                        {role?.includes("Fleet_Manager") && (
                            <Button size="sm" onClick={() => {
                                setSelectedMaintenance(undefined);
                                setShowMaintenanceModal(true)
                            } /*handleEditMaintenance(maintenance)*/}>
                                Add new maintenance
                            </Button>
                        )}
                        <Table striped bordered hover responsive>
                            <thead>
                            <tr>
                                <th>Defect</th>
                                <th>Completed</th>
                                <th>Date</th>
                            </tr>
                            </thead>
                            <tbody>
                            {maintenances.length > 0 ? (
                                maintenances.map((maintenance) => (
                                    <tr key={maintenance.id}>
                                        <td>{maintenance.defect}</td>
                                        <td>{maintenance.completedMaintenance ? 'Yes' : 'No'}</td>
                                        <td>{maintenance.date}</td>
                                        <td>

                                            <Button variant="info"
                                                    size="sm" onClick={() => {
                                                setSelectedMaintenance(maintenance);
                                                setShowMaintenanceModal(true)
                                            } /*handleEditMaintenance(maintenance)*/}>
                                                <i className="bi bi-pencil-square"></i>
                                            </Button>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={7} className="text-center">No maintenances found</td>
                                </tr>
                            )}
                            </tbody>
                        </Table>


                        {totalPages > 1 && (
                            <Pagination className="justify-content-center">
                                {[...Array(totalPages)].map((_, idx) => (
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
            </Container>

            <VehicleModal
                show={showModal}
                handleClose={() => setShowModal(false)}
                onSubmit={handleUpdateVehicle}
                vehicle={vehicleDetails}
                carModelId={vehicleDetails.refCarModel}
            />

            <NotesModal
                isOpen={showNotes}
                onClose={() => setShowNotes(false)}
                vehicleId={parsedVehicleId}
            />

            <MaintenanceModal
                show={showMaintenanceModal}
                onHide={() => setShowMaintenanceModal(false)}
                onSubmit={handleMaintenanceSubmit}
                initialData={selectedMaintenance}
            />
        </>
    );
}

export default SingleVehicle;
