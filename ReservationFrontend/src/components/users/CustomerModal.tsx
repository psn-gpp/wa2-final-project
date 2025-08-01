import React, {ChangeEvent} from 'react';
import { Button, Modal, Form } from 'react-bootstrap';
import {Customer} from "../../types.ts";

interface CustomerAddModalProps {
    show: boolean;
    onClose: () => void;
    onAddCustomer: (newCustomer: Customer) => void;
    newCustomer: Customer;
    handleChange: (e: ChangeEvent<HTMLInputElement>) => void;
    errors: any
}

const CustomerModal: React.FC<CustomerAddModalProps> = ({
                                                               show,
                                                               onClose,
                                                               onAddCustomer,
                                                               newCustomer,
                                                               handleChange,
                                                               errors
                                                           }) => {
    return (
        <Modal show={show} onHide={onClose}>
            <Modal.Header closeButton>
                <Modal.Title>Add New Customer</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form>
                    <Form.Group controlId="formName">
                        <Form.Label>Name</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter name"
                            name="name"
                            value={newCustomer.genericUserData.name}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formSurname">
                        <Form.Label>Surname</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter surname"
                            name="surname"
                            value={newCustomer.genericUserData.surname}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formEmail">
                        <Form.Label>Email</Form.Label>
                        <Form.Control
                            type="email"
                            placeholder="Enter email"
                            name="email"
                            value={newCustomer.genericUserData.email}
                            onChange={handleChange}
                            isInvalid={!!errors.email}
                        />
                        {errors.email && <Form.Control.Feedback type="invalid">{errors.email}</Form.Control.Feedback>}
                    </Form.Group>

                    <Form.Group controlId="formPhone">
                        <Form.Label>Phone</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter phone number"
                            name="phone"
                            value={newCustomer.genericUserData.phone}
                            onChange={handleChange}
                            isInvalid={!!errors.phone}
                        />
                        {errors.phone && <Form.Control.Feedback type="invalid">{errors.phone}</Form.Control.Feedback>}
                    </Form.Group>

                    <Form.Group controlId="formAddress">
                        <Form.Label>Address</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter address"
                            name="address"
                            value={newCustomer.genericUserData.address}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formCity">
                        <Form.Label>City</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter city"
                            name="city"
                            value={newCustomer.genericUserData.city}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formDateOfBirth">
                        <Form.Label>Date of Birth</Form.Label>
                        <Form.Control
                            type="date"
                            name="dateOfBirth"
                            value={newCustomer.dateOfBirth}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formReliabilityScore">
                        <Form.Label>Reliability Score</Form.Label>
                        <Form.Control
                            type="number"
                            placeholder="Enter reliability score"
                            name="reliabilityScores"
                            value={newCustomer.reliabilityScores}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formDrivingLicence">
                        <Form.Label>Driving Licence</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter driving licence"
                            name="drivingLicence"
                            value={newCustomer.drivingLicence}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formExpirationDate">
                        <Form.Label>Expiration Date</Form.Label>
                        <Form.Control
                            type="date"
                            name="expirationDate"
                            value={newCustomer.expirationDate}
                            onChange={handleChange}
                        />
                    </Form.Group>
                </Form>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={onClose}>
                    Close
                </Button>
                <Button variant="primary" onClick={() => onAddCustomer(newCustomer)}>
                    Add Customer
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

export default CustomerModal;