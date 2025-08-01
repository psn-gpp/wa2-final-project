import {Card, Row, Col, Button} from 'react-bootstrap'
import React from "react";
import {useNavigate} from "react-router-dom";

type CarModel = {
    id: number;
    brand: string;
    model: string;
    modelYear: number;
};

interface CarGridProps {
    carModels: CarModel[];  // Definisce carModels come array di CarModel
}

const CarGrid: React.FC<CarGridProps> = ({ carModels })=> {
    const navigate = useNavigate();

    return (
        <Row xs={1} md={3} className="g-4">
            {carModels.map((car:CarModel) => (
                <Col key={car.id}>
                    <Card style={{ width: '18rem' }}>
                        <Card.Body>
                            <Card.Title>{car.model}</Card.Title>
                            <Card.Text>
                                <h2 className="text-lg font-bold">{car.brand}</h2>
                                <p className="text-gray-500">Anno: {car.modelYear}</p>
                            </Card.Text>
                            <Button variant="primary" onClick={() => navigate(`/ui/carModels/${car.id}`)}>
                                Show details
                            </Button>
                        </Card.Body>
                    </Card>
                </Col>
            ))}
        </Row>
    );
}

export default CarGrid;