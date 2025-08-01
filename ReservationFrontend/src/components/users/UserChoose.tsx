import React, { Dispatch, SetStateAction, useEffect, useState } from "react";
import { Button, Col, Form, Pagination, Row, Spinner, Table } from "react-bootstrap";
import { Employee, UserOption } from "../../types.ts";
import { useNavigate } from "react-router-dom";
import CustomerAPI from "../../api/customerAPI.ts";
import EmployeeAPI from "../../api/employeeAPI.ts";

interface UserChooseProps {
    setUser: Dispatch<SetStateAction<UserOption>>;
}

const UserChoose: React.FC<UserChooseProps> = ({ setUser }) => {
    const [users, setUsers] = useState<UserOption[]>([]);
    const [selectedUser, setSelectedUser] = useState<number | undefined>();
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const [filters, setFilters] = useState<any>({});
    const [tempFilters, setTempFilters] = useState<any>({});
    const [page, setPage] = useState<number>(0);

    const [totalCustomers, setTotalCustomers] = useState<number>(0);
    const [totalEmployees, setTotalEmployees] = useState<number>(0);
    //const [totalElements, setTotalElements] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);

    const pageSize = 5;

    useEffect(() => {
        const fetchCounts = async () => {
            let total = 0;
            let totalCust = 0;
            let totalEmp = 0;

            const roleFilter = filters.nameRole;

            if (roleFilter === "Customer" || !roleFilter) {
                const resCustomers = await CustomerAPI.getCustomers(filters, 0, 1, []);
                totalCust = resCustomers.totalElements;
            }

            if (roleFilter !== "Customer") {
                const resEmployees = await EmployeeAPI.getEmployees(filters, 0, 1, []);
                totalEmp = resEmployees.totalElements;
            }

            total = totalCust + totalEmp;

            setTotalCustomers(totalCust);
            setTotalEmployees(totalEmp);
            setTotalPages(Math.ceil(total / pageSize));
            setPage(0);
        };
        fetchCounts();
    }, [filters/*, sort*/]);

    useEffect(() => {
        fetchUsers();
    }, [page, totalCustomers, totalEmployees]);

    const fetchUsers = async () => {
        setLoading(true);
        const startIndex = page * pageSize;

        const roleFilter = filters.nameRole;
        const onlyEmployees = roleFilter && roleFilter !== "Customer";

        try {
            const results: UserOption[] = [];

            if (!onlyEmployees) {
                if (startIndex < totalCustomers) {
                    const customerPage = Math.floor(startIndex / pageSize);
                    const customerOffset = startIndex % pageSize;

                    const customerFilters = { ...filters };
                    delete customerFilters.nameRole;

                    const resCustomers = await CustomerAPI.getCustomers(customerFilters, customerPage, pageSize, []);
                    const customerData = resCustomers.content.slice(customerOffset, customerOffset + pageSize).map(mapCustomer);

                    results.push(...customerData);
                }
            }

            if (onlyEmployees || results.length < pageSize) {
                const employeeStart = onlyEmployees ? startIndex : Math.max(0, startIndex - totalCustomers);
                const employeePage = Math.floor(employeeStart / pageSize);
                const employeeOffset = employeeStart % pageSize;

                const resEmployees = await EmployeeAPI.getEmployees(filters, employeePage, pageSize, []);
                const employeeData = resEmployees.content.slice(employeeOffset, employeeOffset + (pageSize - results.length)).map(mapEmployee);

                results.push(...employeeData);
            }

            setUsers(results);
        } catch (err) {
            console.error("Error loading unified users:", err);
        } finally {
            setLoading(false);
        }
    };

    const mapCustomer = (c: any): UserOption => ({
        id: c.id ?? 0,
        name: c.genericUserData.name,
        surname: c.genericUserData.surname,
        role: "Customer",
    });

    const mapEmployee = (e: Employee): UserOption => ({
        id: e.id ?? 0,
        name: e.genericUserData.name,
        surname: e.genericUserData.surname,
        role: e.role.nameRole,
    });


    const handleConfirm = () => {
        const user = users.find((u) => u.id === selectedUser);
        if (user) {
            setUser(user);
            navigate("/ui/carModels");
        }
    };

    if (loading) return <Spinner animation="border" />;

    return (
        <>
            <Form className="mt-3 mb-3 ms-3">
                <Row className="mt-2">
                    <Col>
                        <Form.Control
                            type="text"
                            placeholder="Name"
                            value={tempFilters.name || ''}
                            onChange={(e) => setTempFilters((prev: any) => ({ ...prev, name: e.target.value }))}
                        />
                    </Col>
                    <Col>
                        <Form.Control
                            type="text"
                            placeholder="Surname"
                            value={tempFilters.surname || ''}
                            onChange={(e) => setTempFilters((prev: any) => ({ ...prev, surname: e.target.value }))}
                        />
                    </Col>
                    {/*<Col>
                        <Form.Control
                            type="text"
                            placeholder="City"
                            value={tempFilters.city || ''}
                            onChange={(e) => setTempFilters((prev: any) => ({ ...prev, city: e.target.value }))}
                        />
                    </Col>
                </Row>
                <Row className="mt-2">
                    <Col>
                        <Form.Control
                            type="text"
                            placeholder="Address"
                            value={tempFilters.address || ''}
                            onChange={(e) => setTempFilters((prev: any) => ({ ...prev, address: e.target.value }))}
                        />
                    </Col>*/}
                    <Col>
                        <Form.Select
                            name="nameRole"
                            value={tempFilters.nameRole || ''}
                            onChange={(e) =>
                                setTempFilters((prev: any) => ({ ...prev, [e.target.name]: e.target.value }))
                            }
                        >
                            <option value="">Select a role</option>
                            <option value="Staff">Staff</option>
                            <option value="Manager">Manager</option>
                            <option value="Fleet manager">Fleet Manager</option>
                            <option value="Customer">Customer</option>
                        </Form.Select>
                    </Col>
                </Row>
                <Row className="mt-2">
                    <Col>
                        <Button
                            variant="primary"
                            onClick={() => {
                                setFilters(tempFilters);
                                setPage(0);
                            }}
                        >
                            Filter
                        </Button>{' '}
                        <Button
                            variant="secondary"
                            onClick={() => {
                                setTempFilters({});
                                setFilters({});
                                setPage(0);
                            }}
                        >
                            Reset
                        </Button>
                    </Col>
                </Row>
            </Form>

            <div className="p-3">
                <Table striped bordered hover responsive className="mt-3">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Surname</th>
                        <th>Role</th>
                    </tr>
                    </thead>
                    <tbody>
                    {users.map(user => (
                        <tr key={user.id} onClick={() => setSelectedUser(user.id)}>
                            <td>{user.name}</td>
                            <td>{user.surname}</td>
                            <td>{user.role}</td>
                        </tr>
                    ))}
                    </tbody>
                </Table>

                <Pagination className="justify-content-center">
                    <Pagination.First onClick={() => setPage(0)} disabled={page === 0} />
                    <Pagination.Prev onClick={() => setPage(p => Math.max(p - 1, 0))} disabled={page === 0} />
                    {[...Array(totalPages)].map((_, idx) => (
                        <Pagination.Item key={idx} active={idx === page} onClick={() => setPage(idx)}>
                            {idx + 1}
                        </Pagination.Item>
                    ))}
                    <Pagination.Next onClick={() => setPage(p => Math.min(p + 1, totalPages - 1))} disabled={page === totalPages - 1} />
                    <Pagination.Last onClick={() => setPage(totalPages - 1)} disabled={page === totalPages - 1} />
                </Pagination>

                <div className="text-end">
                    <Button variant="primary" onClick={handleConfirm} disabled={selectedUser === undefined}>
                        Login
                    </Button>
                </div>
            </div>
        </>
    );
};

export default UserChoose;