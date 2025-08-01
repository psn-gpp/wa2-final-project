import React from 'react';
import {Form, Row, Col} from 'react-bootstrap';
import {CarModel, ModelFilters} from "../../../types.ts";



type FormCarModelProps<T extends ModelFilters | CarModel> = {
    engines: string[];
    transmissions: string[];
    drivetrains: string[];
    safetyOptions: string[];
    infotainmentOptions: string[];
    categories:string[];
    filters: T;
    setFilters: React.Dispatch<React.SetStateAction<T>>;
};

const FormCarModel = <T extends ModelFilters | CarModel>({
                                                             engines,
                                                             transmissions,
                                                             drivetrains,
                                                             safetyOptions,
                                                             infotainmentOptions,
                                                             categories,
                                                             filters,
                                                             setFilters}: FormCarModelProps<T>) => {
    //const [filters, setFilters] = useState<ModelFilters>({});

    // Gestione della modifica dei campi
    const handleTextChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFilters((prev) => ({...prev, [name]: value}));
    };

    const handleNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setFilters((prev) => ({...prev, [name]: value ? Number(value) : undefined}));
    };

    const handleBooleanChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, checked} = e.target;
        setFilters((prev) => ({...prev, [name]: checked}));
    };

    const handleArrayChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, checked } = e.target;

        // Assicurati che `name` sia una delle chiavi di `ModelFilters`
        setFilters((prev) => {
            // Tipo di `name` dichiarato come `keyof ModelFilters`
            const key = name as keyof ModelFilters;

            // Se il campo è di tipo string[], gestiamo l'array
            const currentValues = (prev[key] as string[]) || [];

            return {
                ...prev,
                [key]: checked ? [...currentValues, value] : currentValues.filter((v) => v !== value),
            };
        });
    };

    return (
        <>
            <Row>
                {/* Campo Brand */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="brand">
                        <Form.Label>Brand</Form.Label>
                        <Form.Control
                            type="text"
                            name="brand"
                            value={filters.brand || ''}
                            onChange={handleTextChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Model */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="model">
                        <Form.Label>Model</Form.Label>
                        <Form.Control
                            type="text"
                            name="model"
                            value={filters.model || ''}
                            onChange={handleTextChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Manufacturer */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="manufacturer">
                        <Form.Label>Manufacturer</Form.Label>
                        <Form.Control
                            type="text"
                            name="manufacturer"
                            value={filters.manufacturer || ''}
                            onChange={handleTextChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Year */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="modelYear">
                        <Form.Label>Model Year</Form.Label>
                        <Form.Control
                            type="number"
                            name="modelYear"
                            value={filters.modelYear || ''}
                            onChange={handleNumberChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Segment */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="segment">
                        <Form.Label>Segment</Form.Label>
                        <Form.Control
                            type="text"
                            name="segment"
                            value={filters.segment || ''}
                            onChange={handleTextChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Doors Number */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="doorsNo">
                        <Form.Label>Doors Number</Form.Label>
                        <Form.Control
                            type="number"
                            name="doorsNo"
                            value={filters.doorsNo || ''}
                            onChange={handleNumberChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Seating Capacity */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="seatingCapacity">
                        <Form.Label>Seating Capacity</Form.Label>
                        <Form.Control
                            type="number"
                            name="seatingCapacity"
                            value={filters.seatingCapacity || ''}
                            onChange={handleNumberChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Luggage Capacity */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="luggageCapacity">
                        <Form.Label>Luggage Capacity</Form.Label>
                        <Form.Control
                            type="number"
                            name="luggageCapacity"
                            value={filters.luggageCapacity || ''}
                            onChange={handleNumberChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Category */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="category">
                        <Form.Label>Category</Form.Label>
                        <Form.Control
                            as="select"
                            name="category"
                            value={filters.category || ''}
                            onChange={handleTextChange}
                        >
                            <option value="">Select Category</option>
                            {categories?.map((category) => (
                                <option key={category} value={category}>
                                    {category}
                                </option>
                            ))}
                        </Form.Control>
                    </Form.Group>
                </Col>

                {/* Campo Cost per Day */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="costPerDay">
                        <Form.Label>Cost per Day</Form.Label>
                        <Form.Control
                            type="number"
                            name="costPerDay"
                            value={filters.costPerDay || ''}
                            onChange={handleNumberChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Air Conditioning */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="airConditioning">
                        <Form.Label>Air Conditioning</Form.Label>
                        <Form.Check
                            type="checkbox"
                            name="airConditioning"
                            checked={filters.airConditioning || false}
                            onChange={handleBooleanChange}
                        />
                    </Form.Group>
                </Col>

                {/* Campo Engine */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="engine">
                        <Form.Label>Engine</Form.Label>
                        <Form.Control
                            as="select"
                            name="engine"
                            value={filters.engine || ''}
                            onChange={handleTextChange}
                        >
                            <option value="">Select Engine</option>
                            {engines?.map((engine) => (
                                <option key={engine} value={engine}>
                                    {engine}
                                </option>
                            ))}
                        </Form.Control>
                    </Form.Group>
                </Col>

                {/* Campo Transmission */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="transmission">
                        <Form.Label>Transmission</Form.Label>
                        <Form.Control
                            as="select"
                            name="transmission"
                            value={filters.transmission || ''}
                            onChange={handleTextChange}
                        >
                            <option value="">Select Transmission</option>
                            {transmissions?.map((transmission) => (
                                <option key={transmission} value={transmission}>
                                    {transmission}
                                </option>
                            ))}
                        </Form.Control>
                    </Form.Group>
                </Col>

                {/* Campo Drivetrain */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="drivetrain">
                        <Form.Label>Drivetrain</Form.Label>
                        <Form.Control
                            as="select"
                            name="drivetrain"
                            value={filters.drivetrain || ''}
                            onChange={handleTextChange}
                        >
                            <option value="">Select Drivetrain</option>
                            {drivetrains?.map((drivetrain) => (
                                <option key={drivetrain} value={drivetrain}>
                                    {drivetrain}
                                </option>
                            ))}
                        </Form.Control>
                    </Form.Group>
                </Col>

                {/* Campo Safety Features */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="safetyFeatures">
                        <Form.Label>Safety Features</Form.Label>
                        {safetyOptions?.map((safety) => (
                            <Form.Check
                                key={safety}
                                type="checkbox"
                                label={safety}
                                value={safety}
                                checked={filters.safetyFeatures?.includes(safety) || false}
                                onChange={handleArrayChange}
                                name="safetyFeatures"
                            />
                        ))}
                    </Form.Group>
                </Col>

                {/* Campo Infotainments */}
                <Col md={3} className="mb-3">
                    <Form.Group controlId="infotainments">
                        <Form.Label>Infotainments</Form.Label>
                        {infotainmentOptions?.map((infotainment) => (
                            <Form.Check
                                key={infotainment}
                                type="checkbox"
                                label={infotainment}
                                value={infotainment}
                                checked={filters.infotainments?.includes(infotainment) || false}
                                onChange={handleArrayChange}
                                name="infotainments"
                            />
                        ))}
                    </Form.Group>
                </Col>
            </Row>
        </>
    );
};

export default FormCarModel;
