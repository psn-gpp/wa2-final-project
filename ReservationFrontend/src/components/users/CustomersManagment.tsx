import React, {useContext, useEffect, useState} from 'react';
import {Table, Button, Spinner, Pagination, Form, Row, Col} from 'react-bootstrap';
import CustomerModal from "./CustomerModal.tsx";
import {Customer, User} from "../../types.ts";
import CustomerAPI from "../../api/customerAPI.ts";
import SortIcon from "../SortIcon.tsx";
import {UserContext} from "../../App.tsx";

interface CustomerManagementProps {
}

const CustomerManagement: React.FC<CustomerManagementProps> = () => {
    const [customers, setCustomers] = useState<Customer[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [showModal, setShowModal] = useState<boolean>(false);
    const [newCustomer, setNewCustomer] = useState<Customer>({
        id: 0,
        genericUserData: {
            id: 0,
            name: '',
            surname: '',
            email: '',
            phone: '',
            address: '',
            city: ''
        },
        dateOfBirth: '',
        reliabilityScores: 0,
        drivingLicence: '',
        expirationDate: ''
    });

    const [filters, setFilters] = useState<any>({}); // Filter parameters
    const [sort, setSort] = useState<string[]>([]); // Sorting parameters
    const [page, setPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [tempFilters, setTempFilters] = useState<any>({});

    const context = useContext(UserContext);
    const user = context.user as User;

    const [errors, setErrors] = useState({
        email: '',
        phone: ''
    });

    useEffect(() => {
        const fetchCustomers = async () => {
            setLoading(true);
            try {
                const {content, totalPages} = await CustomerAPI.getCustomers(filters, page, 10, sort);
                setCustomers(content);
                setTotalPages(totalPages);
            } catch (error) {
                console.error("Error fetching customers:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchCustomers();
    }, [filters, sort, page]);

    const handleDelete = async (customerId: number) => {
        try {
            // Call API to delete customer
            await CustomerAPI.deleteCustomer(customerId, user?.csrf as string);
            setCustomers(customers.filter(c => c.id !== customerId)); // Remove customer from state
        } catch (error) {
            console.error("Error deleting customer:", error);
        }
    };

    const handleAddCustomer = async () => {
        try {
            if(errors.phone || errors.email) return

            const {location, data} = await CustomerAPI.addCustomer(newCustomer, user?.csrf as string);
            if (!location || !data) {
                throw new Error("Error in the response");
            }
            setCustomers((prev) => [...prev, data])

            setShowModal(false);
            setNewCustomer({
                id: 0,
                genericUserData: {
                    id: 0,
                    name: '',
                    surname: '',
                    email: '',
                    phone: '',
                    address: '',
                    city: ''
                },
                dateOfBirth: '',
                reliabilityScores: 0,
                drivingLicence: '',
                expirationDate: ''
            });
            setPage(0); // Reset to first page
        } catch (error) {
            console.error("Error adding customer:", error);
        }
    };

    const handleNewCustomerChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;

        const genericUserFields = ["name", "surname", "email", "phone", "address", "city"];

        if (genericUserFields.includes(name)) {
            setNewCustomer(prev => ({
                ...prev,
                genericUserData: {
                    ...prev.genericUserData,
                    [name]: value,
                },
            }));
        } else if (name === "reliabilityScores") {
            setNewCustomer(prev => ({
                ...prev,
                [name]: Number(value),
            }));
        } else {
            // Per tutti gli altri campi
            setNewCustomer(prev => ({
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
        }else if(name === 'phone') {
            const vinRegex = /^\d{3}\s\d{3}\s\d{4}$/;
            if (!vinRegex.test(value)) {
                setErrors({...errors, phone: 'phone not valid. It must be composed by 10 numeric digits in the format: "111 111 1111".'});
            } else {
                setErrors({...errors, phone: ''});
            }
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
                <Row>
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
                                    onChange={(e) => setTempFilters((prev: any) => ({
                                        ...prev,
                                        surname: e.target.value
                                    }))}
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
                        <Col>
                            <Form.Group controlId="Address">
                                <Form.Label>Address</Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="Address"
                                    value={tempFilters.address || ''}
                                    onChange={(e) => setTempFilters((prev: any) => ({...prev, city: e.target.value}))}
                                />
                            </Form.Group>
                        </Col>
                    </Row>
                    <Row className="mt-2">
                        <Col>
                            <Form.Group controlId="DateOfBirth">
                                <Form.Label>Date of birth</Form.Label>
                                <Form.Control
                                    type="date"
                                    placeholder="Date of Birth"
                                    value={tempFilters.dateOfBirth || ''}
                                    onChange={(e) => setTempFilters((prev: any) => ({
                                        ...prev,
                                        dateOfBirth: e.target.value
                                    }))}
                                />
                            </Form.Group>
                        </Col>
                        <Col>
                            <Form.Group controlId="RelaiabilityScores">
                                <Form.Label>Reliability scores</Form.Label>
                                <Form.Control
                                    type="number"
                                    placeholder="Reliability Score"
                                    value={tempFilters.reliabilityScores || ''}
                                    onChange={(e) => setTempFilters((prev: any) => ({
                                        ...prev,
                                        reliabilityScores: e.target.value
                                    }))}
                                />
                            </Form.Group>
                        </Col>
                        <Col>
                            <Form.Group controlId="DrivingLicence">
                                <Form.Label>Driving licence</Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="Driving Licence"
                                    value={tempFilters.drivingLicence || ''}
                                    onChange={(e) => setTempFilters((prev: any) => ({
                                        ...prev,
                                        drivingLicence: e.target.value
                                    }))}
                                />
                            </Form.Group>
                        </Col>
                        <Col>
                            <Form.Group controlId="ExipartionDate">
                                <Form.Label>Driving licence expiration date</Form.Label>
                                <Form.Control
                                    type="date"
                                    placeholder="Expiration Date"
                                    value={tempFilters.expirationDate || ''}
                                    onChange={(e) => setTempFilters((prev: any) => ({
                                        ...prev,
                                        expirationDate: e.target.value
                                    }))}
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
                </Row>
            </Form>
            <Button variant="primary" onClick={() => setShowModal(true)}>Add Customer</Button>
            <Table striped bordered hover responsive className="mt-3">
                <thead>
                <tr>
                    <th onClick={() => handleSort('genericUserData.name')}>
                        Name <SortIcon field="genericUserData.name" sort={sort} />
                    </th>
                    <th onClick={() => handleSort('genericUserData.surname')}>
                        Surname <SortIcon field="genericUserData.surname" sort={sort} />
                    </th>
                    <th onClick={() => handleSort('genericUserData.email')}>
                        Email <SortIcon field="genericUserData.email" sort={sort} />
                    </th>
                    <th onClick={() => handleSort('genericUserData.phone')}>
                        Phone <SortIcon field="genericUserData.phone" sort={sort} />
                    </th>
                    <th onClick={() => handleSort('genericUserData.address')}>
                        Address <SortIcon field="genericUserData.address" sort={sort} />
                    </th>
                    <th onClick={() => handleSort('genericUserData.city')}>
                        City <SortIcon field="genericUserData.city" sort={sort} />
                    </th>
                    <th onClick={() => handleSort('dateOfBirth')}>
                        Date of Birth <SortIcon field="dateOfBirth" sort={sort} />
                    </th>
                    <th onClick={() => handleSort('reliabilityScores')}>
                        Reliability Score <SortIcon field="reliabilityScores" sort={sort} />
                    </th>
                    <th onClick={() => handleSort('drivingLicence')}>
                        Driving Licence <SortIcon field="drivingLicence" sort={sort} />
                    </th>
                    <th onClick={() => handleSort('expirationDate')}>
                        Expiration Date <SortIcon field="expirationDate" sort={sort} />
                    </th>
                    <th>Delete</th>
                </tr>
                </thead>
                <tbody>
                {customers.map(customer => (
                    <tr key={customer.id}>
                        <td>{customer.genericUserData.name}</td>
                        <td>{customer.genericUserData.surname}</td>
                        <td>{customer.genericUserData.email}</td>
                        <td>{customer.genericUserData.phone}</td>
                        <td>{customer.genericUserData.address}</td>
                        <td>{customer.genericUserData.city}</td>
                        <td>{customer.dateOfBirth}</td>
                        <td>{customer.reliabilityScores}</td>
                        <td>{customer.drivingLicence}</td>
                        <td>{customer.expirationDate}</td>
                        <td>
                            <Button variant="danger" onClick={() => handleDelete(customer.id!)}>Delete</Button>
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

            <CustomerModal
                show={showModal}
                onClose={() => setShowModal(false)}
                onAddCustomer={handleAddCustomer}
                newCustomer={newCustomer}
                handleChange={handleNewCustomerChange}
                errors={errors}
            />
        </>
    );
};

export default CustomerManagement;