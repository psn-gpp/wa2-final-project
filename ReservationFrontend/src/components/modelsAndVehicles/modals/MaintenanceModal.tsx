import React, { useState, useEffect } from 'react';
import { Modal, Button, Form } from 'react-bootstrap';
import {Maintenance} from "../../../types.ts";

interface MaintenanceModalProps {
    show: boolean;
    onHide: () => void;
    onSubmit: (data: {
        defect: string;
        date: string;
        completedMaintenance: boolean;
    }) => void;
    initialData?: Maintenance;
}

const MaintenanceModal: React.FC<MaintenanceModalProps> = ({
                                                               show,
                                                               onHide,
                                                               onSubmit,
                                                               initialData = {},
                                                           }) => {
    const [defect, setDefect] = useState('');
    const [date, setDate] = useState('');
    const [completedMaintenance, setCompletedMaintenance] = useState(false);

    useEffect(() => {
        if (show && initialData) {
            setDefect(initialData.defect || '');
            setDate(initialData.date?.slice(0, 10) || '');
            setCompletedMaintenance(initialData.completedMaintenance || false);
        } else if (show && !initialData) {
            // reset fields if adding a new maintenance
            setDefect('');
            setDate('');
            setCompletedMaintenance(false);
        }
    }, [show]);


    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSubmit({ defect, date, completedMaintenance });
        onHide();
    };

    return (
        <Modal show={show} onHide={onHide} backdrop="static">
            <Modal.Header closeButton>
                <Modal.Title>{initialData?.id ? 'Edit' : 'Add'} maintenance</Modal.Title>
            </Modal.Header>
            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    <Form.Group className="mb-3">
                        <Form.Label>Defect</Form.Label>
                        <Form.Control
                            type="text"
                            value={defect}
                            onChange={(e) => setDefect(e.target.value)}
                            required
                        />
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>Date</Form.Label>
                        <Form.Control
                            type="date"
                            value={date}
                            onChange={(e) => setDate(e.target.value)}
                            required
                        />
                    </Form.Group>

                    <Form.Group className="mb-3" controlId="completedMaintenance">
                        <Form.Check
                            type="checkbox"
                            label="Maintenance completed"
                            checked={completedMaintenance}
                            onChange={(e) => setCompletedMaintenance(e.target.checked)}
                        />
                    </Form.Group>
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="secondary" onClick={onHide}>
                        Cancel
                    </Button>
                    <Button variant="primary" type="submit">
                        Save
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default MaintenanceModal;