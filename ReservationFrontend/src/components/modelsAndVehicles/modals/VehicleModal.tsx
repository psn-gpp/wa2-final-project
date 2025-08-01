import React, { useEffect, useState } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import { Vehicle } from "../../../types.ts";
import VehicleAPI from "../../../api/vehicleAPI.ts";

interface VehicleModalProps {
    show: boolean;
    handleClose: () => void;
    onSubmit: (vehicleData: Vehicle) => void;
    carModelId: number;
    vehicle?: Vehicle; // veicolo esistente per la modifica
}

const VehicleModal: React.FC<VehicleModalProps> = ({ show, handleClose, onSubmit, carModelId, vehicle }) => {
    const [formData, setFormData] = useState<Vehicle>({
        refCarModel: carModelId,
        availability: '',
        licencePlate: '',
        vin: '',
        kilometers: 0,
        pendingCleaning: false,
        pendingMaintenance: false
    });

    const [errors, setErrors] = useState({
        vin: '',
        licencePlate: ''
    });
    const [availability, setAvailability] = useState<string[]>([])

    // Se vehicle cambia o il modal si apre, inizializza i dati
    useEffect(() => {
        const fetchVehicleFilters = async() =>{
            const response = await VehicleAPI.getVehicleFilters()
            setAvailability(response.availabilities)
        }

        fetchVehicleFilters()

        if (vehicle) {
            console.log("vedo il veicolo", vehicle)
            setFormData(vehicle);
        } else {
            setFormData({
                refCarModel: carModelId,
                availability: '',
                licencePlate: '',
                vin: '',
                kilometers: 0,
                pendingCleaning: false,
                pendingMaintenance: false
            });
        }
    }, [vehicle, show, carModelId]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type, checked } = e.target;
        setFormData({
            ...formData,
            [name]: type === 'checkbox' ? checked : (type === 'number' ? Number(value) : value)
        });

        if (name === 'vin') {
            const vinRegex = /^[A-HJ-NPR-Z0-9]{17}$/;
            if (!vinRegex.test(value)) {
                setErrors({ ...errors, vin: 'VIN not valid. It must be composed by 17 capital alphanumeric characters.' });
            } else {
                setErrors({ ...errors, vin: '' });
            }
        }else if (name=== 'licencePlate'){
            const vinRegex = /^[A-Z0-9 -]{5,12}$/;
            if (!vinRegex.test(value)) {
                setErrors({ ...errors, licencePlate: 'Licence plate not valid. It must be composed by 5 to 12 capital alphanumeric characters.' });
            } else {
                setErrors({ ...errors, licencePlate: '' });
            }
        }
    };

    const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const { name, value } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: value || undefined,
        }));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (errors.vin) return;

        onSubmit(formData);
        handleClose();
    };

    const isEditing = !!vehicle;

    return (
        <Modal show={show} onHide={handleClose}>
            <Modal.Header closeButton>
                <Modal.Title>{isEditing ? 'Edit vehicle' : 'Add new vehicle'}</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    <Form.Group className="mb-3" controlId="availability">
                        <Form.Label>Availability</Form.Label>
                        <Form.Select
                            name="availability"
                            value={formData.availability ?? ''}
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

                    <Form.Group className="mb-3" controlId="licencePlate">
                        <Form.Label>Licence plate</Form.Label>
                        <Form.Control
                            type="text"
                            name="licencePlate"
                            value={formData.licencePlate}
                            onChange={handleChange}
                            required
                            isInvalid={!!errors.licencePlate}
                        />
                        {errors.licencePlate && <Form.Control.Feedback type="invalid">{errors.licencePlate}</Form.Control.Feedback>}
                    </Form.Group>

                    <Form.Group className="mb-3" controlId="vin">
                        <Form.Label>VIN</Form.Label>
                        <Form.Control
                            type="text"
                            name="vin"
                            value={formData.vin}
                            onChange={handleChange}
                            isInvalid={!!errors.vin}
                            required
                        />
                        {errors.vin && <Form.Control.Feedback type="invalid">{errors.vin}</Form.Control.Feedback>}
                    </Form.Group>

                    <Form.Group className="mb-3" controlId="kilometers">
                        <Form.Label>Kilometers</Form.Label>
                        <Form.Control
                            type="number"
                            name="kilometers"
                            value={formData.kilometers}
                            onChange={handleChange}
                            required
                        />
                    </Form.Group>

                    <Form.Check
                        type="checkbox"
                        id="pendingCleaning"
                        name="pendingCleaning"
                        label="Pending cleaning"
                        checked={formData.pendingCleaning}
                        onChange={handleChange}
                        className="mb-2"
                    />

                    <Form.Check
                        type="checkbox"
                        id="pendingMaintenance"
                        name="pendingMaintenance"
                        label="Pending maintenance"
                        checked={formData.pendingMaintenance}
                        onChange={handleChange}
                        className="mb-2"
                    />
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="secondary" onClick={handleClose}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="primary">
                        {isEditing ? 'Save changes' : 'Add'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default VehicleModal;
