import React, { useState, useEffect } from 'react';
import { Modal, Button } from 'react-bootstrap';
import FormCarModel from "../forms/FormCarModel.tsx";

interface CarModel {
    id?: number;
    brand: string;
    model: string;
    modelYear: number;
    segment: string;
    doorsNo: number;
    seatingCapacity: number;
    luggageCapacity: number;
    category: string;
    manufacturer: string;
    airConditioning: boolean;
    costPerDay: number;
    drivetrain: string;
    engine: string;
    infotainments: string[];
    motorDisplacement: number;
    safetyFeatures: string[];
    transmission: string;
}

interface CarModelModalProps {
    show: boolean;
    handleClose: () => void;
    onSubmitCarModel: (carModel: CarModel) => Promise<void>;
    initialData?: CarModel;  // Dati iniziali per la modifica di un modello esistente
    engines: string[];
    transmissions: string[];
    drivetrains: string[];
    safetyOptions: string[];
    categories: string[];
    infotainmentOptions: string[];
}

const CarModelModal: React.FC<CarModelModalProps> = ({ show, handleClose, onSubmitCarModel, initialData,engines,
                                                         transmissions,
                                                         drivetrains,
                                                         safetyOptions,
                                                         categories,
                                                         infotainmentOptions, }) => {
    // Stato del carModel che verrà gestito dal form
    const [carModel, setCarModel] = useState<CarModel>({
        id: undefined,
        brand: "",
        model: "",
        modelYear: 0,
        segment: "",
        doorsNo: 0,
        seatingCapacity: 0,
        luggageCapacity: 0.0,
        category: "",
        manufacturer: "",
        airConditioning: false,
        costPerDay: 0,
        drivetrain: "",
        engine: "",
        infotainments: [],
        motorDisplacement: 0,
        safetyFeatures: [],
        transmission: "",
    });

    // Se initialData cambia, aggiorna il carModel
    useEffect(() => {
        if (initialData) {
            setCarModel(initialData);
        } else {
            setCarModel({
                id: undefined,
                brand: "",
                model: "",
                modelYear: 0,
                segment: "",
                doorsNo: 0,
                seatingCapacity: 0,
                luggageCapacity: 0.0,
                category: "",
                manufacturer: "",
                airConditioning: false,
                costPerDay: 0,
                drivetrain: "",
                engine: "",
                infotainments: [],
                motorDisplacement: 0,
                safetyFeatures: [],
                transmission: "",
            });
        }
    }, [initialData, show]);



    // Gestisci il submit del form
    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        await onSubmitCarModel(carModel);  // Passa il carModel alla funzione di submit
        handleClose();  // Chiudi il modal dopo il submit
    };

    // Modalità di modifica o aggiunta
    const isEditMode = !!initialData;

    return (
        <Modal show={show} onHide={handleClose} size="xl">
            <Modal.Header closeButton>
                <Modal.Title>{isEditMode ? "Edit model" : "Add new model"}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <form onSubmit={handleSubmit}>
                    {/* Il Form dei campi del modello */}
                    <FormCarModel
                        filters={carModel}
                        setFilters={setCarModel}
                        engines={engines}
                        transmissions={transmissions}
                        drivetrains={drivetrains}
                        safetyOptions={safetyOptions}
                        infotainmentOptions={infotainmentOptions}
                        categories={categories}
                    />
                    <Button variant="primary" type="submit">
                        {isEditMode ? "Save changes" : "Add model"}
                    </Button>
                </form>
            </Modal.Body>
        </Modal>
    );
};

export default CarModelModal;

