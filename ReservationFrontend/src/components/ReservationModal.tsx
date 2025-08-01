import React, {useEffect, useState} from "react";
import {Button, Form, Modal, Row} from "react-bootstrap";
import { Reservation } from "../types.ts";

interface ReservationModalProps {
    show: boolean;
    onHide: () => void;
    onCreateReservation?: (reservation: Omit<Reservation, 'id' | 'employeeId' | 'status'|'vehicleId'|'reservationDate'|'version'>) => void;
    onEditReservation?: (reservation:Reservation)=>void
    userId?: number;
    existingReservation?: Reservation;
    isEditMode?: boolean;
    pricePerDay: number,
}

const ReservationModal: React.FC<ReservationModalProps> = ({ show, onHide, onCreateReservation, userId, isEditMode,existingReservation,onEditReservation, pricePerDay }) => {
    const [startDate, setStartDate] = useState<string>("");
    const [endDate, setEndDate] = useState<string>("");
    const [price, setPrice] = useState(0)

    useEffect(()=>{
        if(existingReservation && isEditMode){
            const formattedStartDate = existingReservation.startDate.split("T")[0];
            const formattedEndDate = existingReservation.endDate.split("T")[0];

            setStartDate(formattedStartDate);
            setEndDate(formattedEndDate);
            const firstDate = new Date(formattedStartDate);
            const secondDate = new Date(formattedEndDate);

            const milliSeconds = Math.abs(secondDate.getTime() - firstDate.getTime());

            // Converti i millisecondi in giorni
            const days = Math.ceil(milliSeconds / (1000 * 60 * 60 * 24));
            setPrice(days*pricePerDay)

        }else {
            setStartDate("");
            setEndDate("");
            setPrice(0)

        }
    },[existingReservation, show, isEditMode, pricePerDay])

    useEffect(()=>{
        if(endDate!=="" && startDate!==""){
            const firstDate = new Date(startDate);
            const secondDate = new Date(endDate);

            const milliSeconds = Math.abs(secondDate.getTime() - firstDate.getTime());

            // Converti i millisecondi in giorni
            const days = Math.ceil(milliSeconds / (1000 * 60 * 60 * 24));
            setPrice(days*pricePerDay)
        }

    },[endDate,startDate,pricePerDay])

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!startDate || !endDate) {
            alert("Please fill in all fields.");
            return;
        }

        if(isEditMode && onEditReservation && existingReservation){
            const editedReservation = {
                id:existingReservation.id,
                customerId: existingReservation.customerId,
                startDate:startDate,
                endDate:endDate,
                reservationDate:existingReservation.reservationDate, //takes care only of the first creation of the reservation
                vehicleId:existingReservation.vehicleId,
                status:existingReservation.status,
                employeeId: existingReservation.employeeId,
                paymentAmount:existingReservation.paymentAmount,
                version: existingReservation.version
            }

            onEditReservation(editedReservation)

        }else if(!isEditMode && onCreateReservation && userId){
            const newReservation = {
                customerId: userId,
                startDate: startDate,
                endDate: endDate,
                paymentAmount: price
            };

            onCreateReservation(newReservation);
        }
        onHide();
        setStartDate("");
        setEndDate("");
    };

    return (
        <Modal show={show} onHide={onHide} centered>
            <Modal.Header closeButton>
                <Modal.Title>{isEditMode ? 'Edit Reservation' : 'Book a Vehicle'}</Modal.Title>
            </Modal.Header>

            <Form onSubmit={handleSubmit}>
                <Modal.Body>
                    <Form.Group className="mb-3">
                        <Form.Label>Start Date</Form.Label>
                        <Form.Control
                            type="date"
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                            required
                        />
                    </Form.Group>

                    <Form.Group className="mb-3">
                        <Form.Label>End Date</Form.Label>
                        <Form.Control
                            type="date"
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                            required
                        />
                    </Form.Group>
                    <Row>
                        {"Cost per day: €"+pricePerDay}
                    </Row>
                    <Row>
                        {"Estimated price for the reservation: €" + price}
                    </Row>
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="secondary" onClick={onHide}>
                        Cancel
                    </Button>
                    <Button type="submit" variant="primary">
                        {isEditMode ? 'Update Reservation' : 'Confirm Reservation'}
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
};

export default ReservationModal;