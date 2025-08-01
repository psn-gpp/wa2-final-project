import React, {Dispatch, SetStateAction, useContext, useEffect, useState} from "react";
import {Button, Card, Col, Form, Row, Spinner} from "react-bootstrap";
import {Customer, Employee, genericUserData, Role, User} from "../../types.ts";
import CustomerAPI from "../../api/customerAPI.ts";
import EmployeeAPI from "../../api/employeeAPI.ts";
import ReservationTable from "../ReservationTable.tsx";
import {UserContext} from "../../App.tsx";

interface UserProfileProps {
    setPaidReservationId: Dispatch<SetStateAction<number | null>>
}

const UserProfile: React.FC<UserProfileProps> = ({setPaidReservationId}) => {
    const context = useContext(UserContext)

    const user = context.user as User;
    const userId = context.userId
    const role = context.role as string[] | null;

    const [userData, setUserData] = useState<Customer | Employee | null>(null);
    const [loading, setLoading] = useState(false);
    const [editing, setEditing] = useState(false);
    const [formData, setFormData] = useState<Partial<Customer & Employee>>({});
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchUserData = async () => {
            setLoading(true);
            setError(null);
            try {
                if (role?.includes("Customer")) {
                    const customer = await CustomerAPI.getCustomer(userId);
                    console.log(customer)
                    setUserData(customer);
                } else {
                    const employee = await EmployeeAPI.getEmployee(userId);
                    setUserData(employee);
                }
            } catch (err) {
                console.error("Error fetching user data:", err);
                setError("Failed to load user information.");
            } finally {
                setLoading(false);
            }
        };

        fetchUserData();
    }, [userId]);

    const handleEdit = () => {
        setFormData(userData || {});
        setEditing(true);
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;

        const genericUserFields = ["name", "surname", "email", "phone", "address", "city"];

        if (genericUserFields.includes(name)) {
            setFormData(prev => ({
                ...prev,
                genericUserData: {
                    ...prev.genericUserData,
                    [name]: value,
                } as genericUserData,
            }));
        } else {
            setFormData(prev => ({
                ...prev,
                [name]: value,
            }));
        }
    };

    const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const { name, value } = e.target;

        if (name === "nameRole") {
            setFormData(prev => ({
                ...prev,
                role: {
                    ...prev.role,
                    [name]: value,
                } as Role,
            }));
        } else {
            //never used at the moment
            setFormData(prev => ({
                ...prev,
                [name]: value,
            }));
        }
    };

    const handleSave = async () => {
        if (!formData.id) return;

        try {
            setLoading(true);
            if (role?.includes("Customer")) {
                await CustomerAPI.modifyCustomer(formData as Customer, user?.csrf as string);
            } else {
                await EmployeeAPI.modifyEmployee(formData as Employee, user?.csrf as string);
            }
            setUserData(formData as Customer | Employee);
            setEditing(false);
        } catch (err) {
            console.error("Error updating user:", err);
            setError("Failed to save changes.");
        } finally {
            setLoading(false);
        }
    };

    if (loading && !editing) {
        return (
            <div className="text-center mt-4">
                <Spinner animation="border"/>
            </div>
        );
    }

    if (error) {
        return (
            <div className="text-center text-danger mt-4">
                {error}
            </div>
        );
    }

    if (!userData) {
        return null;
    }

    return (
        <Card className="p-4 shadow-sm">
            <Card.Title className="text-center mb-4">User Profile</Card.Title>
            {editing ? (
                <Form>
                    <Row className="mb-3">
                        <Col md={6}>
                            <Form.Group controlId="formName">
                                <Form.Label>Name</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="name"
                                    value={formData.genericUserData?.name || ""}
                                    onChange={handleInputChange}
                                />
                            </Form.Group>
                        </Col>
                        <Col md={6}>
                            <Form.Group controlId="formSurname">
                                <Form.Label>Surname</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="surname"
                                    value={formData.genericUserData?.surname || ""}
                                    onChange={handleInputChange}
                                />
                            </Form.Group>
                        </Col>
                    </Row>
                    <Row className="mb-3">
                        <Col md={3}>
                            <Form.Group controlId="formEmail">
                                <Form.Label>Email</Form.Label>
                                <Form.Control
                                    type="email"
                                    name="email"
                                    value={formData.genericUserData?.email || ""}
                                    onChange={handleInputChange}
                                />
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group controlId="formPhone">
                                <Form.Label>Phone</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="phone"
                                    value={formData.genericUserData?.phone || ""}
                                    onChange={handleInputChange}
                                />
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group controlId="formAddress">
                                <Form.Label>Address</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="address"
                                    value={formData.genericUserData?.address || ""}
                                    onChange={handleInputChange}
                                />
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group controlId="formCity">
                                <Form.Label>City</Form.Label>
                                <Form.Control
                                    type="text"
                                    name="city"
                                    value={formData.genericUserData?.city || ""}
                                    onChange={handleInputChange}
                                />
                            </Form.Group>
                        </Col>
                    </Row>
                    {"dateOfBirth" in formData && (
                        <Row className="mb-3">
                            <Col md={4}>
                                <Form.Group controlId="formDateOfBirth">
                                    <Form.Label>Date of Birth</Form.Label>
                                    <Form.Control
                                        type="date"
                                        name="dateOfBirth"
                                        value={formData.dateOfBirth || ""}
                                        onChange={handleInputChange}
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group controlId="formDrivingLicence">
                                    <Form.Label>Driving licence</Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="drivingLicence"
                                        value={formData.drivingLicence || ""}
                                        onChange={handleInputChange}
                                    />
                                </Form.Group>
                            </Col>
                            <Col md={4}>
                                <Form.Group controlId="formExpirationDate">
                                    <Form.Label>Driving licence expiration date</Form.Label>
                                    <Form.Control
                                        type="date"
                                        name="expirationDate"
                                        value={formData.expirationDate || ""}
                                        onChange={handleInputChange}
                                    />
                                </Form.Group>
                            </Col>
                        </Row>
                    )}
                    {"role" in formData && (
                        <>
                            <Row className="mb-3">
                                <Col md={6}>
                                    <Form.Group controlId="formRole">
                                        <Form.Label>Role</Form.Label>
                                        <Form.Select
                                            name="nameRole"
                                            value={formData.role?.nameRole}
                                            onChange={handleSelectChange}
                                        >
                                            <option value="">Select a role</option>
                                            <option value="Staff">Staff</option>
                                            <option value="Manager">Manager</option>
                                            <option value="Fleet manager">Fleet Manager</option>
                                        </Form.Select>
                                    </Form.Group>
                                </Col>
                                <Col md={6}>
                                    <Form.Group controlId="formSalary">
                                        <Form.Label>Salary</Form.Label>
                                        <Form.Control
                                            type="number"
                                            name="salary"
                                            value={formData.salary || ""}
                                            onChange={handleInputChange}
                                        />
                                    </Form.Group>
                                </Col>
                            </Row>
                        </>
                    )}
                    <div className="text-end mt-3">
                        <Button variant="danger" onClick={() => {
                            setEditing(false);
                            setFormData({})
                        }}>
                            Cancel
                        </Button>
                        <Button variant="success" onClick={handleSave}>
                            Save
                        </Button>
                    </div>
                </Form>
            ) : (
                <>
                    <Row>
                        <Col md={6}>
                            <strong>Name:</strong> {userData.genericUserData.name}
                        </Col>
                        <Col md={6}>
                            <strong>Surname:</strong> {userData.genericUserData.surname}
                        </Col>
                    </Row>
                    <Row>
                        <Col md={6}>
                            <strong>Email:</strong> {userData.genericUserData.email}
                        </Col>
                        <Col md={6}>
                            <strong>Address:</strong> {userData.genericUserData.address}
                        </Col>
                    </Row>
                    <Row>
                        <Col md={6}>
                            <strong>Phone:</strong> {userData.genericUserData.phone}
                        </Col>
                        <Col md={6}>
                            <strong>City:</strong> {userData.genericUserData.city}
                        </Col>
                    </Row>

                    {"dateOfBirth" in userData && ( //if is a customer
                        <>
                            <Row>
                                <Col md={6}>
                                    <strong>Date of Birth:</strong> {userData.dateOfBirth}
                                </Col>
                                <Col md={6}>
                                    <strong>Driving licence:</strong> {userData.drivingLicence}
                                </Col>
                                <Col md={6}>
                                    <strong>Driving licence expiration date:</strong> {userData.expirationDate}
                                </Col>
                                <Col md={6}>
                                    <strong>Reliability score:</strong> {userData.reliabilityScores}
                                </Col>
                            </Row>

                            <div className="text-end mt-4">
                                <Button variant="primary" onClick={handleEdit}>
                                    Edit
                                </Button>
                            </div>
                            <Row>
                                {/* one table for present and past reservations */}
                                <ReservationTable
                                    isCustomerView={true}
                                    setPaidReservationId={setPaidReservationId}
                                />
                            </Row>
                        </>
                    )}
                    {"role" in userData && (    //if is an employee
                        <>
                            <Row>
                                <Col md={6}>
                                    <strong>Role:</strong> {userData.role.nameRole}
                                </Col>
                                <Col md={6}>
                                    <strong>Salary:</strong> €{userData.salary}
                                </Col>
                            </Row>


                            <div className="text-end mt-4">
                                <Button variant="primary" onClick={handleEdit}>
                                    Edit
                                </Button>
                            </div>
                        </>

                    )}
                </>
            )}
        </Card>
    );
};


export default UserProfile;