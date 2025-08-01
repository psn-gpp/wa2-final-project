import React, { ChangeEvent } from 'react';
import { Button, Modal, Form } from 'react-bootstrap';
import {Employee} from "../../types.ts";


interface EmployeeAddModalProps {
    show: boolean;
    onClose: () => void;
    onAddEmployee: (newEmployee: Employee) => void;
    newEmployee: Employee;
    handleChange: (e: ChangeEvent<HTMLInputElement>) => void;
    handleSelectChange: (e: React.ChangeEvent<HTMLSelectElement>) => void;
    errors: any
}

const EmployeeModal: React.FC<EmployeeAddModalProps> = ({
                                                               show,
                                                               onClose,
                                                               onAddEmployee,
                                                               newEmployee,
                                                               handleChange,
                                                               handleSelectChange,
                                                               errors
                                                           }) => {
    return (
        <Modal show={show} onHide={onClose}>
            <Modal.Header closeButton>
                <Modal.Title>Add New Employee</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form>
                    <Form.Group controlId="formName">
                        <Form.Label>Name</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter name"
                            name="name"
                            value={newEmployee.genericUserData.name}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formSurname">
                        <Form.Label>Surname</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter surname"
                            name="surname"
                            value={newEmployee.genericUserData.surname}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formEmail">
                        <Form.Label>Email</Form.Label>
                        <Form.Control
                            type="email"
                            placeholder="Enter email"
                            name="email"
                            value={newEmployee.genericUserData.email}
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
                            value={newEmployee.genericUserData.phone}
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
                            value={newEmployee.genericUserData.address}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formCity">
                        <Form.Label>City</Form.Label>
                        <Form.Control
                            type="text"
                            placeholder="Enter city"
                            name="city"
                            value={newEmployee.genericUserData.city}
                            onChange={handleChange}
                        />
                    </Form.Group>

                    <Form.Group controlId="formRole">
                        <Form.Label>Role</Form.Label>
                        <Form.Select
                            name="nameRole"
                            value={newEmployee.role.nameRole}
                            onChange={handleSelectChange}
                        >
                            <option value="">Select a role</option>
                            <option value="Staff">Staff</option>
                            <option value="Manager">Manager</option>
                            <option value="Fleet manager">Fleet Manager</option>
                        </Form.Select>
                    </Form.Group>

                    <Form.Group controlId="formSalary">
                        <Form.Label>Salary</Form.Label>
                        <Form.Control
                            type="number"
                            placeholder="Enter salary"
                            name="salary"
                            value={newEmployee.salary}
                            onChange={handleChange}
                        />
                    </Form.Group>
                </Form>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="secondary" onClick={onClose}>
                    Close
                </Button>
                <Button variant="primary" onClick={() => onAddEmployee(newEmployee)}>
                    Add Employee
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

export default EmployeeModal;