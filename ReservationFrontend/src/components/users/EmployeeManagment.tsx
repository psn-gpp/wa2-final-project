import React, {useContext, useEffect, useState} from 'react';
import {Table, Button, Spinner, Pagination, Row, Col, Form} from 'react-bootstrap';
import EmployeeAPI from "../../api/employeeAPI.ts";
import EmployeeModal from "./EmployeeModal.tsx";
import {Employee, genericUserData, Role, User} from "../../types.ts";
import SortIcon from "../SortIcon.tsx";
import {UserContext} from "../../App.tsx";

const EmployeeManagement: React.FC = () => {
    const context = useContext(UserContext);
    const user = context.user as User;

    const [employees, setEmployees] = useState<Employee[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [showModal, setShowModal] = useState<boolean>(false);
    const [newEmployee, setNewEmployee] = useState<Employee>({
        id:0,
        genericUserData: {
            id:0,
            name: '',
            surname: '',
            email: '',
            phone: '',
            address: '',
            city: ''
        },
        role: {id: 0, nameRole: ''},
        salary: 0
    });

    const [filters, setFilters] = useState<any>({});
    const [sort, setSort] = useState<string[]>([]);
    const [page, setPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [tempFilters, setTempFilters] = useState<any>({});

    const [errors, setErrors] = useState({
        email: '',
        phone: ''
    });

    useEffect(() => {
        const fetchEmployees = async () => {
            setLoading(true);
            try {
                const {content, totalPages} = await EmployeeAPI.getEmployees(filters, page, 10, sort);
                setEmployees(content);
                setTotalPages(totalPages);
            } catch (error) {
                console.error("Error fetching employees:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchEmployees();
    }, [filters, sort, page]);

    const handleDelete = async (employeeId: number) => {
        try {
            await EmployeeAPI.deleteEmployee(employeeId, user.csrf as string);
            setEmployees(employees.filter(e => e.id !== employeeId));
        } catch (error) {
            console.error("Error deleting employee:", error);
        }
    };

    const handleAddEmployee = async (newEmployee: Employee) => {
        try {
            if(errors.phone || errors.email) return

            const {location, data} = await EmployeeAPI.addEmployee(newEmployee, user.csrf as string);
            console.log(location)
            if (!location || !data) {
                throw new Error("Error in the response");
            }
            setEmployees((prev) => [...prev, data])

            setShowModal(false);
            setNewEmployee({
                id:0,
                genericUserData: {
                    id:0,
                    name: '',
                    surname: '',
                    email: '',
                    phone: '',
                    address: '',
                    city: ''
                },
                role: {id: 0, nameRole: ''},
                salary: 0
            });
            setPage(0);
        } catch (error) {
            console.error("Error adding employee:", error);
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;

        const genericUserFields = ["name", "surname", "email", "phone", "address", "city"];

        if (genericUserFields.includes(name)) {
            setNewEmployee(prev => ({
                ...prev,
                genericUserData: {
                    ...prev.genericUserData,
                    [name]: value,
                } as genericUserData,
            }));
        } else if(name==="salary") {
            setNewEmployee(prev => ({
                ...prev,
                [name]: Number(value),
            }));
        } else if(name == "phone" && (value.length === 3 || value.length === 7)) { // Add space after 3rd and 7th character

            setNewEmployee(prev => ({
                ...prev,
                phone: value + ' '
            }));

        } else {
            setNewEmployee(prev => ({
                ...prev,
                [name]: value,
            }));
        }

        if (name === 'email') {
            const vinRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!vinRegex.test(value)) {
                setErrors({...errors, email: 'email not valid'});
            } else {
                setErrors({...errors, email: ''});
            }
        }/*else if(name === 'phone') {
            const vinRegex = /^\d{3}\s\d{3}\s\d{4}$/;
            if (!vinRegex.test(value)) {
                setErrors({...errors, phone: 'phone not valid. It must be composed by 10 numeric digits in the format: "111 111 1111".'});
            } else {
                setErrors({...errors, phone: ''});
            }
        }*/
    };

    const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const { name, value } = e.target;

        if (name === "nameRole") {
            setNewEmployee(prev => ({
                ...prev,
                role: {
                    ...prev.role,
                    [name]: value,
                } as Role,
            }));
        } else {
            //never used at the moment
            setNewEmployee(prev => ({
                ...prev,
                [name]: value,
            }));
        }
    };

    const handleSort = (field: string) => {
        setSort(prevSort => {
            const existingIndex = prevSort.findIndex(sortItem => sortItem.startsWith(`${field},`));
            if (existingIndex !== -1) {
                const [_, direction] = prevSort[existingIndex].split(',');
                const newDirection = direction === 'asc' ? 'desc' : 'asc';
                const newSortItem = `${field},${newDirection}`;

                // Sostituisco il vecchio con il nuovo sort
                const updatedSort = [...prevSort];
                updatedSort[existingIndex] = newSortItem;
                return updatedSort;
            } else {
                // Aggiungo nuovo campo ordinato asc
                return [...prevSort, `${field},asc`];
            }
        });
    };

    if (loading) {
        return <Spinner animation="border"/>;
    }

    return (
        <>
            <Form className="mt-3 mb-3">

                <Row className="mt-2">
                    <Col>
                        <Form.Group controlId="Name">
                            <Form.Label>Name</Form.Label>
                            <Form.Control
                                type="text"
                                placeholder="Name"
                                value={tempFilters.name || ''}
                                onChange={(e) => setTempFilters((prev: any) => ({...prev, name: e.target.value}))}
                            />
                        </Form.Group>
                    </Col>
                    <Col>
                        <Form.Group controlId="Surname">
                            <Form.Label>Surname</Form.Label>
                            <Form.Control
                                type="text"
                                placeholder="Surname"
                                value={tempFilters.surname || ''}
                                onChange={(e) => setTempFilters((prev: any) => ({...prev, surname: e.target.value}))}
                            />
                        </Form.Group>
                    </Col>
                    <Col>
                        <Form.Group controlId="City">
                            <Form.Label>City</Form.Label>
                            <Form.Control
                                type="text"
                                placeholder="City"
                                value={tempFilters.city || ''}
                                onChange={(e) => setTempFilters((prev: any) => ({...prev, city: e.target.value}))}
                            />
                        </Form.Group>
                    </Col>
                </Row>
                <Row className="mt-2">
                    <Col>
                        <Form.Group controlId="Address">
                            <Form.Label>Address</Form.Label>
                            <Form.Control
                                type="text"
                                placeholder="Address"
                                value={tempFilters.address || ''}
                                onChange={(e) => setTempFilters((prev: any) => ({...prev, address: e.target.value}))}
                            />
                        </Form.Group>
                    </Col>

                    <Col>
                        <Form.Group controlId="Role">
                            <Form.Label>Role</Form.Label>
                            <Form.Select
                                name="nameRole"
                                value={tempFilters.nameRole || ''}
                                /*onChange={(e) => setTempFilters((prev: any) => (
                                    {...prev,
                                    role: {
                                        ...prev.role,
                                        [e.target.name]: e.target.value,
                                    } as Role}
                                ))}*/
                                onChange={(e)=> setTempFilters((prev: any) => (
                                    {...prev, [e.target.name]:e.target.value}
                                ))}
                            >
                                <option value="">Select a role</option>
                                <option value="Staff">Staff</option>
                                <option value="Manager">Manager</option>
                                <option value="Fleet Manager">Fleet Manager</option>
                            </Form.Select>
                        </Form.Group>
                    </Col>
                    <Col>
                        <Form.Group controlId="Salary">
                            <Form.Label>Salary</Form.Label>
                            <Form.Control
                                type="number"
                                placeholder="Salary"
                                value={tempFilters.salary || ''}
                                onChange={(e) => setTempFilters((prev: any) => ({...prev, salary: e.target.value}))}
                            />
                        </Form.Group>
                    </Col>

                </Row>
                <Row className="mt-2">
                    <Col>
                        <Button variant="primary" onClick={() => {
                            setFilters(tempFilters);
                            setPage(0);
                        }}>
                            Filter
                        </Button>{' '}
                        <Button variant="secondary" onClick={() => {
                            setTempFilters({});
                            setFilters({});
                            setSort([])
                            setPage(0);
                        }}>
                            Reset
                        </Button>
                    </Col>
                </Row>
            </Form>
            <Button variant="primary" onClick={() => setShowModal(true)}>Add Employee</Button>
            <Table striped bordered hover responsive className="mt-3">
                <thead>
                <tr>
                    <th onClick={() => handleSort('genericUserData.name')}>
                        Name <SortIcon field="genericUserData.name" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('genericUserData.surname')}>
                        Surname <SortIcon field="genericUserData.surname" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('genericUserData.email')}>
                        Email <SortIcon field="genericUserData.email" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('genericUserData.phone')}>
                        Phone <SortIcon field="genericUserData.phone" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('genericUserData.address')}>
                        Address <SortIcon field="genericUserData.address" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('genericUserData.city')}>
                        City <SortIcon field="genericUserData.city" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('role')}>
                        Role <SortIcon field="role" sort={sort}/>
                    </th>
                    <th onClick={() => handleSort('salary')}>
                        Salary <SortIcon field="salary" sort={sort}/>
                    </th>
                    <th>Delete</th>
                </tr>
                </thead>
                <tbody>
                {employees.map(employee => (
                    <tr key={employee.id}>
                        <td>{employee.genericUserData.name}</td>
                        <td>{employee.genericUserData.surname}</td>
                        <td>{employee.genericUserData.email}</td>
                        <td>{employee.genericUserData.phone}</td>
                        <td>{employee.genericUserData.address}</td>
                        <td>{employee.genericUserData.city}</td>
                        <td>{employee.role?.nameRole || ""}</td>
                        <td>{employee.salary}</td>
                        <td>
                            <Button variant="danger" onClick={() => handleDelete(employee.id!)}>Delete</Button>
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

            <EmployeeModal
                show={showModal}
                onClose={() => setShowModal(false)}
                onAddEmployee={handleAddEmployee}
                newEmployee={newEmployee}
                handleChange={handleChange}
                handleSelectChange={handleSelectChange}
                errors={{errors}}
            />
        </>
    );
};

export default EmployeeManagement;