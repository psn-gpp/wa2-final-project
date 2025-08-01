import React, {useContext, useEffect, useState} from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Spinner, Modal, Button } from "react-bootstrap";
import ReservationAPI from "../api/reservationAPI";
import {UserContext} from "../App.tsx";

const LoadingPage: React.FC = () => {
    const navigate = useNavigate();
    const { search } = useLocation();
    const params = new URLSearchParams(search);
    //const token = params.get("token");
    const isCancel = params.get("cancel") === "true";

    const [showModal, setShowModal] = useState(false);
    const [modalMsg, setModalMsg] = useState("");
    const [attempts, setAttempts] = useState(0);

    const context = useContext(UserContext)
    const reservationId=context.paidReservationId




    useEffect(() => {
        if (isCancel) {
            setModalMsg("Hai annullato il pagamento.");
            setShowModal(true);
            return;
        }
        //if (!token) return;

        const interval = setInterval(async () => {
            console.log(" Reservation ID ", reservationId)
            setAttempts(a => a + 1);
            if (attempts >= 10) {
                clearInterval(interval);
                setModalMsg("Timeout: non è stato possibile verificare il pagamento.");
                setShowModal(true);
                return;
            }

            try {
                let status = "debug"
                if (!reservationId) {
                    console.error("Reservation ID is null in LoadingPage");
                    return;
                }
                const paidReservation = await ReservationAPI.getReservationById(reservationId);
                status = paidReservation.status.status
                //console.log(" Reservation: " + paidReservation)
                //console.log("status:", status);
                if (status == "PAYED") {
                    clearInterval(interval);
                    setModalMsg("Pagamento avvenuto con successo!");
                    setShowModal(true);
                } else if (status == "PAYMENT_REFUSED") {
                    clearInterval(interval);
                    setModalMsg("Il pagamento è stato annullato.");
                    setShowModal(true);
                } else {
                    //console.log(" non fa nienteeeeee")
                }
            } catch (err) {
                console.error(err);
            }
        }, 5000);
        return () => clearInterval(interval);
    }, [isCancel, navigate, attempts, reservationId]);

    const handleClose = () => {
        setShowModal(false);
        navigate("/ui/users/:userId");
    };

    return (
        <>
            <div className="d-flex flex-column justify-content-center align-items-center vh-100">
                <Spinner animation="border" />
                <p className="mt-3">Sto verificando lo stato del pagamento…</p>
            </div>
            <Modal show={showModal} onHide={handleClose} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Pagamento</Modal.Title>
                </Modal.Header>
                <Modal.Body>{modalMsg}</Modal.Body>
                <Modal.Footer>
                    <Button onClick={handleClose}>OK</Button>
                </Modal.Footer>
            </Modal>
        </>
    );
}

export default LoadingPage;
