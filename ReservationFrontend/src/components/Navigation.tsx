import {Button, Col, Container, Nav, Navbar, Row} from 'react-bootstrap';
import {useNavigate} from "react-router-dom";
import {useContext} from "react";
import {UserContext} from "../App.tsx";
import {User} from "../types.ts";


interface NavigationProps {
}

const Navigation: React.FC<NavigationProps> = () => {
    const navigate = useNavigate();

    const context = useContext(UserContext)
    const user = context.user as User;
    const fullName = user.userInfo?.claims?.given_name + " " + user.userInfo?.claims?.family_name;

    const role = context.role as string[] | null;
    const userId = context.userId as number;

    console.log(role)

    console.log("UserContext in Navigation:", user);

    return (
        <Navbar expand="lg" className="bg-body-tertiary w-100">
            <Container fluid>
                <Navbar.Brand>Ez-carRent</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav"/>
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        <Button
                            variant="link"
                            className="nav-link"
                            onClick={() => navigate('/ui/carModels')}
                        >
                            Car Models
                        </Button>

                        {role?.includes("Staff")  && user.name ?
                            <>
                                <Button
                                    variant="link"
                                    className="nav-link"
                                    onClick={() => navigate('/ui/reservations')}
                                >
                                    Reservations Management
                                </Button>
                           </>
                        : null}
                        {role?.includes("Manager") &&
                            <>
                                <Button
                                    variant="link"
                                    className="nav-link"
                                    onClick={() => navigate('/ui/customers')}
                                >
                                    Customer Management
                                </Button>
                                <Button
                                    variant="link"
                                    className="nav-link"
                                    onClick={() => navigate('/ui/employees')}
                                >
                                    Employee Management
                                </Button>
                            </>
                        }
                        {user.name as string &&
                            <Button
                                variant="link"
                                className="nav-link"
                                onClick={() => navigate(`/ui/users/${userId}`)}
                            >
                                Profile
                            </Button>
                        }
                    </Nav>

                    { user.name &&
                        <Nav>
                            <Col className="d-flex align-items-center justify-content-center gap-1">
                                <Row><b>Hello, {fullName}</b></Row>
                                <Row className="mx-1 gap-1" style={{fontSize: "0.8rem"}}>
                                    {role?.includes("Customer") &&
                                        <Col style={{border: "1px solid black", borderRadius:"1rem", padding: "0.2rem"}}>
                                            Customer
                                        </Col>
                                    }
                                    {role?.includes("Manager") &&
                                        <Col style={{border: "1px solid black", borderRadius:"1rem", padding: "0.2rem"}}>
                                            Manager
                                        </Col>
                                    }
                                    {role?.includes("Fleet_Manager") &&
                                        <Col style={{border: "1px solid black", borderRadius:"1rem", padding: "0.2rem"}}>
                                            Fleet Manager
                                        </Col>
                                    }
                                    {role?.includes("Staff") &&
                                        <Col style={{border: "1px solid black", borderRadius:"1rem", padding: "0.2rem"}}>
                                            Staff
                                        </Col>
                                    }
                                </Row>
                            </Col>
                        </Nav>
                    }

                    <Nav className="m-2">
                        {user.name ?
                            <LogoutButton/>
                        :
                            <LoginButton/>
                        }
                    </Nav>


                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

function LoginButton() {
    return (
        <div>
            <Button onClick={() => window.location.href="/serverLogin"}>Login</Button>
        </div>
    )
}

function LogoutButton() {
    const context = useContext(UserContext)
    const me = context.user as User;

    return (
        <form action="/logout" method="post">
            <input type="hidden" name="_csrf" value={me.csrf as string} />
            <Button
                variant="outline-danger"
                type="submit"
            >
                Logout
            </Button>
        </form>
    )
}

export default Navigation;